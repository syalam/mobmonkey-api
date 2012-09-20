package com.MobMonkey.Helpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.MobMonkey.Models.CheckIn;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.RequestMediaLite;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;

public final class Locator extends ResourceHelper {
	static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public Locator() {
		super();
	}

	/*
	 * public List<CheckIn> findMonkeysNearBy(long xC, long yC, int yards){
	 * 
	 * DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(new
	 * AttributeValue("eMailAddress"));
	 * 
	 * queryExpression.setRangeKeyCondition(new Condition().)
	 * super.mapper().query(CheckIn.class, queryExpression)
	 * 
	 * 
	 * }
	 */
	public ArrayList<RequestMediaLite> findRequestsNearBy(String latitude,
			String longitude) {
		// 15L*24L*60L*60L*1000L = 15 days
		// DAYL*HOURS*L*MINSL*SECSL*MILLISECSL
		ArrayList<RequestMediaLite> results = new ArrayList<RequestMediaLite>();
		long rightNowPlus3Hours = (new Date()).getTime()
				+ (3L * 60L * 60L * 1000L); // added
											// three
											// hours
		long rightNowMilli = new Date().getTime();
		String rightNowPlus3HourDate = dateFormatter.format(rightNowPlus3Hours);
		String rightNowDate = dateFormatter.format(rightNowMilli);

		// request is scheduled at 2012-09-12T22:10:02.000Z

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		  scanExpression.addFilterCondition( "requestFulfilled", new
				 Condition().withComparisonOperator
				  (ComparisonOperator.EQ).withAttributeValueList( new
				  AttributeValue().withN("0")));
		
		
		/*
		 * scanExpression.addFilterCondition( "scheduleDate", new
		 * Condition().withComparisonOperator
		 * (ComparisonOperator.LT).withAttributeValueList( new
		 * AttributeValue().withS(rightNowDate)));
		 * scanExpression.addFilterCondition( "scheduleDate", new
		 * Condition().withComparisonOperator(
		 * ComparisonOperator.LT).withAttributeValueList( new
		 * AttributeValue().withS(rightNowPlus3HourDate)));
		 */

		List<RequestMedia> scanResult = super.mapper().scan(RequestMedia.class,
				scanExpression);

		for (RequestMedia req : scanResult) {
			// TODO need to take the requests scheduled date and duration and
			// check to see if the current time is
			// greater than this value.. because the request is expired!
			if (isInVicinity(req.getLatitude(), req.getLongitude(), latitude,
					longitude, req.getRadiusInYards())) {

				RequestMediaLite newReq = new RequestMediaLite();
				newReq.setRequestId(req.getRequestId());
				newReq.setMessage(req.getMessage());
				newReq.setType(req.getRequestType());
				results.add(newReq);
			}
		}
		return results;

	}

	public boolean isInVicinity(String requestLat, String requestLong,
			String userLat, String userLong, int radiusInYards) {

		int R = 6371; // Earth's radius in km
		double radiusInkm = radiusInYards * .0009144;
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
	}

}
