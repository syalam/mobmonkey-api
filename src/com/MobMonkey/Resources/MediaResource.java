package com.MobMonkey.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Map.Entry;

import javax.xml.bind.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Helpers.ApplePNSHelper;
import com.MobMonkey.Helpers.NotificationHelper;
import com.MobMonkey.Models.AssignedRequest;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.LocationMedia;
import com.MobMonkey.Models.Media;
import com.MobMonkey.Models.MediaLite;
import com.MobMonkey.Models.RecurringRequestMedia;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.RequestMediaLite;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.Trending;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Path("/media")
public class MediaResource extends ResourceHelper {
	static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public MediaResource() {
		super();
	}

	@GET
	@Path("/request")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getImageRequestInJSON(
			@QueryParam("requestId") String requestId,
			@QueryParam("mediaId") String mediaId) {

		try {
			Media m = null;
			Object o = super.getFromCache(requestId + ":" + mediaId);
			if (o != null) {
				m = (Media) o;
			} else {
				m = super.mapper().load(Media.class, requestId, mediaId);
				super.storeInCache(requestId + ":" + mediaId, 259200, o);
			}
			MediaLite ml = new MediaLite();
			ml.setContentType(m.getContentType());
			ml.setExpiryDate(getExpiryDate(m.getUploadedDate().getTime()));
			ml.setMediaId(m.getMediaId());
			ml.setMediaURL(m.getMediaURL());
			ml.setRequestId(m.getRequestId());
			ml.setType(getMediaType(m.getMediaType()));

			return Response.ok().entity(ml).build();
		} catch (Exception exc) {
			return Response
					.status(500)
					.entity(new Status(
							"Error",
							"There was a problem loading this media from the DB",
							"")).build();
		}

	}

	@DELETE
	@Path("/request")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response rejectImageRequestInJSON(
			@QueryParam("requestId") String requestId,
			@QueryParam("mediaId") String mediaId, @Context HttpHeaders headers) {
		String username = headers.getRequestHeader("MobMonkey-user").get(0);
		Media m = new Media();

		try {
			m = super.mapper().load(Media.class, requestId, mediaId);
			if (!m.getOriginalRequestor().toLowerCase()
					.equals(username.toLowerCase())) {
				return Response
						.status(500)
						.entity(new Status(
								"Error",
								"The requestId provided is not associated with your username",
								"")).build();
			}
		} catch (Exception exc) {
			return Response
					.status(500)
					.entity(new Status(
							"Error",
							"There was a problem loading this media from the DB",
							"")).build();
		}
		if (m.getRequestType().equals("0")) {
			try {
				RequestMedia rm = super.mapper().load(RequestMedia.class,
						username, requestId);
				if (rm != null) {
					super.mapper().delete(m);
					super.deleteFromCache(m.getRequestId() + ":"
							+ m.getMediaId());
					LocationMedia lm = super.mapper().load(LocationMedia.class,
							rm.getLocationId() + ":" + rm.getProviderId(),
							m.getUploadedDate());
					super.mapper().delete(lm);
					rm.setFulfilledDate(null);
					rm.setRequestFulfilled(false);
					rm.setScheduleDate(new Date());
					super.mapper().save(rm);
					return Response
							.ok()
							.entity(new Status("Success",
									"Successfully rejected media", "")).build();
				}
			} catch (Exception exc) {
				return Response
						.status(500)
						.entity(new Status(
								"Error",
								"The requestId provided is not associated with your username",
								"")).build();
			}
		} else {
			// super.ampper().load(RecurringRequstMedia)
			// TODO implement recurring
			return Response
					.ok()
					.entity(new Status("NEED TO IMPLEMENT",
							"NEED TO IMPLEMENT", "")).build();

		}
		return Response.ok().build();

	}

