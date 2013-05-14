package com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Helpers.NotificationHelper;
import com.MobMonkey.Models.AssignedRequest;
import com.MobMonkey.Models.CheckIn;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.RequestMediaLite;
import com.MobMonkey.Models.User;
import com.MobMonkey.Resources.InboxResource;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

public class AssignRequestActivitiesImpl extends ResourceHelper implements
		AssignRequestActivities {

	private final int MAX_NUM_USERS_PER_REQUEST = 10;
	private final int MAX_INAPPROPRIATE_STRIKES = 3;

	@Override
	@Activity(name = "AssignRequest", version = "1.3")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
	public void assignRequest(String eMailAddress, String latitude,
			String longitude) throws Exception {

		ArrayList<RequestMediaLite> reqsNearBy = new ArrayList<RequestMediaLite>();

		reqsNearBy = new Locator().findRequestsNearBy(latitude, longitude);

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

			super.sendNotification(eMailAddress, deviceIds, msg, badgeCount);
		}
	}

	@Override
	@Activity(name = "AssignRequestMedia", version = "1.7")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
	public void assignRequestMedia(String origRequestor, RequestMediaLite rm) {

		rm.setRequestorEmail(origRequestor);
		HashMap<CheckIn, Double> usersNearBy = new Locator().findMonkeysNearBy(
				rm.getLatitude(), rm.getLongitude(), rm.getRadiusInYards());

		int selectCount = 1;
		HashMap<User, Integer> selectedUsers = new HashMap<User, Integer>();
		for (Entry<CheckIn, Double> entry : entriesSortedByValues(usersNearBy)) {
			if (selectCount <= MAX_NUM_USERS_PER_REQUEST) {
				CheckIn c = entry.getKey();
				User user = (User) super.load(User.class, c.geteMailAddress(),
						c.getPartnerId());
				if (user == null) {
					// user is no longer here!
					super.delete(c, c.geteMailAddress());
				}
				try {
					if (!user.isSuspended()
							&& canUserBeAssigned(user)
							&& !user.geteMailAddress().toLowerCase()
									.equals(origRequestor.toLowerCase())) {
						selectedUsers.put(user, user.getRank());
						selectCount++;
					}
				} catch (Exception exc) {

				}
			}
		}

		for (Entry<User, Integer> entry : entriesSortedByValues(selectedUsers)) {
			try {
				AssignRequest(entry.getKey().geteMailAddress(), rm);
			} catch (Exception exc) {

			}
		}

	}

	public boolean canUserBeAssigned(User user) {

		boolean result = true;
		long oneYearInMS = 15L * 24L * 60L * 60L * 1000L;
		Date firstInappropriateStrike = user.getFirstInappropriateStrike();
		int inappropriateStrikes = user.getInappropriateStrikes();
		Date now = new Date();

		if (inappropriateStrikes == 0)
			return true;
		if (firstInappropriateStrike.getTime() > (now.getTime() - oneYearInMS)) {
			// first strike is within one year.
			if (inappropriateStrikes >= MAX_INAPPROPRIATE_STRIKES) {
				// this user has too many strikes, YOU'RE OUTTA THERE
				user.setSuspended(true);
				super.save(user, user.geteMailAddress(), user.getPartnerId());
				return false;
			}
		} else {
			// first strike is older than one year..
			if (inappropriateStrikes < MAX_INAPPROPRIATE_STRIKES) {
				// lets wipe the slate clean
				user.setInappropriateStrikes(0);
				user.setFirstInappropriateStrike(null);
				super.save(user, user.geteMailAddress(), user.getPartnerId());
			}
		}

		return result;

	}

}
