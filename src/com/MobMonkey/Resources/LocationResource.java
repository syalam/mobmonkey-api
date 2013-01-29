package com.MobMonkey.Resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.SearchHelper;
import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.LocationMessage;
import com.MobMonkey.Models.LocationProvider;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.Status;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodb.model.AttributeValue;

@Path("/locations")
public class LocationResource extends ResourceHelper {

	public LocationResource() {
		super();
	}

	@Path("/create")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createLocationInJSON(Location loc) {

		LocationProvider locprov = super.mapper().load(LocationProvider.class,
				loc.getProviderId());

		// Right now we are only accepting new locations for MobMonkey
		if (!locprov.getName().toLowerCase().equals("mobmonkey"))
			return Response
					.status(500)
					.entity("Cannot accept location data for the provider you specified.")
					.build();

		try {
			loc.setLocationId(UUID.randomUUID().toString());
			
			super.mapper().save(loc);
			@SuppressWarnings("unchecked")
			List<Location> o = (List<Location>) super.getFromCache("MobMonkeyLocationData");
			o.add(loc);
			super.storeInCache("MobMonkeyLocationData", 259200, o);
			

		} catch (Exception exc) {
			return Response
					.status(500)
					.entity("Error creating the location specified: "
							+ exc.getMessage().toString() + ".").build();
		}

		return Response.ok().entity(loc).build();

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocationRequestsInJSON(
			@QueryParam("locationId") String locationId,
			@QueryParam("providerId") String providerId,
			@Context HttpHeaders headers) {
		Location loc = new Locator().reverseLookUp(providerId, locationId);

		if (loc == null) {
			return Response
					.status(500)
					.entity(new Status(
							"Error",
							"The locationId & providerId you specified does not resolve to a known location",
							"")).build();
		}
		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0)
				.toLowerCase();

		List<Location> request = new ArrayList<Location>();
		request.add(loc);
		List<Location> response = new SearchHelper().PopulateCounts(request,
				eMailAddress);

		return Response.ok().entity(response.get(0)).build();

	}

	@Path("/{type}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocationRequestsInJSON(@PathParam("type") String type,
			@Context HttpHeaders headers) {
		List<Location> response = new ArrayList<Location>();
		List<RequestMedia> results = new ArrayList<RequestMedia>();
		HashMap<String, ArrayList<RequestMedia>> locProvToRequests = new HashMap<String, ArrayList<RequestMedia>>();

		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0)
				.toLowerCase();

		results = new InboxResource().getRequests(type, eMailAddress);
		for (RequestMedia r : results) {
			String key = r.getLocationId() + ":" + r.getProviderId();

			if (type.toLowerCase().equals("assignedrequests")) {
				// need to remove original requestor
				r.seteMailAddress(null);
			}

			if (locProvToRequests.containsKey(key)) {
				ArrayList<RequestMedia> tmp = locProvToRequests.get(key);
				tmp.add(r);
				locProvToRequests.put(key, tmp);
			} else {
				ArrayList<RequestMedia> tmp = new ArrayList<RequestMedia>();
				tmp.add(r);
				locProvToRequests.put(key, tmp);
			}
		}

		for (String key : locProvToRequests.keySet()) {
			String locationId = key.split(":")[0];
			String providerId = key.split(":")[1];

			Location loc = new Locator().reverseLookUp(providerId, locationId);
			loc.setRequests(locProvToRequests.get(key));
			response.add(loc);
		}

		return Response.ok().entity(response).build();
	}

	@Path("/message")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createMessageInJSON(LocationMessage locMsg,
			@Context HttpHeaders headers,
			@QueryParam("providerId") String providerId,
			@QueryParam("locationId") String locationId) {

		if (locationId == null || providerId == null
				|| locMsg.getMessage() == null) {
			return Response
					.status(500)
					.entity(new Status(
							"Failure",
							"You must provide: locationId, providerId, and message.",
							"")).build();

		}

		locMsg.setMessageId(UUID.randomUUID().toString());
		locMsg.setModifiedDate(new Date());
		locMsg.setLocprovId(locationId + ":" + providerId);

		super.mapper().save(locMsg);

		// caching
		String key = "LOCMSG" + locMsg.getLocprovId();

		Object o = super.getFromCache(key);

		if (o != null) {

			@SuppressWarnings("unchecked")
			HashMap<String, LocationMessage> map = (HashMap<String, LocationMessage>) o;
			map.put(locMsg.getMessageId(), locMsg);
			super.storeInCache(key, 259200, map);
		} else {

			HashMap<String, LocationMessage> map = new HashMap<String, LocationMessage>();
			DynamoDBQueryExpression scanExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(locMsg.getLocprovId()));
			PaginatedQueryList<LocationMessage> locMsgs = super.mapper().query(
					LocationMessage.class, scanExpression);
			for (LocationMessage locmsg : locMsgs) {
				map.put(locmsg.getMessageId(), locmsg);
			}
			super.storeInCache(key, 259200, map);
		}

		return Response
				.ok()
				.entity(new Status("Success", "Added new message for location",
						locMsg.getMessageId())).build();

	}
}
