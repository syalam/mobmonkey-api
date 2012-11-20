package com.MobMonkey.Helpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.style.SuperscriptSpan;

import com.MobMonkey.Helpers.FactualHelper;
import com.MobMonkey.Models.CheckIn;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.LocationMedia;
import com.MobMonkey.Models.Media;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;

public final class SearchHelper extends ResourceHelper {
	static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public SearchHelper() {
		super();
	}

	public List<Location> getLocationsByGeo(Location loc) {
		// TODO add limiting, and paging
		List<Location> results = new ArrayList<Location>();

		FactualHelper factual = new FactualHelper();
		List<Location> factualLocs = factual.GeoFilter(loc);
		List<Location> mobMonkeyLocs = getMobMonkeyLocationsByGeo(loc);

		results.addAll(mobMonkeyLocs);
		results.addAll(factualLocs);
		return results;
	}

	public List<Location> getMobMonkeyLocationsByGeo(Location loc) {

		List<Location> results = new ArrayList<Location>();

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

		PaginatedScanList<Location> locs = super.mapper().scan(Location.class,
				scanExpression);
		for (Location location : locs) {
			if (Locator.isInVicinity(location.getLatitude(),
					location.getLongitude(), loc.getLatitude(),
					loc.getLongitude(),
					Integer.parseInt(loc.getRadiusInYards()))) {
				results.add(location);
			}
		}
		return results;
	}

	public static List<Location> getLocationsByAddress(Location loc) {
		FactualHelper factual = new FactualHelper();
		return factual.AddressFilter(loc);
	}

	public List<Location> PopulateCounts(List<Location> locations) {
		// "monkeys=1,images=3,videos=2,livestreaming=false"

		for (Location loc : locations) {
			String counts = "";
			int monkeys = 0;

			// Let's see if there are any users checked in the vicinity
			monkeys = UserCountAtLocation(loc);

			// how about media
			Map<String, Integer> media = MediaCountAtLocation(loc);

			loc.setMonkeys(monkeys);
			loc.setImages(media.get("images"));
			loc.setVideos(media.get("videos"));
			loc.setLivestreaming(media.get("livestreaming"));
		}

		return locations;
	}

	@SuppressWarnings("unchecked")
	public int UserCountAtLocation(Location loc) {
		int count = 0;

		List<CheckIn> checkIn = null;

		// Check if its in cache first
		Object o = super.getFromCache("CheckInData");

		if (o != null) {
			try {
				checkIn = (List<CheckIn>) o;

			} catch (IllegalArgumentException e) {

			}
		} else {
			// Lets go get it from DynamoDB and then put it in cache
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

			PaginatedScanList<CheckIn> results = super.mapper().scan(
					CheckIn.class, scanExpression);
			checkIn = results.subList(0, results.size());

			super.storeInCache("CheckInData", 259200, checkIn);
		}

		for (CheckIn c : checkIn) {
			new Locator();
			if (loc.getLongitude() != null && loc.getLatitude() != null
					&& c.getLatitude() != null && c.getLongitude() != null) {

				if (Locator.isInVicinity(loc.getLatitude(), loc.getLongitude(),
						c.getLatitude(), c.getLongitude(), 250)) {
					count++;
				}
			}
		}

		return count;
	}

	public Map<String, Integer> MediaCountAtLocation(Location loc) {
		Map<String, Integer> results = new HashMap<String, Integer>();
		int images = 0;
		int videos = 0;
		int livestreaming = 0;
		long rightNowMinus3Days = (new Date()).getTime()
				- (3L * 24L * 60L * 60L * 1000L); // subtracted 3 days

		String rightNowMinus3DaysDate = dateFormatter
				.format(rightNowMinus3Days);

		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(loc.getLocationId() + ":"
						+ loc.getProviderId()));

		queryExpression.setRangeKeyCondition(new Condition()
				.withComparisonOperator(ComparisonOperator.GT)
				.withAttributeValueList(
						new AttributeValue().withS(rightNowMinus3DaysDate)));

		PaginatedQueryList<LocationMedia> requests = super.mapper().query(
				LocationMedia.class, queryExpression);

		for (LocationMedia l : requests) {
			Media r = null;
			Object o = super.getFromCache(l.getRequestId() + ":"
					+ l.getMediaId());
			if (o != null) {
				r = (Media) o;
			} else {
				r = super.mapper().load(Media.class, l.getRequestId(),
						l.getMediaId());
				super.storeInCache(l.getRequestId() + ":" + l.getMediaId(),
						259200, r);
			}
			if (r != null) {
				if (r.getMediaType() == 1) {
					images++;
				} else if (r.getMediaType() == 2) {
					videos++;
				} else if (r.getMediaType() == 3) {
					livestreaming++;
				}
			}
		}

		results.put("images", images);
		results.put("videos", videos);
		results.put("livestreaming", livestreaming);

		return results;
	}

}
