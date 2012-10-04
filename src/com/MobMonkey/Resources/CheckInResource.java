package com.MobMonkey.Resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import android.location.Location;

import com.MobMonkey.Helpers.ApplePNSHelper;
import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Models.AssignedRequest;
import com.MobMonkey.Models.CheckIn;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.RequestMediaLite;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;

@Path("/checkin")
public class CheckInResource extends ResourceHelper {

	public CheckInResource() {
		super();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createCheckInInJSON(CheckIn c, @Context HttpHeaders headers) {
		ArrayList<RequestMediaLite> reqsNearBy = new ArrayList<RequestMediaLite>();
		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0);
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId").get(
				0);
		c.seteMailAddress(eMailAddress);
		c.setPartnerId(partnerId);
		c.setDateCheckedIn(new Date());
		

		String locationId = "";
		String providerId = "";
		String latitude = "";
		String longitude = "";

		// TODO if locationid & providerid is present, then we need to check
		// that!

		// so i have checked in the user at a specific x,y
		// i should check to see if there are any requests in the area
		try {
			locationId = (!c.getLocationId().equals(null)) ? c.getLocationId()
					: "";
			providerId = (!c.getProviderId().equals(null)) ? c.getProviderId()
					: "";
		} catch (Exception exc) {

		}
		try {
			latitude = (!c.getLatitude().equals(null)) ? c.getLatitude() : "";
			longitude = (!c.getLongitude().equals(null)) ? c.getLongitude()
					: "";
		} catch (Exception exc) {

		}
		if (locationId != "" && providerId != "") {
			com.MobMonkey.Models.Location loc = new Locator().reverseLookUp(providerId,
					locationId);
			if (loc.getLatitude() != null && loc.getLongitude() != null) {
				reqsNearBy = new Locator().findRequestsNearBy(loc.getLatitude(),
						loc.getLongitude());
				c.setLatitude(loc.getLatitude());
				c.setLongitude(loc.getLongitude());
			} else if (latitude != "" && longitude != "") {
				reqsNearBy = new Locator().findRequestsNearBy(c.getLatitude(),
						c.getLongitude());

			}
		} else if (latitude != "" && longitude != "") {
			reqsNearBy = new Locator().findRequestsNearBy(c.getLatitude(),
					c.getLongitude());
		}

		for (RequestMediaLite req : reqsNearBy) {
			AssignedRequest assReq = new AssignedRequest();
			assReq.seteMailAddress(eMailAddress);
			assReq.setRequestId(req.getRequestId());
			assReq.setMediaType(req.getMediaType());
			assReq.setRequestType(req.getRequestType());
			assReq.setAssignedDate(new Date());
			assReq.setMessage(req.getMessage());
			assReq.setExpiryDate(req.getExpiryDate());
			assReq.setRequestorEmail(req.getRequestorEmail());
			super.mapper().save(assReq);
		}
		try {
			super.mapper().save(c);
		} catch (Exception exc) {
			return Response.status(500).entity("An error has occured").build();
		}
		return Response.ok().entity(reqsNearBy).build();

	}
}
