package com.MobMonkey.Helpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.text.style.SuperscriptSpan;

import com.MobMonkey.Helpers.FactualHelper;
import com.MobMonkey.Models.Bookmark;
import com.MobMonkey.Models.CheckIn;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.LocationMedia;
import com.MobMonkey.Models.LocationMessage;
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

	@SuppressWarnings("unchecked")
	public List<Location> getLocationsByGeo(Location loc) {
		// TODO add limiting, and paging
		List<Location> tmp = new ArrayList<Location>();
		List<Location> results = new ArrayList<Location>();

		FactualHelper factual = new FactualHelper();
		List<Location> factualLocs = factual.GeoFilter(loc);
		List<Location> mobMonkeyLocs = getMobMonkeyLocationsByGeo(loc);

		tmp.addAll(mobMonkeyLocs);
		tmp.addAll(factualLocs);

		for (Location location : tmp) {
			HashMap<String, LocationMessage> map = new HashMap<String, LocationMessage>();
			String key = location.getLocationId() + ":"
					+ location.getProviderId();
			Object o = super.getFromCache("LOCMSG" + key);
			if (o != null) {
				map = (HashMap<String, LocationMessage>) o;
			} else {

				DynamoDBQueryExpression scanExpression = new DynamoDBQueryExpression(
						new AttributeValue().withS(key));
				PaginatedQueryList<LocationMessage> locMsgs = super.mapper()
						.query(LocationMessage.class, scanExpression);
				for (LocationMessage locmsg : locMsgs) {
					map.put(locmsg.getMessageId(), locmsg);
				}
				super.storeInCache("LOCMSG" + key, 259200, map);

			}
			if (map.size() > 0) {
				Random random = new Random();
				List<String> keys = new ArrayList<String>(map.keySet());
				String randomKey = keys.get(random.nextInt(keys.size()));
				LocationMessage value = map.get(randomKey);
				location.setMessage(value.getMessage());
			}
			results.add(location);
		}

		return results;
	}

	@SuppressWarnings("unchecked")
	public List<Location> getMobMonkeyLocationsByGeo(Location loc) {

		List<Location> results = new ArrayList<Location>();
		List<Location> locs = new ArrayList<Location>();
		Object o = super.getFromCache("MobMonkeyLocationData");
		if (o != null) {
			locs = (List<Location>) o;
		} else {
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

			PaginatedScanList<Location> tmp = super.mapper().scan(
					Location.class, scanExpression);
			locs = tmp.subList(0, tmp.size());

			super.storeInCache("MobMonkeyLocationData", 259200, locs);

		}
		if (loc.getCategoryIds() != null) {
			String[] filterBycatIds = loc.getCategoryIds().split(",");

			for (Location location : locs) {
				if (location.getCategoryIds() != null) {
					String[] locationCatIds = location.getCategoryIds().split(
							",");
					for (String filterCatId : filterBycatIds) {
						for (String locationCatId : locationCatIds) {
							if (filterCatId.trim().equals(locationCatId.trim())) {
								if (Locator.isInVicinity(
										location.getLatitude(), location
												.getLongitude(), loc
												.getLatitude(), loc
												.getLongitude(), Integer
												.parseInt(loc
														.getRadiusInYards()))) {

									results.add(location);
								}
							}
						}
					}
				}
			}
		} else {
			for (Location location : locs) {

				if (Locator.isInVicinity(location.getLatitude(),
						location.getLongitude(), loc.getLatitude(),
						loc.getLongitude(),
						Integer.parseInt(loc.getRadiusInYards()))) {

					results.add(location);
				}
			}
		}
		return results;
	}

	public static List<Location> getLocationsByAddress(Location loc) {
		FactualHelper factual = new FactualHelper();
		return factual.AddressFilter(loc);
	}

	public List<Location> PopulateCounts(List<Location> locations,
			String eMailAddress) {
		// "monkeys=1,images=3,videos=2,livestreaming=false"

		for (Location loc : locations) {
			String counts = "";
			int monkeys = 0;

			// Let's see if there are any users checked in the vicinity
			monkeys = UserCountAtLocation(loc, eMailAddress);

			// how about media
			Map<String, Integer> media = MediaCountAtLocation(loc);

			loc.setMonkeys(monkeys);
			loc.setImages(media.get("images"));
			loc.setVideos(media.get("videos"));
			loc.setLivestreaming(media.get("livestreaming"));

			if (!eMailAddress.equals("")) {

				DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
						new AttributeValue().withS(eMailAddress));

				queryExpression.setRangeKeyCondition(new Condition()
						.withComparisonOperator(ComparisonOperator.EQ)
						.withAttributeValueList(
								new AttributeValue().withS(loc.getLocationId()
										+ ":" + loc.getProviderId())));
				if (super.mapper().count(Bookmark.class, queryExpression) > 0) {
					loc.setBookmark(true);
				} else {
					loc.setBookmark(false);
				}

			}

		}

		return locations;
	}

	@SuppressWarnings("unchecked")
	public int UserCountAtLocation(Location loc, String email) {
		int count = 0;

		Map<String, CheckIn> checkIn = new HashMap<String, CheckIn>();

		// Check if its in cache first
		Object o = super.getFromCache("CheckInData");

		if (o != null) {
			try {
				checkIn = (HashMap<String, CheckIn>) o;

			} catch (IllegalArgumentException e) {

			}
		} else {
			// Lets go get it from DynamoDB and then put it in cache
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

			PaginatedScanList<CheckIn> results = super.mapper().scan(
					CheckIn.class, scanExpression);
			for (CheckIn c : results) {
				checkIn.put(c.geteMailAddress(), c);
			}

			super.storeInCache("CheckInData", 259200, checkIn);
		}

		for (CheckIn c : checkIn.values()) {
			if (!c.geteMailAddress().equals(email)) {
				new Locator();
				if (loc.getLongitude() != null && loc.getLatitude() != null
						&& c.getLatitude() != null && c.getLongitude() != null) {

					if (Locator.isInVicinity(loc.getLatitude(),
							loc.getLongitude(), c.getLatitude(),
							c.getLongitude(), 250)) {
						count++;
					}
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
		String mediaURL = "";
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
			Media r = (Media) super.load(Media.class, l.getRequestId(),
					l.getMediaId());

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