	@POST
	@Path("/request")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response acceptImageRequestInJSON(
			@QueryParam("requestId") String requestId,
			@QueryParam("mediaId") String mediaId, @Context HttpHeaders headers) {
		String username = headers.getRequestHeader("MobMonkey-user").get(0);
		Media m = new Media();

		try {
			m = super.mapper().load(Media.class, requestId, mediaId);
			if (!m.getOriginalRequestor().toLowerCase()
					.equals(username.toLowerCase())) {
				return Response
						.status(500)
						.entity(new Status(
								"Error",
								"The requestId provided is not associated with your username",
								"")).build();
			}
		} catch (Exception exc) {
			return Response
					.status(500)
					.entity(new Status(
							"Error",
							"There was a problem loading this media from the DB",
							"")).build();
		}
		if (m.getRequestType().equals("0")) {
			try {

				m.setAccepted(true);
				super.mapper().save(m);
				super.storeInCache(requestId + ":" + mediaId, 259200, m);

				return Response
						.ok()
						.entity(new Status("Success",
								"Successfully accepted media", "")).build();

			} catch (Exception exc) {
				return Response
						.status(500)
						.entity(new Status(
								"Error",
								"The requestId provided is not associated with your username",
								"")).build();
			}
		} else {
			// super.ampper().load(RecurringRequstMedia)
			// TODO implement recurring
			return Response
					.ok()
					.entity(new Status("NEED TO IMPLEMENT",
							"NEED TO IMPLEMENT", "")).build();

		}

	}

	@POST
	@Path("/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadImageInJSON(@PathParam("type") String requestType,
			Media media, @Context HttpHeaders headers) throws IOException {

		Date now = new Date();
		String username = headers.getRequestHeader("MobMonkey-user").get(0);
		String originalRequestor = username;
		String locationId = "";
		String providerId = "";
		media.setMediaId(UUID.randomUUID().toString());
		String mediaURL = "";

		media.seteMailAddress(username);
		media.setUploadedDate(now);
		media.setOriginalRequestor(originalRequestor); // for livestreaming
		media.setAccepted(false);
		Map<String, String> reqDetails = getRequestDetails(media);
		if (reqDetails.containsKey("Error"))
			return Response.status(500)
					.entity(new Status("Error", reqDetails.get("Error"), ""))
					.build();
		if (media.getMediaType() != 3) // livestreaming does not follow the same
										// rules.
			media.setOriginalRequestor(reqDetails.get("origRequestor"));
		locationId = reqDetails.get("locationId");
		providerId = reqDetails.get("providerId");

		if (requestType.equals("image")) {
			media.setMediaType(1);
			mediaURL = uploadToAmazonS3(media);
			if (!mediaURL.startsWith("Error")) {
				media.setMediaURL(mediaURL);
			} else {
				return Response
						.status(500)
						.entity(new Status("Error", mediaURL.split(":")[1], ""))
						.build();
			}

		} else if (requestType.equals("video")) {
			media.setMediaType(2);
			mediaURL = uploadToAmazonS3(media);
			if (!mediaURL.startsWith("Error")) {
				media.setMediaURL(mediaURL);
			} else {
				return Response
						.status(500)
						.entity(new Status("Error", mediaURL.split(":")[1], ""))
						.build();
			}
		} else if (requestType.equals("livestreaming")) {
			media.setMediaType(3);
			media.setRequestType("0");
		}else if(requestType.equals("text")){
			
	
		}
		
		else {
			return Response
					.status(500)
					.entity(new Status("Error", requestType
							+ " is not a supported endpoint.", "")).build();
		}

		super.mapper().save(media);

		super.storeInCache(media.getRequestId() + ":" + media.getMediaId(),
				259200, media);

		// Add to the LocationMedia table for others to retrieve
		LocationMedia lm = new LocationMedia();
		lm.setLocationProviderId(locationId + ":" + providerId);
		lm.setUploadedDate(media.getUploadedDate());
		lm.setRequestId(media.getRequestId());
		lm.setMediaId(media.getMediaId());

		super.mapper().save(lm);

		// Trending
		Trending t = new Trending();
		t.setType("Media");
		t.setTimeStamp(now);
		t.setLocationId(locationId);
		t.setProviderId(providerId);
		super.mapper().save(t);

		// Send notification to apple device

		NotificationHelper noteHelper = new NotificationHelper();
		String[] deviceIds = noteHelper.getUserDevices(originalRequestor);
		ApplePNSHelper.send(deviceIds, media.getMediaURL());

		return Response
				.status(201)
				.entity(new Status("Success", "Successfully uploaded "
						+ requestType + ": " + media.getMediaURL(), ""))
				.build();

	}
	
