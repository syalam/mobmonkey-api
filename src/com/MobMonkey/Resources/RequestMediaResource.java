package com.MobMonkey.Resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Helpers.NotificationHelper;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.RecurringRequestMedia;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.RequestMediaLite;
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

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteRequestMediaInJSON(@Context HttpHeaders headers,
			@QueryParam("requestId") String requestId,
			@QueryParam("isRecurring") boolean isRecurring) {
		String username = headers.getRequestHeader("MobMonkey-user").get(0);

		if (isRecurring) {
			try {
				RecurringRequestMedia rm = super.mapper().load(
						RecurringRequestMedia.class, username, requestId);
				super.mapper().delete(rm);
				return Response
						.ok()
						.entity(new Status("Success", "Successfully deleted",
								requestId)).build();
			} catch (Exception exc) {
				return Response
						.status(500)
						.entity(new Status("Failure",
								"Unable to find ID in database", requestId))
						.build();
			}
		} else if (!isRecurring) {
			try {
				RequestMedia rm = super.mapper().load(RequestMedia.class,
						username, requestId);
				super.mapper().delete(rm);
				return Response
						.ok()
						.entity(new Status("Success", "Successfully deleted",
								requestId)).build();
			} catch (Exception exc) {
				return Response
						.status(500)
						.entity(new Status("Failure",
								"Unable to find ID in database", requestId))
						.build();
			}
		} else {

			return Response
					.status(500)
					.entity(new Status(
							"Failure",
							"Please specify whether or not the request is recurring or non-recurring",
							"")).build();
		}

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
		if (mediaTypeS.trim().toLowerCase().equals("livestreaming"))
			mediaType = 3;
		if (mediaTypeS.trim().toLowerCase().equals("text"))
			mediaType = 4;

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
			coords = new Locator().reverseLookUp(r.getProviderId(),
					r.getLocationId());
			if (coords.getLatitude() != null && coords.getLongitude() != null) {
				r.setLatitude(coords.getLatitude());
				r.setLongitude(coords.getLongitude());

				// lets set the name of the location
				r.setNameOfLocation(coords.getName());

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
							"Please provide a provider and location ID", ""))
					.build();
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
			
			r.setPartnerId(partnerId);
			r.setRecurring(true);
			r.setRequestFulfilled(false);
			r.setRequestType(1);
			r.setRequestDate(now);
			RecurringRequestMedia rm = convertToRRM(r);
			
			
			super.mapper().save(rm);
			
			//Update the cache
			Object o = super.getFromCache("ReccuringRequestTable");

			if (o != null) {
				@SuppressWarnings("unchecked")
				List<RecurringRequestMedia> tmp = (List<RecurringRequestMedia>) o;
				
				tmp.add(rm);
				super.storeInCache("ReccuringRequestTable", 259200, tmp);
			}
			
			
		} else {
			r.setRequestType(0);
			super.mapper().save(r);
			
			//Update the cache
			Object o = super.getFromCache("RequestTable");

			if (o != null) {
				@SuppressWarnings("unchecked")
				List<RequestMedia> tmp = (List<RequestMedia>) o;
				
				tmp.add(r);
				super.storeInCache("RequestTable", 259200, tmp);
			}
		}

		// user officially makes a request.. lets increment his request value
		// TODO move the number of requests to caching
		// also make all requests JSON
		user.setNumberOfRequests(user.getNumberOfRequests() + 1);
		super.mapper().save(user);

		// Check to see if there are users by and assign them the request
		List<String> eMailAddresses = new Locator().findMonkeysNearBy(
				r.getLatitude(), r.getLongitude(), r.getRadiusInYards());
		CheckInResource cir = new CheckInResource();
		for (String eMail : eMailAddresses) {
			if (!eMail.toLowerCase().equals(r.geteMailAddress())) {
				cir.AssignRequest(eMail, convertToRML(r));
			}
		}

		// Trending metric!
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

		return Response.ok().entity(status).build();

	}

	public RequestMediaLite convertToRML(RequestMedia r) {

		Date expiryDate = new Date();
		expiryDate.setTime(r.getScheduleDate().getTime()
				+ (r.getDuration() * 60L * 1000L));

		RequestMediaLite newReq = new RequestMediaLite();
		newReq.setRequestId(r.getRequestId());
		newReq.setMessage(r.getMessage());
		newReq.setMediaType(r.getMediaType());
		newReq.setExpiryDate(expiryDate);
		newReq.setRequestorEmail(r.geteMailAddress());
		newReq.setLocationId(r.getLocationId());
		newReq.setProviderId(r.getProviderId());
		newReq.setLatitude(r.getLatitude());
		newReq.setLongitude(r.getLongitude());
		newReq.setLocationName(r.getNameOfLocation());
		
		if (r.isRecurring())
			newReq.setRequestType(1);
		else
			newReq.setRequestType(0);
		return newReq;

	}
	
	public RequestMedia convertToRM(RecurringRequestMedia r){
	
	    RequestMedia rm = new RequestMedia();
		rm.setDuration(r.getDuration());
		rm.seteMailAddress(r.geteMailAddress());
		rm.setFulfilledDate(r.getFulfilledDate());
		rm.setLatitude(r.getLatitude());
		rm.setLocationId(r.getLocationId());
		rm.setLongitude(r.getLongitude());
		rm.setMessage(r.getMessage());
		rm.setPartnerId(r.getPartnerId());
		rm.setProviderId(r.getProviderId());
		rm.setRadiusInYards(r.getRadiusInYards());
		rm.setRecurring(r.isRecurring());
		rm.setRequestFulfilled(r.isRequestFulfilled());
		rm.setRequestId(r.getRequestId());
		rm.setRequestType(1);
		rm.setScheduleDate(r.getScheduleDate());
		rm.setFrequencyInMS(r.getFrequencyInMS());
		rm.setRequestDate(r.getRequestDate());
		rm.setNameOfLocation(r.getNameOfLocation());
		rm.setExpired(r.isExpired());
		
		return rm;
		
	}

	public RecurringRequestMedia convertToRRM(RequestMedia r){
		
	    RecurringRequestMedia rm = new RecurringRequestMedia();
		rm.setDuration(r.getDuration());
		rm.seteMailAddress(r.geteMailAddress());
		rm.setFulfilledDate(r.getFulfilledDate());
		rm.setLatitude(r.getLatitude());
		rm.setLocationId(r.getLocationId());
		rm.setLongitude(r.getLongitude());
		rm.setMessage(r.getMessage());
		rm.setPartnerId(r.getPartnerId());
		rm.setProviderId(r.getProviderId());
		rm.setRadiusInYards(r.getRadiusInYards());
		rm.setRecurring(r.isRecurring());
		rm.setRequestFulfilled(r.isRequestFulfilled());
		rm.setRequestId(r.getRequestId());
		rm.setRequestType(1);
		rm.setScheduleDate(r.getScheduleDate());
		rm.setFrequencyInMS(r.getFrequencyInMS());
		rm.setRequestDate(r.getRequestDate());
		rm.setNameOfLocation(r.getNameOfLocation());
		rm.setExpired(r.isExpired());
		
		return rm;
		
	}
	
}
