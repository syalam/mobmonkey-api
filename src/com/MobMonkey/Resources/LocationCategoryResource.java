package com.MobMonkey.Resources;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.LocationCategory;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;

@Path("/category")
public class LocationCategoryResource extends ResourceHelper {

	public LocationCategoryResource() {
		super();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createCategoryInJSON(LocationCategory category) {

		
		try {
			super.mapper().save(category);

		} catch (Exception exc) {
			return Response.status(500)
					.entity("Error creating category: " + exc.getMessage())
					.build();
		}
		return Response.ok()
				.entity(category)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<LocationCategory> getCategoriesInJSON() {
		DynamoDBScanExpression scan = new DynamoDBScanExpression();

		/*scan.addFilterCondition("parentId", new Condition()
				.withComparisonOperator(ComparisonOperator.EQ)
				.withAttributeValueList(new AttributeValue().withS("null")));
*/
		PaginatedScanList<LocationCategory> categories = super.mapper().scan(
				LocationCategory.class, scan);

		return categories.subList(0, categories.size());

	}

/*	@GET
	@Path("/{parentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<LocationCategory> getSubCategoriesInJSON(
			@PathParam("parentId") String parentId) {
		DynamoDBQueryExpression queryExp = new DynamoDBQueryExpression(
				new AttributeValue().withS(parentId));

		PaginatedQueryList<LocationCategory> categories = super.mapper().query(
				LocationCategory.class, queryExp);

		return categories.subList(0, categories.size());

	}
*/
}
