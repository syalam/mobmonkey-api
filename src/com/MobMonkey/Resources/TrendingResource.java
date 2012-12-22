package com.MobMonkey.Resources;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.FactualHelper;
import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Helpers.SearchHelper;
import com.MobMonkey.Models.Bookmark;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.Trending;
import com.MobMonkey.Models.User;
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

	@Path("/topviewed")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNearMeInJSON(
			@Context HttpHeaders headers,
		//	@QueryParam("timeSpan") String timeSpan,
			@QueryParam("latitude") String latitude,
			@QueryParam("longitude") String longitude,
			@QueryParam("radius") String radius,
			@QueryParam("categoryIds") String categoryIds,
			@DefaultValue("false") @QueryParam("nearby") boolean nearby,
			@DefaultValue("false") @QueryParam("myinterests") boolean myinterests,
			@DefaultValue("false") @QueryParam("bookmarksonly") boolean bookmarks,
			@DefaultValue("false") @QueryParam("countsonly") boolean countonly) {

		String hashKey = "Media";
		User user = super.getUser(headers);

		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		int bookmarkCount = 0;
		int interestCount = 0;
		int topviewedCount = 0;
		int nearbyCount = 0;
	/*	if (timeSpan == null) {
			return Response
					.status(500)
					.entity(new Status(
							"Failure",
							"Need to provide query parameter \'timeSpan\'. Valid timeSpan values are: day, week, or month",
							"")).build();
		}*/

		ArrayList<String> catIds = new ArrayList<String>();

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

		List<Location> sortedList = GetTrends(hashKey, user.geteMailAddress());
	/*	List<Location> sortedList = GetTrends(timeSpan, hashKey, user.geteMailAddress());*/
		List<Location> itemsToRemove = new ArrayList<Location>();

		 topviewedCount = sortedList.size();
		
		//
		if (myinterests) {

			for (Location loc : sortedList) {
				String[] locCats = loc.getCategoryIds().split(",");

				for (String s : locCats) {
					if (!catIds.contains(s)) {
						itemsToRemove.add(loc);
					}else{
						interestCount++;
					}
				}
			}
		}

		if (nearby) {
			for (Location loc : sortedList) {

				if (!Locator.isInVicinity(loc.getLatitude(),
						loc.getLongitude(), latitude, longitude,
						Integer.parseInt(radius))) {
					itemsToRemove.add(loc);
				}else{
					nearbyCount++;
					
				}
			}

		}
		if (bookmarks) {
			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(user.geteMailAddress()));

			PaginatedQueryList<Bookmark> bookmarkList = super.mapper().query(
					Bookmark.class, queryExpression);
			// Create a hashmap that allows for using contains method
			Hashtable<String, String> ht = new Hashtable<String, String>();
			for (Bookmark b : bookmarkList) {
				ht.put(b.getLocprovId(), "");
			}

			for (Location loc : sortedList) {
			  
				if (!ht.containsKey(loc.getLocationId() + ":" + loc.getProviderId())) {
					itemsToRemove.add(loc);
				}else{
					bookmarkCount++;
				}
			}

		}

		
		for (Location i : itemsToRemove) {
			sortedList.remove(i);
		}

		if(countonly){
			counts.put("topviewedCount", topviewedCount);
			counts.put("nearbyCount", nearbyCount);
			counts.put("bookmarkCount", bookmarkCount);
			counts.put("interestCount", interestCount);
			return Response.ok().entity(counts).build();
		}else{
			return Response.ok().entity(sortedList).build();
		}
	

	}

	//private List<Location> GetTrends(String timeSpan, String type, String eMailAddress) {
	private List<Location> GetTrends(String type, String eMailAddress) {
		
		
		long threeHours = 3L * 60L * 60L * 1000L;
		Date now = new Date();
		dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		String timeSpanString = dateFormatter.format(now.getTime() - threeHours);

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
						if (count == 11) {
							sortedList = new SearchHelper()
									.PopulateCounts(sortedList, eMailAddress);
							return sortedList;
						}
					}
				} catch (Exception exc) {

				}
			}
		}

		return new SearchHelper().PopulateCounts(sortedList, eMailAddress);

	}

}
