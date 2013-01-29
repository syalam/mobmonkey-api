package com.MobMonkey.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
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
public class MediaResource extends ResourceHelper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6763705967945152547L;
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

			Media m = (Media) super.load(Media.class, requestId, mediaId);

			MediaLite ml = this.convertMediaToMediaLite(m);

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
		} else if (requestType.equals("text")) {
			media.setMediaType(4);

		}

		else {
			return Response
					.status(500)
					.entity(new Status("Error", requestType
							+ " is not a supported endpoint.", "")).build();
		}

		Map<String, String> reqDetails = new HashMap<String, String>();

		reqDetails = getRequestDetails(media);

		media.setOriginalRequestor(reqDetails.get("origRequestor"));
		if (reqDetails.containsKey("Error"))
			return Response.status(500)
					.entity(new Status("Error", reqDetails.get("Error"), ""))
					.build();
		locationId = reqDetails.get("locationId");
		providerId = reqDetails.get("providerId");

		MediaLite ml = convertMediaToMediaLite(media);
		super.storeInCache("m" + locationId.toLowerCase().trim()
				+ providerId.toLowerCase().trim(), 259200, ml);

		super.save(media, media.getRequestId(), media.getMediaId());

		// Send notification to apple device
		NotificationHelper noteHelper = new NotificationHelper();
		String[] deviceIds = noteHelper.getUserDevices(media
				.getOriginalRequestor());

		String message = "Your " + getMediaType(media.getMediaType())
				+ " request at " + reqDetails.get("nameOfLocation")
				+ " has been fulfilled.";

		super.sendAPNS(deviceIds, message);

		super.storeInCache(media.getRequestId() + ":" + media.getMediaId(),
				259200, media);

		// Add to the LocationMedia table for others to retrieve
		if (media.getMediaType() != 4) {
			LocationMedia lm = new LocationMedia();
			lm.setLocationProviderId(locationId + ":" + providerId);
			lm.setUploadedDate(media.getUploadedDate());
			lm.setRequestId(media.getRequestId());
			lm.setMediaId(media.getMediaId());

			super.save(lm, lm.getLocationProviderId(), lm.getUploadedDate()
					.toString());

			// Trending
			Trending t = new Trending();
			t.setType("Media");
			t.setTimeStamp(now);
			t.setLocationId(locationId);
			t.setProviderId(providerId);
			super.mapper().save(t);

		}

		String resp = "";
		if (media.getMediaType() == 4) {
			resp = media.getText();
		} else {
			resp = media.getMediaURL();
		}

		return Response
				.status(201)
				.entity(new Status("Success", "Successfully uploaded "
						+ requestType + ": " + resp, "")).build();

	}

	@POST
	@Path("/testAPNS")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response testAPNSInJSON(@QueryParam("deviceId") String deviceId) {
		String[] deviceIds = new String[1];
		deviceIds[0] = deviceId;
		String message = "This is a test. Sent on: " + (new Date()).toString();

		String result;
		try {
			result = super.sendAPNS(deviceIds, message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			result = "Failure.";
		}

		return Response.ok().entity("Result: " + result).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMediaInJSON(@QueryParam("locationId") String locationId,
			@QueryParam("providerId") String providerId) {

		// Query location media table for date range today - 3 days.
		dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

		long threeHoursAgo = (new Date()).getTime() - (3L * 60L * 60L * 1000L); // three
																				// hours
																				// in
																				// milliseconds

		String threeHoursAgoDate = dateFormatter.format(threeHoursAgo);

		LocationMedia result = new LocationMedia();
		result.setLocationId(locationId);
		result.setProviderId(providerId);
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(locationId + ":" + providerId));
		queryExpression.setRangeKeyCondition(new Condition()
				.withComparisonOperator(ComparisonOperator.GT)
				.withAttributeValueList(
						new AttributeValue().withS(threeHoursAgoDate)));

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
			if (m == null) {
				// Media was removed, remove from LocMedia
				super.mapper().delete(locMedia);
			} else {
				MediaLite ml = this.convertMediaToMediaLite(m);
				media.add(ml);
			}

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
		if (typeId == 4)
			return "text";
		else
			return "unknown";
	}

	private Date getExpiryDate(long uploadDate) {
		long threehours = 3L * 60L * 60L * 1000L;
		Date expiryDate = new Date();
		expiryDate.setTime(uploadDate + threehours);
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
			url = "https://s3.amazonaws.com/" + bucket + "/" + mediaFileName;

		}

		return url;

	}

	private Map<String, String> getRequestDetails(Media m) {
		String origRequestor = m.getOriginalRequestor();
		Map<String, String> results = new HashMap<String, String>();
		String locationId = "";
		String providerId = "";
		String nameOfLocation = "";

		AssignedRequest assReq = new AssignedRequest();
		if (m.getMediaType() != 3) {
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
				results.put("Error",
						"Request does not exist, removing assignment");
				super.mapper().delete(assReq);
				return results;
			}
			rrm.setRequestId(m.getRequestId());
			rrm.setRequestFulfilled(true);
			rrm.setFulfilledDate(m.getUploadedDate());
			locationId = rrm.getLocationId();
			providerId = rrm.getProviderId();
			nameOfLocation = rrm.getNameOfLocation();

			super.mapper().save(rrm);

			// Update the cache
			Object o = super.getFromCache("RecurringRequestTable");

			if (o != null) {
				@SuppressWarnings("unchecked")
				List<RecurringRequestMedia> tmp = (List<RecurringRequestMedia>) o;

				tmp.add(rrm);
				super.storeInCache("RecurringRequestTable", 259200, tmp);
			}

		} else if (m.getRequestType().equals("0")) {
			RequestMedia rm = super.mapper().load(RequestMedia.class,
					origRequestor, m.getRequestId());
			if (rm == null) {
				if (m.getMediaType() != 3) {
					results.put("Error",
							"Request is no longer assigned to user");
					super.mapper().delete(assReq);
					return results;
				}
			}

			rm.setRequestFulfilled(true);
			rm.setFulfilledDate(m.getUploadedDate());
			locationId = rm.getLocationId();
			providerId = rm.getProviderId();
			nameOfLocation = rm.getNameOfLocation();
			rm.setRequestId(m.getRequestId());
			super.mapper().save(rm);
			// Update the cache
			Object o = super.getFromCache("RequestTable");

			if (o != null) {
				@SuppressWarnings("unchecked")
				List<RequestMedia> tmp = (List<RequestMedia>) o;

				tmp.add(rm);
				super.storeInCache("RequestTable", 259200, tmp);
			}
		}

		try {
			super.mapper().delete(assReq);
		} catch (Exception exc) {

		}

		results.put("origRequestor", origRequestor);
		results.put("locationId", locationId);
		results.put("providerId", providerId);
		results.put("nameOfLocation", nameOfLocation);
		return results;
	}

	public MediaLite convertMediaToMediaLite(Media m) {
		MediaLite ml = new MediaLite();
		ml.setMediaURL(m.getMediaURL());
		ml.setRequestId(m.getRequestId());
		ml.setMediaId(m.getMediaId());
		ml.setAccepted(m.isAccepted());
		ml.setExpiryDate(getExpiryDate(m.getUploadedDate().getTime()));
		ml.setType(getMediaType(m.getMediaType()));
		ml.setUploadedDate(m.getUploadedDate());
		ml.setContentType(m.getContentType());
		ml.setText(m.getText());

		if (m.getMediaType() == 3) {
			ml.setExpiryDate(null);
		}

		return ml;
	}

	@POST
	@Path("/flaginappropriate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response flagMediaInappropriateInJSON(
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

}
