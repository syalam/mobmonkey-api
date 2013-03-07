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
import com.MobMonkey.Models.Statistics;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.Trending;
import com.MobMonkey.Models.User;
import com.MobMonkey.Helpers.SimpleWorkFlow.GcmSWF.*;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.amazonaws.services.simpleworkflow.flow.DataConverter;
import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;

@Path("/media")
public class MediaResource extends ResourceHelper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6763705967945152547L;
	private static final int IMAGE = 1;
	private static final int VIDEO = 2;
	private static final int LIVESTREAMING = 3;
	private static final int TEXT = 4;
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
			m = (Media) super.load(Media.class, requestId, mediaId);
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
				RequestMedia rm = (RequestMedia) super.load(RequestMedia.class,
						username, requestId);
				if (rm != null) {
					super.delete(m, m.getRequestId(), m.getMediaId());
					LocationMedia lm = (LocationMedia) super.load(
							LocationMedia.class,
							rm.getLocationId() + ":" + rm.getProviderId(),
							m.getUploadedDate());
					try {
						super.delete(lm,
								rm.getLocationId() + ":" + rm.getProviderId(),
								m.getUploadedDate().toString());
					} catch (Exception exc) {
						// TODO: well we could find the locationMedia in the
						// database.. need to log this
					}
					rm.setFulfilledDate(null);
					rm.setRequestFulfilled(false);
					rm.setScheduleDate(new Date());
					super.save(rm, rm.geteMailAddress(), rm.getRequestId());
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
			m = (Media) super.load(Media.class, requestId, mediaId);
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
				super.save(m, requestId, mediaId);

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
		String resp = "";
				
		if (requestType.toLowerCase().equals("image")) {
			media.setMediaType(IMAGE);
		} else if (requestType.toLowerCase().equals("video")) {
			media.setMediaType(VIDEO);
		} else if (requestType.toLowerCase().equals("livestreaming")) {
			media.setMediaType(LIVESTREAMING);
			media.setRequestType("0");
		} else if (requestType.toLowerCase().equals("text")) {
			media.setMediaType(TEXT);
			resp = media.getText();
		}
		else {
			return Response
					.status(500)
					.entity(new Status("Error", requestType
							+ " is not a supported endpoint.", "")).build();
		}

		media.setMediaId(UUID.randomUUID().toString());
		media.seteMailAddress(username);
		media.setOriginalRequestor(originalRequestor); // for livestreaming
		
		Map<String, String> reqDetails = new HashMap<String, String>();

		if (requestType.toLowerCase().equals("image")
				|| requestType.toLowerCase().equals("video")) {
			reqDetails = getRequestDetails(media);
			if (reqDetails.containsKey("Error")) {
				return Response
						.status(500)
						.entity(new Status("Error", reqDetails.get("Error"), ""))
						.build();
			}
			media.setMediaURL(reqDetails.get("url"));
			media.setOriginalRequestor(reqDetails.get("origRequestor"));
		}
						
		media.setUploadedDate(now);
		media.setAccepted(false);
	
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

		super.clearCountCache(media.getOriginalRequestor());
		HashMap<String, Integer> counts = new InboxResource().getCounts(media
				.getOriginalRequestor());

		int badgeCount = counts.get("fulfilledUnreadCount")
				+ counts.get("assignedUnreadRequests");

		// Send notification to apple device
		NotificationHelper noteHelper = new NotificationHelper();
		Device[] deviceIds = noteHelper.getUserDevices(media
				.getOriginalRequestor());

		String message = "Your " + getMediaType(media.getMediaType())
				+ " request at " + reqDetails.get("nameOfLocation")
				+ " has been fulfilled.";

		super.sendNotification(deviceIds, message, badgeCount);

		super.storeInCache(media.getRequestId() + ":" + media.getMediaId(),
				259200, media);

		// Add to the LocationMedia table for others to retrieve
		if (media.getMediaType() != TEXT) {
			resp = media.getMediaURL();
			LocationMedia lm = new LocationMedia();
			lm.setLocationProviderId(locationId + ":" + providerId);
			lm.setUploadedDate(media.getUploadedDate());
			lm.setRequestId(media.getRequestId());
			lm.setMediaId(media.getMediaId());

			super.save(lm, lm.getLocationProviderId(), lm.getUploadedDate());

			// Trending
			Trending t = new Trending();
			t.setType("Media");
			t.setTimeStamp(now);
			t.setLocationId(locationId);
			t.setProviderId(providerId);
			super.save(t, t.getType(), t.getTimeStamp().toString());

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

		String result = "success.";

		try {
			super.sendAPNS(deviceIds, message, 69);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
		}

		return Response.ok().entity("Result: " + result).build();
	}

	@POST
	@Path("/testGCM")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response testGCMInJSON(@QueryParam("deviceId") String deviceId)
			throws SecurityException, InstantiationException,
			IllegalAccessException, NoSuchMethodException {

		return Response.ok().entity("Result: ").build();
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
			Media m = (Media) super.load(Media.class, locMedia.getRequestId(),
					locMedia.getMediaId());
			if (m == null) {
				// Media was removed, remove from LocMedia
				super.delete(locMedia, locMedia.getLocationProviderId(),
						locMedia.getUploadedDate().toString());
			} else {
				MediaLite ml = this.convertMediaToMediaLite(m);
				media.add(ml);
			}

		}

		result.setMedia(media);

		return Response.ok().entity(result).build();
	}

	private String getMediaType(int typeId) {
		if (typeId == IMAGE)
			return "image";
		if (typeId == VIDEO)
			return "video";
		if (typeId == LIVESTREAMING)
			return "livestreaming";
		if (typeId == TEXT)
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
		if (m.getMediaType() == IMAGE) {
			prefix = "img-";
			bucket = "mobmonkeyimages";
		}
		if (m.getMediaType() == VIDEO) {
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

		if (m.getMediaType() == IMAGE) {
			url = "https://s3-us-west-1.amazonaws.com/" + bucket + "/"
					+ mediaFileName;

		} else if (m.getMediaType() == VIDEO) {
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
		String partnerId = "";
		String url = "";

		int countOfMediaUploaded = 0;

		AssignedRequest assReq = new AssignedRequest();
		if (m.getMediaType() != LIVESTREAMING) {
			assReq = (AssignedRequest) super.load(AssignedRequest.class,
					m.geteMailAddress(), m.getRequestId());
			if (assReq == null) {
				results.put("Error", "Request is no longer assigned to user");
				return results;
			}
			origRequestor = assReq.getRequestorEmail();
		}

		if (m.getRequestType().equals("1")) {
			RecurringRequestMedia rrm = (RecurringRequestMedia) super.load(
					RecurringRequestMedia.class, origRequestor,
					m.getRequestId());
			if (rrm.equals(null)) {
				results.put("Error",
						"Request does not exist, removing assignment");
				super.delete(assReq, m.geteMailAddress(), m.getRequestId());
				return results;
			}

			if (rrm.getCountOfMediaUploaded() >= 9) {
				results.put("Error",
						"The maximum number of media have been uploaded for this request");
				return results;
			}

			url = uploadToAmazonS3(m);
			if (url.startsWith("Error")) {
				results.put("Error", url.split(":")[1]);
				return results;
			}
			rrm.setRequestId(m.getRequestId());
			rrm.setRequestFulfilled(true);
			rrm.setFulfilledDate(new Date());
			partnerId = rrm.getPartnerId();
			locationId = rrm.getLocationId();
			providerId = rrm.getProviderId();
			nameOfLocation = rrm.getNameOfLocation();
			countOfMediaUploaded = rrm.getCountOfMediaUploaded();
			rrm.setCountOfMediaUploaded(countOfMediaUploaded + 1);
			super.save(rrm, rrm.geteMailAddress(), rrm.getRequestId());

			// Update the cache

		} else if (m.getRequestType().equals("0")) {
			RequestMedia rm = (RequestMedia) super.load(RequestMedia.class,
					origRequestor, m.getRequestId());
			if (rm == null) {
				if (m.getMediaType() != LIVESTREAMING) {
					results.put("Error",
							"Request is no longer assigned to user");
					super.delete(assReq, m.geteMailAddress(), m.getRequestId());
					return results;
				}
			}

			if (rm.getCountOfMediaUploaded() >= 9) {
				results.put("Error",
						"The maximum number of media have been uploaded for this request");
				return results;
			}

			url = uploadToAmazonS3(m);
			if (url.startsWith("Error")) {
				results.put("Error", url.split(":")[1]);
				return results;
			}

			rm.setRequestFulfilled(true);
			rm.setFulfilledDate(new Date());
			partnerId = rm.getPartnerId();
			locationId = rm.getLocationId();
			providerId = rm.getProviderId();
			nameOfLocation = rm.getNameOfLocation();
			countOfMediaUploaded = rm.getCountOfMediaUploaded();
			rm.setCountOfMediaUploaded(countOfMediaUploaded + 1);
			rm.setRequestId(m.getRequestId());
			super.save(rm, rm.geteMailAddress(), rm.getRequestId());
		}

		try {
			super.delete(assReq, assReq.geteMailAddress(),
					assReq.getRequestId());
		} catch (Exception exc) {

		}

		if (countOfMediaUploaded < 3) {
			User user = (User) super.load(User.class, m.geteMailAddress(),
					partnerId);
			switch (countOfMediaUploaded) {
			case 0:
				user.setRank(user.getRank() + 3);
				Statistics stats = new Statistics();
				stats.setStatisticId("1ST2RESP:" + m.geteMailAddress() + ":"
						+ partnerId);
				stats.setDate(new Date());
				super.save(stats, stats.getStatisticId(), stats.getDate());
				break;
			case 1:
				user.setRank(user.getRank() + 2);
				break;
			case 2:
				user.setRank(user.getRank() + 1);
				break;
			}
			super.save(user, m.geteMailAddress(), partnerId);
		}

		results.put("origRequestor", origRequestor);
		results.put("locationId", locationId);
		results.put("providerId", providerId);
		results.put("nameOfLocation", nameOfLocation);
		results.put("url", url);
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

		if (m.getMediaType() == LIVESTREAMING) {
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
			m = (Media) super.load(Media.class, requestId, mediaId);
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
				super.save(m, m.getRequestId(), m.getMediaId());

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
