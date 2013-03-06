package com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Helpers.Mailer;
import com.MobMonkey.Helpers.NotificationHelper;
import com.MobMonkey.Models.AssignedRequest;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.RequestMediaLite;
import com.MobMonkey.Resources.InboxResource;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;


public class AssignRequestActivitiesImpl extends ResourceHelper implements AssignRequestActivities {
	

	@Override
	@Activity(name = "AssignRequest", version = "1.1")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
	public void assignRequest(String eMailAddress, String latitude,
			String longitude) throws Exception {
		
		ArrayList<RequestMediaLite> reqsNearBy = new ArrayList<RequestMediaLite>();
		
		reqsNearBy = new Locator().findRequestsNearBy(latitude,
				longitude);
		
		for (RequestMediaLite req : reqsNearBy) {
			if (!req.getRequestorEmail().toLowerCase()
					.equals(eMailAddress.toLowerCase())) {
				AssignRequest(eMailAddress, req);
			}
		}
		
		
	}
	
	private void AssignRequest(String eMailAddress, RequestMediaLite req) {
		Device[] deviceIds = null;
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

			String msg = "You've been assigned a request for a(n) "
					+ super.MediaType(req.getMediaType()) + " at "
					+ req.getLocationName();
			
			super.sendNotification(
					deviceIds, msg, badgeCount);
		}
	}
	

}
