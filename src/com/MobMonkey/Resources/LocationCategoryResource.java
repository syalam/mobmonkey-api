package com.MobMonkey.Resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.FactualHelper;
import com.MobMonkey.Models.LocationCategory;
import com.MobMonkey.Models.Status;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.factual.driver.Factual;

@Path("/category")
public class LocationCategoryResource extends ResourceHelper {

	public LocationCategoryResource() {
		super();
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createCategoryMappingInJSON(@Context HttpHeaders headers,
			@QueryParam("categoryName") String categoryName,
			@QueryParam("categoryList") String categoryList) {
		String response_text = "Successfully mapped categories: ";
		String[] locIds = categoryList.split(",");
		List<String> locIdsVerified = new ArrayList<String>();
		for (String locId : locIds) {
			LocationCategory locCat = (LocationCategory) super.load(
					LocationCategory.class, locId);
			response_text += String.format("%s - [%s]", locCat.getEn(),
					locCat.getCategoryId());
			locIdsVerified.add(locCat.getCategoryId());
		}

		String newCatName = "MMCAT:" + categoryName;
		LocationCategory tmp = (LocationCategory) super.load(
				LocationCategory.class, newCatName);
		if (tmp == null) {
			LocationCategory newLocCat = new LocationCategory();
			newLocCat.setCategoryId(newCatName);
			newLocCat.setParents(super.join(",", locIdsVerified.toArray()));
			super.save(newLocCat, newCatName);

		} else {
			response_text = "Unable to add category, it already exists.  Please use the POST method for updating categories.";
			return Response.status(Response.Status.CONFLICT)
					.entity(new Status("FAILURE", response_text, "")).build();
		}

		return Response.ok().entity(new Status("SUCCESS", response_text, ""))
				.build();

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateCategoryMappingInJSON(@Context HttpHeaders headers,
			@QueryParam("categoryName") String categoryName,
			@QueryParam("categoryList") String categoryList) {
		String response_text = "Successfully mapped categories: ";
		String[] locIds = categoryList.split(",");
		List<String> locIdsVerified = new ArrayList<String>();
		for (String locId : locIds) {
			LocationCategory locCat = (LocationCategory) super.load(
					LocationCategory.class, locId);
			response_text += String.format("%s - [%s]", locCat.getEn(),
					locCat.getCategoryId());
			locIdsVerified.add(locCat.getCategoryId());
		}

		String newCatName = "MMCAT:" + categoryName;
		LocationCategory tmp = (LocationCategory) super.load(
				LocationCategory.class, newCatName);
		if (tmp != null) {
			LocationCategory newLocCat = new LocationCategory();
			newLocCat.setCategoryId(newCatName);
			newLocCat.setParents(super.join(",", locIdsVerified.toArray()));
			super.save(newLocCat, newCatName);

		} else {
			response_text = "Unable to update category. Please use the PUT method for creating new categories.";
			return Response.status(Response.Status.CONFLICT)
					.entity(new Status("FAILURE", response_text, "")).build();
		}

		return Response.ok().entity(new Status("SUCCESS", response_text, ""))
				.build();

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCategoryInJSON(@Context HttpHeaders headers,
			@QueryParam("categoryName") String categoryName) {

		HashMap<String, List<LocationCategory>> resultMap = new HashMap<String, List<LocationCategory>>();

		if (categoryName == null) {

			@SuppressWarnings("unchecked")
			HashMap<String, List<LocationCategory>> fromCache = (HashMap<String, List<LocationCategory>>) super
					.getFromCache("MobMonkeyCats");
			if (fromCache == null) {
				DynamoDBScanExpression queryExpression = new DynamoDBScanExpression();
				queryExpression.addFilterCondition(
						"categoryId",
						new Condition().withComparisonOperator(
								ComparisonOperator.BEGINS_WITH)
								.withAttributeValueList(
										new AttributeValue().withS("MMCAT")));
				PaginatedScanList<LocationCategory> results = super.mapper()
						.scan(LocationCategory.class, queryExpression);

				for (LocationCategory loc : results) {

					List<LocationCategory> mappings = new ArrayList<LocationCategory>();
					for (String s : loc.getParents().split(",")) {
						LocationCategory tmp = (LocationCategory) super.load(
								LocationCategory.class, s);
						mappings.add(tmp);
					}
					resultMap.put(loc.getCategoryId().split(":")[1], mappings);

				}
				super.storeInCache("MobMonkeyCats", 259200, resultMap);
			} else {
				resultMap = fromCache;
			}

		} else {
			LocationCategory loc = (LocationCategory) super.load(
					LocationCategory.class, "MMCAT:" + categoryName);

			@SuppressWarnings("unchecked")
			HashMap<String, List<LocationCategory>> fromCache = (HashMap<String, List<LocationCategory>>) super
					.getFromCache("MobMonkeyCats" + categoryName);
			if (fromCache == null) {
				List<LocationCategory> mappings = new ArrayList<LocationCategory>();
				for (String s : loc.getParents().split(",")) {
					LocationCategory tmp = (LocationCategory) super.load(
							LocationCategory.class, s);
					mappings.add(tmp);
				}
				resultMap.put(loc.getCategoryId().split(":")[1], mappings);
				super.storeInCache("MobMonkeyCats" + categoryName, 259200,
						resultMap);
			} else {
				resultMap = fromCache;
			}
		}
		return Response.ok().entity(resultMap).build();

	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCategoriesInJSON() {

		// Move the catId's and parents into memached for ultra performance

		DynamoDBScanExpression queryExpression = new DynamoDBScanExpression();

		queryExpression.addFilterCondition("categoryId", new Condition()
				.withComparisonOperator(ComparisonOperator.NOT_CONTAINS)
				.withAttributeValueList(new AttributeValue().withS("MMCAT")));
		PaginatedScanList<LocationCategory> results = super.mapper().scan(
				LocationCategory.class, queryExpression);

		return Response.ok().entity(results).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/loadfactualcategories")
	public Response loadFactualCategoriesInJSON() {

		String status = new FactualHelper().LoadCategories();

		return Response.ok().entity(new Status("Success", status, "")).build();
	}

}