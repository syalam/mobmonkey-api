package com.MobMonkey.Resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Models.RecurringRequestMedia;
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

		PaginatedScanList<RequestMedia> media = super.mapper().scan(
				RequestMedia.class, scan);

		return media.subList(0, media.size());

	}

	@POST
	@Path("/{mediaType}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createRequestMediaInJSON(
			@PathParam("mediaType") String mediaTypeS, RequestMedia r,
			@Context HttpHeaders headers) {
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
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId").get(
				0);

		// TODO Has user verified their email? Need to move this to a helper
		// class, we're going to use it a bunch
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

		String[] coords = new String[2];
		//so lets reverse lookup some coords if they havent proivded them
		if(r.getProviderId() != null && r.getLocationId() != null){
			coords = new Locator().reverseLookUp(r.getProviderId(),
				r.getLocationId());
			r.setLatitude(coords[0]);
			r.setLongitude(coords[1]);
		}
		r.setRequestId(UUID.randomUUID().toString());
		r.setPartnerId(partnerId);
		r.seteMailAddress(username);
		r.setMediaType(mediaType);

		if (r.getScheduleDate() == null) {
			r.setScheduleDate(new Date());
		}

		
		
		// lets check if its a recurring request
		if (r.isRecurring()) {

			RecurringRequestMedia rm = new RecurringRequestMedia();
			rm.setDuration(r.getDuration());
			rm.seteMailAddress(r.geteMailAddress());
			rm.setFulfilledDate(r.getFulfilledDate());
			rm.setLatitude(r.getLatitude());
			rm.setLocationId(r.getLocationId());
			rm.setLongitude(r.getLongitude());
			rm.setMessage(r.getMessage());
			rm.setPartnerId(partnerId);
			rm.setProviderId(r.getProviderId());
			rm.setRadiusInYards(r.getRadiusInYards());
			rm.setRecurring(true);
			rm.setRequestFulfilled(false);
			rm.setRequestId(r.getRequestId());
			rm.setRequestType(1);
			rm.setScheduleDate(r.getScheduleDate());
			rm.setFrequencyInMS(r.getFrequencyInMS());
			super.mapper().save(rm);
		} else {
			r.setRequestType(0);
			super.mapper().save(r);
		}
		String response = "requestID:" + r.getRequestId();
		// user officially makes a request.. lets increment his request value
		// TODO move the number of requests to caching
		// also make all requests JSON
		user.setNumberOfRequests(user.getNumberOfRequests() + 1);
		super.mapper().save(user);

		if (user.getNumberOfRequests() % 5 == 0)
			response += ",displayAd=true";
		else
			response += ",displayAd=false";

		return Response.ok().entity(response).build();

	}

}
