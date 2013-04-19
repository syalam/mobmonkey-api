package com.MobMonkey.Resources;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import com.MobMonkey.Models.MiscTable;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.Status;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.model.AttributeValue;

@Path("/defaulttext")
public class DefaultTextResource extends ResourceHelper {

	public DefaultTextResource() {
		super();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDefaultTextInJSON(
			@DefaultValue("10") @QueryParam("maxResults") int maxResults,
			@DefaultValue("1") @QueryParam("pageNum") int pageNum) {

		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS("DefaultText"));

		PaginatedQueryList<MiscTable> defaultTexts = super.mapper().query(
				MiscTable.class, queryExpression);

	
		Map<String, Object> response = new HashMap<String, Object>();

		double pages = (double)defaultTexts.size() / (double)maxResults;
		int numOfPages = (int) Math.ceil(pages);
		int startPosition = (pageNum - 1) * maxResults;
		int tmp = (int) (((pages - (pageNum - 1)) < 1) ? Math.ceil((pages - (pageNum -1))* 10) : maxResults);
		int endPosition =  startPosition + tmp;
		response.put("numberOfPages", numOfPages);
		response.put("page", pageNum);
		response.put("totalItems", defaultTexts.size());
		if (defaultTexts.size() > 0) {
			response.put(
					"defaultTexts",
					defaultTexts.subList(startPosition, endPosition));
		}
		return Response.ok().entity(response).build();

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createDefaultTextInJSON(@QueryParam("text") String text) {

		if (text == null) {
			return Response
					.status(500)
					.entity(new Status("Failure",
							"Query parameter [text] cannot be null", "500"))
					.build();
		} else {
			MiscTable misc = new MiscTable();
			misc.setMiscId("DefaultText");
			misc.setMiscRange(UUID.randomUUID().toString());
			misc.setValue(text);
			super.save(misc, "DefaultText", misc.getMiscRange());

			return Response
					.ok()
					.entity(new Status("Success", "Added text to database",
							misc.getMiscRange())).build();
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDefaultTextInJSON(@QueryParam("id") String id, @QueryParam("text") String text) {

		if (text == null || id == null) {
			return Response
					.status(500)
					.entity(new Status("Failure",
							"Query parameter [id] & [text] cannot be null", "500"))
					.build();
		} else {
			MiscTable misc = (MiscTable)super.load(MiscTable.class, "DefaultText", id);
		    if(misc == null){
		    	return Response.status(500).entity(new Status("Failure", "The ID you specified does not exist in the database, try a PUT", "500")).build();
		    }
			misc.setValue(text);
			super.save(misc, "DefaultText" + misc.getMiscRange());

			return Response
					.ok()
					.entity(new Status("Success", "Updated default text item",
							misc.getMiscRange())).build();
		}
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteDefaultTextInJSON(@QueryParam("id") String id) {

		if (id == null) {
			return Response
					.status(500)
					.entity(new Status("Failure",
							"Query parameter [id] cannot be null", "500"))
					.build();
		} else {
			MiscTable misc = (MiscTable)super.load(MiscTable.class, "DefaultText", id);
		    if(misc == null){
		    	return Response.status(500).entity(new Status("Failure", "The ID you specified does not exist in the database, try a PUT", "500")).build();
		    }
			
			super.delete(misc, "DefaultText", misc.getMiscRange());

			return Response
					.ok()
					.entity(new Status("Success", "Deleted default text item",
							misc.getMiscRange())).build();
		}
	}
	

}
