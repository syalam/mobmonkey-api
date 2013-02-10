package com.MobMonkey.Helpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.MobMonkey.Models.CheckIn;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.RecurringRequestMedia;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.RequestMediaLite;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;

public final class Locator extends ResourceHelper {
	private static String factual_providerId = "222e736f-c7fa-4c40-b78e-d99243441fae";
	private static String mobmonkey_providerId = "e048acf0-9e61-4794-b901-6a4bb49c3181";
	static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public Locator() {
		super();
	}

	public Location reverseLookUp(String providerId, String locationId) {
		Location loc = new Location();
		if (providerId.toLowerCase().equals(factual_providerId.toLowerCase())) {
			FactualHelper factual = new FactualHelper();
			loc = factual.reverseLookUp(locationId);
		} else if (providerId.toLowerCase().equals(
				mobmonkey_providerId.toLowerCase())) {
			loc = (Location) super.load(Location.class, locationId, providerId);
		}

		return loc;
	}

	public ArrayList<RequestMediaLite> findRequestsNearBy(String latitude,
			String longitude) {
		// 15L*24L*60L*60L*1000L = 15 days
		// DAYL*HOURS*L*MINSL*SECSL*MILLISECSL
		dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		List<RequestMedia> scanResult = null;
		ArrayList<RequestMediaLite> results = new ArrayList<RequestMediaLite>();
		long rightNowPlus3Hours = (new Date()).getTime()
				- (3L * 60L * 60L * 1000L); // subtracted 3 hours
											// three
											// hours
		long rightNowMilli = new Date().getTime();
		String rightNowMinus3HourDate = dateFormatter
				.format(rightNowPlus3Hours);
		String rightNowDate = dateFormatter.format(rightNowMilli);

		// This is for non-recurring media requests.
		// Check cache first
		Object o = super.getFromCache("RequestTable");

		if (o != null) {
			@SuppressWarnings("unchecked")
			List<RequestMedia> tmp = (List<RequestMedia>) o;
			scanResult = tmp;

		} else {

			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

			scanExpression
					.addFilterCondition(
							"scheduleDate",
							new Condition()
									.withComparisonOperator(
											ComparisonOperator.GT)
									.withAttributeValueList(
											new AttributeValue()
													.withS(rightNowMinus3HourDate)));

			scanResult = super.mapper()
					.scan(RequestMedia.class, scanExpression);
			super.storeInCache("RequestTable", 259200, scanResult);

		}

		for (RequestMedia req : scanResult) {
			Date expiryDate = new Date();
			expiryDate.setTime(req.getScheduleDate().getTime()
					+ (req.getDuration() * 60L * 1000L));

			if ((req.getScheduleDate().getTime() < rightNowMilli)
					&& (rightNowMilli < expiryDate.getTime())) {

				// if the request is made without latitude longitude, lets look
				// it up by reversing the coordinates
				String locationId = "";
				String providerId = "";
				String reqlatitude = "";
				String reqlongitude = "";
				try {
					locationId = (!req.getLocationId().equals(null)) ? req
							.getLocationId() : "";
					providerId = (!req.getProviderId().equals(null)) ? req
							.getProviderId() : "";
					Location coords = new Locator().reverseLookUp(providerId,
							locationId);
					reqlatitude = coords.getLatitude();
					reqlongitude = coords.getLongitude();
				} catch (Exception exc) {
					try {
						reqlatitude = (!req.getLatitude().equals(null)) ? req
								.getLatitude() : "";
						reqlongitude = (!req.getLongitude().equals(null)) ? req
								.getLongitude() : "";
					} catch (Exception exc2) {

					}
				}

				if (isInVicinity(reqlatitude, reqlongitude, latitude,
						longitude, req.getRadiusInYards())) {

					RequestMediaLite newReq = new RequestMediaLite();
					newReq.setRequestId(req.getRequestId());
					newReq.setMessage(req.getMessage());
					newReq.setMediaType(req.getMediaType());
					newReq.setExpiryDate(expiryDate);
					newReq.setRequestorEmail(req.geteMailAddress());
					newReq.setLocationId(req.getLocationId());
					newReq.setProviderId(req.getProviderId());
					newReq.setLatitude(req.getLatitude());
					newReq.setLongitude(req.getLongitude());
					newReq.setLocationName(req.getNameOfLocation());
					newReq.setRequestDate(req.getRequestDate());
					if (req.isRecurring())
						newReq.setRequestType(1);
					else
						newReq.setRequestType(0);
					results.add(newReq);
				}
			}
		}

		// This is for recurring requests.. let the fun begin!

		List<RecurringRequestMedia> recurringScanResult = null;

		// This is for recurring media requests.
		// Check cache first
		Object o2 = super.getFromCache("RecurringRequestTable");

		if (o2 != null) {
			@SuppressWarnings("unchecked")
			List<RecurringRequestMedia> tmp2 = (List<RecurringRequestMedia>) o;
			recurringScanResult = tmp2;

		} else {
			DynamoDBScanExpression scanExpressionRecurring = new DynamoDBScanExpression();
			recurringScanResult = super.mapper().scan(
					RecurringRequestMedia.class, scanExpressionRecurring);
			super.storeInCache("RecurringRequestTable", 259200, scanResult);

		}

		for (RecurringRequestMedia rm : recurringScanResult) {

			if (isInVicinity(rm.getLatitude(), rm.getLongitude(), latitude,
					longitude, rm.getRadiusInYards())) {

				if (rightNowMilli > rm.getScheduleDate().getTime()) {
					Object[] result = isDuringRecurringTimeFrame(rm, new Date());
					if ((Integer) result[0] == 1) {
						RequestMediaLite newSReq = new RequestMediaLite();
						newSReq.setRequestId(rm.getRequestId());
						newSReq.setMediaType(rm.getRequestType());
						newSReq.setRequestType(1);
						newSReq.setMessage(rm.getMessage());
						newSReq.setExpiryDate((Date) result[1]);
						newSReq.setLocationId(rm.getLocationId());
						newSReq.setProviderId(rm.getProviderId());
						newSReq.setLocationName(rm.getNameOfLocation());
						newSReq.setRequestorEmail(rm.geteMailAddress());
						newSReq.setRequestDate(rm.getRequestDate());
						results.add(newSReq);
					}
				}
			}
		}

		return results;

	}

