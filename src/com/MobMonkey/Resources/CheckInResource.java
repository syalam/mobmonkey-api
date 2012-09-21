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

		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0);
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId").get(
				0);
		c.seteMailAddress(eMailAddress);
		c.setPartnerId(partnerId);
		c.setDateCheckedIn(new Date());
		try {
			super.mapper().save(c);
		} catch (Exception exc) {
			return Response.status(500).entity("An error has occured").build();
		}

		// so i have checked in the user at a specific x,y
		// i should check to see if there are any requests in the area
		ArrayList<RequestMediaLite> reqsNearBy = new Locator()
				.findRequestsNearBy(c.getLatitude(), c.getLongitude());

		for (RequestMediaLite req : reqsNearBy) {
			AssignedRequest assReq = new AssignedRequest();
			assReq.seteMailAddress(eMailAddress);
			assReq.setRequestId(req.getRequestId());
			assReq.setMediaType(req.getMediaType());
			assReq.setRequestType(req.getRequestType());
			assReq.setAssignedDate(new Date());
			assReq.setMessage(req.getMessage());
			assReq.setExpiryDate(req.getExpiryDate());
			super.mapper().save(assReq);
		}
		// TODO we should do something here
	/*	if (reqsNearBy.size() > 0) { // Get the users devices

			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			List<Device> scanResult = super.mapper().query(Device.class,
					queryExpression);

			String[] deviceIds = new String[scanResult.size()];

			for (int i = 0; i < deviceIds.length; i++) {
				deviceIds[i] = scanResult.get(i).getDeviceId().toString();
			}

			ApplePNSHelper.send(deviceIds, "There are " + reqsNearBy.size()
					+ " requests for media near you!");

		}*/
		return Response.ok().entity(reqsNearBy).build();

	}
}
