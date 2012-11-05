package com.MobMonkey.Resources;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCategoriesInJSON(
			@QueryParam(value = "categoryId") String categoryId) {

		// Move the catId's and parents into memached for ultra performance

		DynamoDBScanExpression queryExpression = new DynamoDBScanExpression();
		queryExpression.addFilterCondition(
				"parents",
				new Condition().withComparisonOperator(ComparisonOperator.EQ)
						.withAttributeValueList(
								new AttributeValue().withS("[" + categoryId
										+ "]")));

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