	public List<String> findMonkeysNearBy(String latitude, String longitude,
			int radiusInYards) {

		List<String> results = new ArrayList<String>();
		Object o = super.getFromCache("CheckInData");
		if (o != null) {
			try {
				@SuppressWarnings("unchecked")
				Map<String, CheckIn> checkIn = (HashMap<String, CheckIn>) o;

				for (CheckIn c : checkIn.values()) {
					if (Locator.isInVicinity(latitude, longitude,
							c.getLatitude(), c.getLongitude(), radiusInYards)) {
						results.add(c.geteMailAddress());
					}
				}

			} catch (IllegalArgumentException e) {

			}
		} else {
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
			PaginatedScanList<CheckIn> checkIn = super.mapper().scan(
					CheckIn.class, scanExpression);

			for (CheckIn c : checkIn) {
				if (Locator.isInVicinity(latitude, longitude, c.getLatitude(),
						c.getLongitude(), radiusInYards)) {
					results.add(c.geteMailAddress());
				}
			}
		}

		return results;
	}

	public Object[] isDuringRecurringTimeFrame(RecurringRequestMedia rm,
			Date now) {
		Object[] results = new Object[2];
		Date expiryDate = new Date();
		long rightNow = now.getTime() / 1000;
		long scheduleDate = rm.getScheduleDate().getTime() / 1000;
		long frequencyInMS = rm.getFrequencyInMS();
		long duration = rm.getDuration() * 60000; // convert duration to
													// milliseconds

		double x = Math.abs((scheduleDate - rightNow) % frequencyInMS);
		double y = ((x * 1000) / frequencyInMS);
		double z = (y % 1) * frequencyInMS;

		if (z < duration) {
			// So we are in the valid timespan of this requst.
			// We should check to see the last time it was fufilled
			// rm.getFulfilledDate() and frequency

			int iterations = (int) y;
			int i = (int) (y * frequencyInMS + scheduleDate); // this should be
																// the beginning
																// of the
																// current
																// iteration of
																// the series
			expiryDate.setTime((scheduleDate * 1000)
					+ (iterations * frequencyInMS) + duration);
			try {
				if ((rm.getFulfilledDate().getTime() / 1000) > i) {
					// we have already fulfilled for this series
					results[0] = new Integer(0);
				} else {

					results[0] = new Integer(1);
				}
			} catch (NullPointerException exc) {
				// no fulfillment date. return true
				results[0] = new Integer(1);
			}
		} else
			results[0] = new Integer(0);

		if ((Integer) results[0] == 1)
			results[1] = expiryDate;
		return results;

	}

	public static boolean isInVicinity(String requestLat, String requestLong,
			String userLat, String userLong, int radiusInYards) {
		try {
			int R = 6371; // Earth's radius in km
			double radiusInkm = radiusInYards * .0009144; // convert yards to KM
			Double rLat = Math.toRadians(Double.parseDouble(requestLat));
			Double rLong = Math.toRadians(Double.parseDouble(requestLong));
			Double uLat = Math.toRadians(Double.parseDouble(userLat));
			Double uLong = Math.toRadians(Double.parseDouble(userLong));

			Double x = (uLong - rLong) * Math.cos((rLat + uLat) / 2);
			Double y = (uLat - rLat);
			Double d = Math.sqrt(x * x + y * y) * R;
			if (d <= radiusInkm)
				return true;
			else
				return false;
		} catch (Exception exc) {
			return false;
		}
		
	}

}