	@POST 
	@Path("/testAPNS")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response testAPNSInJSON(@QueryParam("deviceId") String deviceId){
		String[] deviceIds = new String[1];
		deviceIds[0] = deviceId;
		String result = ApplePNSHelper.testSend(deviceIds, "This is a test. Sent on: " + (new Date()).toString());
		return Response.ok().entity("Result: " + result).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMediaInJSON(@QueryParam("locationId") String locationId,
			@QueryParam("providerId") String providerId) {

		// Query location media table for date range today - 3 days.
		dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

		long threeDaysAgo = (new Date()).getTime()
				- (3L * 24L * 60L * 60L * 1000L); // three days in milliseconds

		String threeDaysAgoDate = dateFormatter.format(threeDaysAgo);

		LocationMedia result = new LocationMedia();
		result.setLocationId(locationId);
		result.setProviderId(providerId);
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(locationId + ":" + providerId));
		queryExpression.setRangeKeyCondition(new Condition()
				.withComparisonOperator(ComparisonOperator.GT)
				.withAttributeValueList(
						new AttributeValue().withS(threeDaysAgoDate)));

		PaginatedQueryList<LocationMedia> results = super.mapper().query(
				LocationMedia.class, queryExpression);

		HashMap<LocationMedia, Long> unsorted = new HashMap<LocationMedia, Long>();
		for (LocationMedia lm : results) {
			unsorted.put(lm, lm.getUploadedDate().getTime());
		}

		List<MediaLite> media = new ArrayList<MediaLite>();
		for (Entry<LocationMedia, Long> entry : entriesSortedByValues(unsorted)) {
			LocationMedia locMedia = entry.getKey();
			Media m = super.mapper().load(Media.class, locMedia.getRequestId(),
					locMedia.getMediaId());
			MediaLite ml = new MediaLite();
			ml.setMediaURL(m.getMediaURL());
			ml.setRequestId(locMedia.getRequestId());
			ml.setMediaId(locMedia.getMediaId());
			ml.setAccepted(m.isAccepted());
			ml.setExpiryDate(getExpiryDate(m.getUploadedDate().getTime()));
			ml.setType(getMediaType(m.getMediaType()));
			if (m.getMediaType() == 3) {
				ml.setExpiryDate(null);
			}

			media.add(ml);
		}

		result.setMedia(media);

		return Response.ok().entity(result).build();
	}

	private String getMediaType(int typeId) {
		if (typeId == 1)
			return "image";
		if (typeId == 2)
			return "video";
		if (typeId == 3)
			return "livestreaming";
		else
			return "unknown";
	}

	private Date getExpiryDate(long uploadDate) {
		long threedays = 3L * 24L * 60L * 60L * 1000L;
		Date expiryDate = new Date();
		expiryDate.setTime(uploadDate + threedays);
		return expiryDate;
	}

