package com.MobMonkey.Resources;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.FactualHelper;
import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Helpers.SearchHelper;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.Trending;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;

@Path("/trending")
public class TrendingResource extends ResourceHelper {
	static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public TrendingResource() {
		super();
	}

	@Path("/{type}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNearMeInJSON(
			@PathParam("type") String type,
			@QueryParam("timeSpan") String timeSpan,
			@QueryParam("latitude") String latitude,
			@QueryParam("longitude") String longitude,
			@QueryParam("radius") String radius,
			@QueryParam("categoryIds") String categoryIds,
			@DefaultValue("false") @QueryParam("nearby") boolean nearby,
			@DefaultValue("false") @QueryParam("myinterests") boolean myinterests) {
		String requestType = type.toLowerCase();
		if (timeSpan == null) {
			return Response
					.status(500)
					.entity(new Status(
							"Failure",
							"Need to provide query parameter \'timeSpan\'. Valid timeSpan values are: day, week, or month",
							"")).build();
		}
		String hashKey = "";
		ArrayList<String> catIds = new ArrayList<String>();

		if (requestType.equals("bookmarks"))
			hashKey = "Bookmark";
		if (requestType.equals("topviewed"))
			hashKey = "Media";

		if (nearby) {
			if (latitude == null || longitude == null || radius == null) {
				return Response
						.status(500)
						.entity(new Status(
								"Failure",
								"Need to provide query parameter values for: latitude, longitude & radius. Example: ?latitude=33.23983&longitude=-112.12342&radius=250",
								"")).build();
			}
		}
		if (myinterests) {
			if (categoryIds == null) {
				return Response
						.status(500)
						.entity(new Status(
								"Failure",
								"Need to provide query parameter values for: categoryIds. Example: ?categoryIds=342,3,200",
								"")).build();
			}
			String[] cats = categoryIds.split(",");
			for (String s : cats) {
				catIds.add(s);
			}
		}

		List<Location> results = new ArrayList<Location>();
		List<Location> sortedList = GetTrends(timeSpan, hashKey);

		for (Location loc : sortedList) {
			if (nearby) {
				if (Locator.isInVicinity(loc.getLatitude(), loc.getLongitude(),
						latitude, longitude, Integer.parseInt(radius))) {
					results.add(loc);
				}
			} else if (myinterests) {
				String[] locCats = loc.getCategoryIds().split(",");

				for (String s : locCats) {
					if (catIds.contains(s)) {
						results.add(loc);
					}
				}

			} else {
				results.add(loc);
			}

		}

		return Response.ok().entity(results).build();

	}

	static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
				new Comparator<Map.Entry<K, V>>() {
					@Override
					public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
						int res = e2.getValue().compareTo(e1.getValue());
						return res != 0 ? res : 1; // Special fix to preserve
													// items with equal values
					}
				});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	private List<Location> GetTrends(String timeSpan, String type) {
		int timeSpanInDays = 0;
		if (timeSpan.toLowerCase().equals("day")) {
			timeSpanInDays = 1;
		} else if (timeSpan.toLowerCase().equals("week")) {
			timeSpanInDays = 7;
		} else if (timeSpan.toLowerCase().equals("month")) {
			timeSpanInDays = 31;
		}
		long oneDay = 24L * 60L * 60L * 1000L;
		Date now = new Date();
		dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		String timeSpanString = dateFormatter.format(now.getTime() - oneDay
				* timeSpanInDays);

		DynamoDBQueryExpression scanExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(type));
		scanExpression.setRangeKeyCondition(new Condition()
				.withComparisonOperator(ComparisonOperator.GT)
				.withAttributeValueList(
						new AttributeValue().withS(timeSpanString)));

		PaginatedQueryList<Trending> trends = super.mapper().query(
				Trending.class, scanExpression);

		HashMap<String, Integer> popularity = new HashMap<String, Integer>();
		for (Trending trend : trends) {
			String key = trend.getLocationId() + ":" + trend.getProviderId();
			if (!popularity.containsKey(key)) {
				popularity.put(key, 1);
			} else {
				popularity.put(key, popularity.get(key) + 1);
			}

		}
		List<Location> sortedList = new ArrayList<Location>();

		
		
		
		
		int count = 1;
		for (Entry<String, Integer> entry : entriesSortedByValues(popularity)) {
			String[] locprov = entry.getKey().split(":");
			String providerId = locprov[1];
			String locationId = locprov[0];
			if (locationId != null && providerId != null) {

				Location loc = new Locator().reverseLookUp(providerId,
						locationId);

				try {
					if (loc.getLocationId() != null) {
						loc.setRank(count);
						count++;
						sortedList.add(loc);
						if(count == 11){
							sortedList = new SearchHelper().PopulateCounts(sortedList);
							return sortedList;
						}
					}
				} catch (Exception exc) {

				}
			}
		}
		
		

		return new SearchHelper().PopulateCounts(sortedList);
		
	}

}
