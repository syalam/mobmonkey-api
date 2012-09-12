package com.MobMonkey.Resources;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.User;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;

@Path("/requestmedia")
public class RequestMediaResource extends ResourceHelper {

	public RequestMediaResource() {
		super();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<RequestMedia> getRequestMediaInJSON() {

		DynamoDBScanExpression scan = new DynamoDBScanExpression();

		PaginatedScanList<RequestMedia> media = super.mapper().scan(RequestMedia.class,
				scan);

		return media.subList(0, media.size());

	}

	@POST
	@Path("/{mediaType}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createRequestMediaInJSON(
			@PathParam("mediaType") String mediaTypeS, RequestMedia r, @Context HttpHeaders headers) {
		// TODO implement a request tracking system to determine the number of
		// requests made by a user
		// create an attribute called displayAd and return true based on a
		// predetermined value
		
		int mediaType = 0;
		if (mediaTypeS.trim().toLowerCase().equals("image"))
			mediaType = 1;
		if (mediaTypeS.trim().toLowerCase().equals("video"))
			mediaType = 2;
		// Get username & PartnerId from header
		String username = headers.getRequestHeader("MobMonkey-user").get(0);
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId").get(0);

		// TODO Has user verified their email?  Need to move this to a helper class, we're going to use it a bunch
		User user = super.mapper().load(User.class, username, partnerId);
		try {
			if (!user.isVerified()) {
				return Response.status(401)
						.entity("User has not verified their email address")
						.build();
			}
		} catch (Exception e) {
			return Response.status(500)
					.entity("User was not found in the MobMonkey database.")
					.build();
		}

		r.setRequestId(UUID.randomUUID().toString());

		// saving the request to DB
		r.setRequestType(mediaType);
		super.mapper().save(r);
		return Response.ok()
				.entity(mediaTypeS + " requestID: " + r.getRequestId()).build();

	}

}