	private String uploadToAmazonS3(Media m) {
		String prefix = "";
		String ext = "";
		String bucket = "";
		if (m.getMediaType() == 1) {
			prefix = "img-";
			bucket = "mobmonkeyimages";
		}
		if (m.getMediaType() == 2) {
			prefix = "vod-";
			bucket = "mobmonkeyvod";
		}
		try {
			if (m.getContentType().toLowerCase().equals("image/jpg")
					|| m.getContentType().toLowerCase().equals("image/jpeg"))
				ext = ".jpg";
			else if (m.getContentType().toLowerCase().equals("image/png"))
				ext = ".png";
			else if (m.getContentType().toLowerCase().equals("image/gif"))
				ext = ".gif";
			else if (m.getContentType().toLowerCase().equals("video/mp4"))
				ext = ".mp4";
			else if (m.getContentType().toLowerCase().equals("video/mpeg"))
				ext = ".mpeg";
			else if (m.getContentType().toLowerCase().equals("video/quicktime"))
				ext = ".mov";
			else
				return "Error:Unsupported content type provided: "
						+ m.getContentType();
		} catch (Exception exc) {
			return "Error:Missing the ContentType attribute";
		}
		String mediaFileName = prefix + m.getMediaId() + ext;

		byte[] btDataFile = DatatypeConverter.parseBase64Binary(m
				.getMediaData());
		ByteArrayInputStream bais = new ByteArrayInputStream(btDataFile);
		ObjectMetadata objmeta = new ObjectMetadata();
		objmeta.setContentLength(btDataFile.length);
		objmeta.setContentType(m.getContentType());
		objmeta.setExpirationTimeRuleId("images");
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucket,
				mediaFileName, bais, objmeta);
		putObjectRequest.setRequestCredentials(super.credentials());
		putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
		super.s3cli().putObject(putObjectRequest);
		String url = "";
	

		if (m.getMediaType() == 1) {
			url = "https://s3-us-west-1.amazonaws.com/" + bucket + "/"
					+ mediaFileName;
		} else if (m.getMediaType() == 2) {
			url = "https://s3.amazonaws.com/" + bucket + "/"
					+ mediaFileName;

		}

		return url;

	}

	private Map<String, String> getRequestDetails(Media m) {
		String origRequestor = "";
		Map<String, String> results = new HashMap<String, String>();
		String locationId = "";
		String providerId = "";

		AssignedRequest assReq = new AssignedRequest();
		if (!m.getRequestType().equals("3")) {
			assReq = super.mapper().load(AssignedRequest.class,
					m.geteMailAddress(), m.getRequestId());
			if (assReq == null) {
				results.put("Error", "Request is no longer assigned to user");
				return results;
			}
			origRequestor = assReq.getRequestorEmail();
		}

		if (m.getRequestType().equals("1")) {
			RecurringRequestMedia rrm = super.mapper().load(
					RecurringRequestMedia.class, origRequestor,
					m.getRequestId());
			if (rrm.equals(null)) {
				results.put("Error", "Request is no longer assigned to user");
				super.mapper().delete(assReq);
				return results;
			}
			rrm.setRequestId(m.getRequestId());
			rrm.setRequestFulfilled(true);
			rrm.setFulfilledDate(m.getUploadedDate());
			// TODO rrm.setMediaUrl
			locationId = rrm.getLocationId();
			providerId = rrm.getProviderId();
			super.mapper().save(rrm);
		} else if (m.getRequestType().equals("0")) {
			RequestMedia rm = super.mapper().load(RequestMedia.class,
					origRequestor, m.getRequestId());
			if (rm == null) {
				if (!m.getRequestType().equals("3"))
					results.put("Error",
							"Request is no longer assigned to user");
				super.mapper().delete(assReq);
				return results;
			}
			rm.setRequestFulfilled(true);
			rm.setFulfilledDate(m.getUploadedDate());
			rm.setMediaUrl(m.getMediaURL());
			locationId = rm.getLocationId();
			providerId = rm.getProviderId();
			super.mapper().save(rm);
		}

		super.mapper().delete(assReq);

		results.put("origRequestor", origRequestor);
		results.put("locationId", locationId);
		results.put("providerId", providerId);
		return results;
	}

	
}
