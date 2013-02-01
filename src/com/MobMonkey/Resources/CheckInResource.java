package com.MobMonkey.Resources;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import com.MobMonkey.Helpers.NotificationHelper;
import com.MobMonkey.Models.AssignedRequest;
import com.MobMonkey.Models.CheckIn;
import com.MobMonkey.Models.RequestMediaLite;

@Path("/checkin")
public class CheckInResource extends ResourceHelper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7922156377734286617L;

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

		String latitude = "";
		String longitude = "";

		// so i have checked in the user at a specific x,y
		// i should check to see if there are any requests in the area
		try {
			latitude = (!c.getLatitude().equals(null)) ? c.getLatitude() : "";
			longitude = (!c.getLongitude().equals(null)) ? c.getLongitude()
					: "";
		} catch (Exception exc) {

		}

		if (latitude != "" && longitude != "") {
			reqsNearBy = new Locator().findRequestsNearBy(c.getLatitude(),
					c.getLongitude());
		}

		for (RequestMediaLite req : reqsNearBy) {
			if (!req.getRequestorEmail().toLowerCase()
					.equals(eMailAddress.toLowerCase())) {
				AssignRequest(eMailAddress, req);
			}
		}
		try {
			// Need to update our cache

			Object o = super.getFromCache("CheckInData");

			if (o != null) {
				try {
					@SuppressWarnings("unchecked")
					Map<String, CheckIn> checkIn = (HashMap<String, CheckIn>) o;

					checkIn.put(eMailAddress, c);

					super.storeInCache("CheckInData", 259200, checkIn);

				} catch (IllegalArgumentException e) {

				}
			}

			super.save(c, c.geteMailAddress());
		} catch (Exception exc) {
			return Response.status(500).entity("An error has occured").build();
		}
		return Response.ok().entity(reqsNearBy).build();

	}

	public void AssignRequest(String eMailAddress, RequestMediaLite req) {
		String[] deviceIds = null;
		AssignedRequest assReq = (AssignedRequest) super.load(
				AssignedRequest.class, eMailAddress, req.getRequestId());
		if (assReq == null) {
			assReq = new AssignedRequest();
			assReq.seteMailAddress(eMailAddress);
			assReq.setRequestId(req.getRequestId());
			assReq.setMediaType(req.getMediaType());
			assReq.setRequestType(req.getRequestType());
			assReq.setAssignedDate(new Date());
			assReq.setMessage(req.getMessage());
			assReq.setExpiryDate(req.getExpiryDate());
			assReq.setRequestorEmail(req.getRequestorEmail());
			assReq.setNameOfLocation(req.getLocationName());
			assReq.setProviderId(req.getProviderId());
			assReq.setLocationId(req.getLocationId());
			assReq.setLatitude(req.getLatitude());
			assReq.setLongitude(req.getLongitude());
			assReq.setMarkAsRead(false);
			assReq.setRequestDate(req.getRequestDate());

			try {
				super.save(assReq, eMailAddress, req.getRequestId());
			} catch (Exception exc) {

			}
			try {
				NotificationHelper noteHelper = new NotificationHelper();
				deviceIds = noteHelper.getUserDevices(eMailAddress);
			} catch (Exception exc) {

			}
			super.clearCountCache(eMailAddress);
			HashMap<String, Integer> counts = new InboxResource()
					.getCounts(eMailAddress);

			int badgeCount = counts.get("fulfilledUnreadCount")
					+ counts.get("assignedUnreadRequests");

			try {
				super.sendAPNS(
						deviceIds,
						"You've been assigned a request for a(n) "
								+ super.MediaType(req.getMediaType()) + " at "
								+ req.getLocationName(), badgeCount);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
