package com.MobMonkey.Resources;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Helpers.SearchHelper;
import com.MobMonkey.Models.CheckIn;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.Media;
import com.MobMonkey.Models.MediaLite;
import com.MobMonkey.Models.RecurringRequestMedia;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.User;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;

@Path("/search")
public class SearchResource extends ResourceHelper {
	static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public SearchResource() {
		super();

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/location")
	public Response findLocationsInJSON(Location loc,
			@Context HttpHeaders headers) {

		// Location now has a count attribute that will look like this:
		// "monkeys=1,images=3,videos=2,livestreaming=false"

		User user = null;
		try {
			user = super.getUser(headers);
		} catch (Exception exc) {

		}
		if (loc.getLatitude() != null && loc.getLongitude() != null
				&& loc.getName() != null && loc.getRadiusInYards() != null) {
			// TODO validate lat/long with regex
			List<Location> locations = new SearchHelper().getLocationsByGeo(
					loc, loc.getName());

			// Populate the counts!
			locations = PopulateCounts(locations);
			
			//If we have a user, check to see if these locations are bookmarked
			if (user != null) {
				List<Location> bookmarkedLocations = this.AssignBookmarks(
						locations, user.geteMailAddress());

				return Response.ok().entity(bookmarkedLocations).build();
			} else {
				return Response.ok().entity(locations).build();
			}
		}
		if (loc.getLocality() != null && loc.getRegion() != null
				&& loc.getPostcode() != null && loc.getName() != null) {
			// TODO validate postcode with regex, make sure locality and region
			// are sane
			List<Location> locations = SearchHelper.getLocationsByAddress(loc);
			return Response.ok().entity(locations).build();

		}
		return Response
				.status(500)
				.entity(new Status(
						"Failure",
						"You need to specify either (lat & long & name & radiusInYards) OR (name, address, locality, region & zip)",
						"")).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/location/glob")
	public Response findLotsOfLocationsInJSON(List<Location> locs) {

		List<Location> locations = new ArrayList<Location>();
		for (Location loc : locs) {
			if (loc.getLatitude() != null && loc.getLongitude() != null
					&& loc.getName() != null) {
				// TODO validate lat/long with regex
				List<Location> locList = new SearchHelper().getLocationsByGeo(
						loc, loc.getName());
				locations.addAll(locList);

			}
			if (loc.getLocality() != null && loc.getRegion() != null
					&& loc.getPostcode() != null) {
				// TODO validate postcode with regex, make sure locality and
				// region are sane
				List<Location> locList = SearchHelper
						.getLocationsByAddress(loc);
				locations.addAll(locList);

			}
		}

		return Response.ok().entity(locations).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/media/{type}")
	public Response findMediaAroundLocationInJSON(
			@PathParam("type") String type, Location loc) {
		List<MediaLite> results = new ArrayList<MediaLite>();
		Integer mediaType = 0;
		if (type.equals("image"))
			mediaType = 0;
		else if (type.equals("video"))
			mediaType = 1;
		else if (type.equals("livestreaming"))
			mediaType = 3;

		// we have a locationId!
		if (loc.getLocationId() != null && loc.getProviderId() != null) {

			// scan recurringrequestmedia and requestmedia tables for locationid

			// the last 3 days
			// get the request Id's for these medias
			// based on the type, search in request media or recurring request
			// media for the requestid's
			// include in the scan the location & provider id
			// get the x/y coordinate of location.. use algorithm to determine
			// if there are other requests that were fulfilled
			// return the list with the exact locationid first, then the fuzzy
			// results around
			long rightNowMinus3Days = (new Date()).getTime()
					- (3L * 24L * 60L * 60L * 1000L); // subtracted 3 days
			long threedays = 3L * 24L * 60L * 60L * 1000L;

			String rightNowMinus3DaysDate = dateFormatter
					.format(rightNowMinus3Days);

			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
			if (mediaType != 3) {
				scanExpression.addFilterCondition(
						"uploadedDate",
						new Condition().withComparisonOperator(
								ComparisonOperator.GT).withAttributeValueList(
								new AttributeValue()
										.withS(rightNowMinus3DaysDate)));
			}
			scanExpression.addFilterCondition(
					"mediaType",
					new Condition().withComparisonOperator(
							ComparisonOperator.EQ).withAttributeValueList(
							new AttributeValue().withN(mediaType.toString())));

			PaginatedScanList<Media> allNonExpiredMedia = mapper().scan(
					Media.class, scanExpression);
			for (Media m : allNonExpiredMedia) {
				if (m.getRequestType().equals("1")) {
					// we have a recurring request type that could be our
					// location!
					RecurringRequestMedia origReq = super.mapper().load(
							RecurringRequestMedia.class, m.getRequestId());

					if (origReq.getProviderId().equals(loc.getProviderId())
							&& origReq.getLocationId().equals(
									loc.getLocationId())) {
						MediaLite media = new MediaLite();
						media.setMediaURL(m.getMediaURL());
						Date expiryDate = new Date();
						expiryDate.setTime(m.getUploadedDate().getTime()
								+ threedays);
						media.setExpiryDate(expiryDate);
						results.add(media);
					}

				} else {
					RequestMedia origReq = super.mapper().load(
							RequestMedia.class, m.getOriginalRequestor(),
							m.getRequestId());

					if (origReq.getProviderId().equals(loc.getProviderId())
							&& origReq.getLocationId().equals(
									loc.getLocationId())) {
						MediaLite media = new MediaLite();
						media.setMediaURL(m.getMediaURL());

						// TODO if the mediaType = 3, we have livestreaming and
						// it never expires..
						Date expiryDate = new Date();

						expiryDate.setTime(m.getUploadedDate().getTime()
								+ threedays);
						media.setExpiryDate(expiryDate);

						results.add(media);
					}
				}
			}
			return Response.ok().entity(results).build();

		} else if (loc.getLatitude() != null && loc.getLongitude() != null
				&& loc.getRadiusInYards() != null) {
			return Response.status(500)
					.entity(new Status("Success", "Not implemented", ""))
					.build();
		} else {
			return Response
					.status(500)
					.entity(new Status(
							"Failure",
							"You must either specify locationId & providerId, or a set of latitude/long coordinates with radius in yards",
							"")).build();
		}

	}

	private List<Location> AssignBookmarks(List<Location> locations,
			String eMailAddress) {
		List<Location> bookmarks = new BookmarkResource()
				.getBookmarks(eMailAddress);

		for (Location loc : locations) {
			for (Location bookmark : bookmarks) {
				if (loc.getProviderId().equals(bookmark.getProviderId())
						&& loc.getLocationId().equals(bookmark.getLocationId())) {
					loc.setBookmark(true);
				}
			}

		}
		return locations;
	}

	private List<Location> PopulateCounts(List<Location> locations) {
		// "monkeys=1,images=3,videos=2,livestreaming=false"

		for (Location loc : locations) {
			String counts = "";
			int monkeys = 0;
			

			// Let's see if there are any users checked in the vicinity
			monkeys = UserCountAtLocation(loc);

			// how about media
			Map<String,Integer> media = MediaCountAtLocation(loc);
			
			loc.setMonkeys(monkeys);
			loc.setImages(media.get("images"));
			loc.setVideos(media.get("videos"));
			loc.setLivestreaming(media.get("livestreaming"));
		}

		return locations;
	}

	public int UserCountAtLocation(Location loc) {
		int count = 0;
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		PaginatedScanList<CheckIn> results = super.mapper().scan(CheckIn.class, scanExpression);
		
		for(CheckIn c : results){
			new Locator();
			if(Locator.isInVicinity(loc.getLatitude(), loc.getLongitude(), c.getLatitude(), c.getLongitude(), 250)){
				count++;
			}
		}
	
		return count;
	}

	public Map<String, Integer> MediaCountAtLocation(Location loc) {
		Map<String, Integer> results = new HashMap<String, Integer>();
		int images = 0;
		int videos = 0;
		int livestreaming = 0;

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

		scanExpression.addFilterCondition(
				"locationId",
				new Condition().withComparisonOperator(ComparisonOperator.EQ)
						.withAttributeValueList(
								new AttributeValue().withS(loc.getLocationId()
										.toString())));

		scanExpression.addFilterCondition(
				"providerId",
				new Condition().withComparisonOperator(ComparisonOperator.EQ)
						.withAttributeValueList(
								new AttributeValue().withS(loc.getProviderId()
										.toString())));
		
		

		PaginatedScanList<RequestMedia> requests = super.mapper().scan(
				RequestMedia.class, scanExpression);

		for (RequestMedia r : requests) {
			if (r.getMediaType() == 1) {
				images++;
			} else if (r.getMediaType() == 2) {
				videos++;
			} else if(r.getMediaType() == 3){
			  livestreaming++;
			}
		}

		results.put("images", images);
		results.put("videos", videos);
		results.put("livestreaming", livestreaming);
		
		return results;
	}
}
