package com.MobMonkey.Resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.RecurringRequestMedia;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.Trending;
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
	@Produces(MediaType.APPLICATION_JSON)
	public Response createRequestMediaInJSON(
			@PathParam("mediaType") String mediaTypeS, RequestMedia r,
			@Context HttpHeaders headers) {
		// TODO implement a request tracking system to determine the number of
		// requests made by a user
		// create an attribute called displayAd and return true based on a
		// predetermined value
		Date now = new Date();

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

		Location coords = new Location();
		// so lets reverse lookup some coords if they havent proivded them
		if (r.getProviderId() != null && r.getLocationId() != null) {
			new Locator();
			coords = new Locator()
					.reverseLookUp(r.getProviderId(), r.getLocationId());
			if (coords.getLatitude() != null && coords.getLongitude() != null) {
				r.setLatitude(coords.getLatitude());
				r.setLongitude(coords.getLongitude());
			} else {
				return Response
						.status(500)
						.entity(new Status(
								"Failure",
								"The provider and location ID specified does not resolve to a known location",
								"")).build();
			}
		} else {
			return Response
					.status(500)
					.entity(new Status("Failure",
							"Please provide a provider and location ID", "")).build();
		}
		r.setRequestId(UUID.randomUUID().toString());
		r.setPartnerId(partnerId);
		r.seteMailAddress(username);
		r.setMediaType(mediaType);
		r.setRequestDate(now);
		if (r.getScheduleDate() == null) {
			r.setScheduleDate(now);
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
			rm.setRequestDate(now);
			super.mapper().save(rm);
		} else {
			r.setRequestType(0);
			super.mapper().save(r);
		}
	
		// user officially makes a request.. lets increment his request value
		// TODO move the number of requests to caching
		// also make all requests JSON
		user.setNumberOfRequests(user.getNumberOfRequests() + 1);
		super.mapper().save(user);
		
		//Trending metric!
		Trending t = new Trending();
		t.setLocationId(r.getLocationId());
		t.setProviderId(r.getProviderId());
		t.setTimeStamp(new Date());
		t.setType("Request");
		super.mapper().save(t);
		
		Status status = new Status();
		status.setStatus("Success");
		status.setId(r.getRequestId());
		if (user.getNumberOfRequests() % 5 == 0)

			status.setDescription("DisplayAd=true");
		else
			status.setDescription("DisplayAd=false");
		// TODO turn this into status
		return Response.ok().entity(status).build();

	}

}
