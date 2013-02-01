package com.MobMonkey.Resources;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Models.AssignedRequest;
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

@Path("/inbox")
public class InboxResource extends ResourceHelper {

	public InboxResource() {
		super();
	}

	// Open, answered and requests
	@GET
	@Path("/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInboxInJSON(@PathParam("type") String type,
			@Context HttpHeaders headers) {
		// TODO answered requests

		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0)
				.toLowerCase();

		super.clearCountCache(eMailAddress);
		if (type.toLowerCase().equals("assignedrequests")) {
			List<AssignedRequest> results = new ArrayList<AssignedRequest>();
			results = getAssignedRequests(eMailAddress);

			return Response.ok().entity(results).build();
		} else {
			List<RequestMedia> results = new ArrayList<RequestMedia>();

			results = getRequests(type, eMailAddress);
			return Response.ok().entity(results).build();
		}

	}

	@GET
	@Path("/counts")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCountsInJSON(@Context HttpHeaders headers) {
		// TODO answered requests

		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0)
				.toLowerCase();
		Map<String, Integer> results = this.getCounts(eMailAddress);

		return Response.ok().entity(results).build();

	}

	public List<RequestMedia> getRequests(String type, String eMailAddress) {
		List<RequestMedia> results = new ArrayList<RequestMedia>();
		// long threeDaysAgo = (new Date()).getTime()
		// - (3L * 24L * 60L * 60L * 1000L);
		long threeHoursAgo = (new Date()).getTime() - (3L * 60L * 60L * 1000L);

		if (type.toLowerCase().equals("openrequests")) {
			// TODO add recurring requests to this list
			// ALSO ADD FILTERING!!!!!!!!!!!!

			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			PaginatedQueryList<RequestMedia> openRequests = super.mapper()
					.query(RequestMedia.class, queryExpression);

			
			DynamoDBQueryExpression queryExpression2 = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			PaginatedQueryList<RecurringRequestMedia> openRecRequests = super
					.mapper().query(RecurringRequestMedia.class,
							queryExpression2);

			HashMap<RequestMedia, Long> unsorted = new HashMap<RequestMedia, Long>();
			for (RequestMedia rm : openRequests) {
				unsorted.put(rm, rm.getScheduleDate().getTime());

			}

			RequestMediaResource rmr = new RequestMediaResource();
			for (RecurringRequestMedia rrm : openRecRequests) {
				RequestMedia rm = rmr.convertToRM(rrm);
				unsorted.put(rm, rm.getScheduleDate().getTime());

			}

			for (Entry<RequestMedia, Long> entry : entriesSortedByValues(unsorted)) {
				// check to see if request is fulfilled
				RequestMedia rm = entry.getKey();
				if (!rm.isRequestFulfilled()) {

					if (rm.isRecurring()) {
						results.add(rm);
					} else {
						Date now = new Date();
						Date expiryDate = new Date();
						int duration = rm.getDuration(); // in minutes
						expiryDate.setTime(rm.getScheduleDate().getTime()
								+ duration * 60000);
						if (now.getTime() > expiryDate.getTime()) {
							rm.setExpired(true);
							super.delete(rm, rm.geteMailAddress(),
									rm.getRequestId());
							// results.add(rm);
						} else {
							rm.setExpired(false);
							results.add(rm);
						}
					}
				}
			}

		}
		if (type.toLowerCase().equals("assignedrequests")) {
			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));
			PaginatedQueryList<AssignedRequest> assignedToMe = super.mapper()
					.query(AssignedRequest.class, queryExpression);

			HashMap<AssignedRequest, Long> unsorted = new HashMap<AssignedRequest, Long>();
			for (AssignedRequest rm : assignedToMe) {
				unsorted.put(rm, rm.getAssignedDate().getTime());

			}

			List<AssignedRequest> requestsToSave = new ArrayList<AssignedRequest>();
			for (Entry<AssignedRequest, Long> entry : entriesSortedByValues(unsorted)) {
				AssignedRequest assReq = entry.getKey();
				switch (assReq.getRequestType()) {
				case 0:
					RequestMedia rm = (RequestMedia) super.load(RequestMedia.class,
							assReq.getRequestorEmail(), assReq.getRequestId());
					if (rm.getRequestDate().getTime() > threeHoursAgo) {
						if (assReq.isMarkAsRead())
							rm.setMarkAsRead(true);
						else
							rm.setMarkAsRead(false);
						results.add(rm);

						try {
							AssignedRequest newReq = (AssignedRequest) assReq
									.clone();
							newReq.setMarkAsRead(true);
							requestsToSave.add(newReq);

						} catch (CloneNotSupportedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				case 1:
					RequestMediaResource rmr = new RequestMediaResource();
					RecurringRequestMedia rrm = (RecurringRequestMedia) super.load(
							RecurringRequestMedia.class,
							assReq.getRequestorEmail());
					RequestMedia tmp = rmr.convertToRM(rrm);
					if (assReq.isMarkAsRead())
						tmp.setMarkAsRead(true);
					else
						tmp.setMarkAsRead(false);
					results.add(tmp);

					try {
						AssignedRequest newReq = (AssignedRequest) assReq
								.clone();
						newReq.setMarkAsRead(true);
						requestsToSave.add(newReq);

					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				}

			}
			super.mapper().batchSave(requestsToSave);
		}
		if (type.toLowerCase().equals("fulfilledrequests")) {
			// TODO Caching

			// Regular requests
			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			PaginatedQueryList<RequestMedia> openRequests = super.mapper()
					.query(RequestMedia.class, queryExpression);

			// Recurring requests
			DynamoDBQueryExpression queryExpression2 = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			PaginatedQueryList<RecurringRequestMedia> openRecRequests = super
					.mapper().query(RecurringRequestMedia.class,
							queryExpression2);

			List<RequestMedia> requestsToSave = new ArrayList<RequestMedia>();
			HashMap<RequestMedia, Long> unsorted = new HashMap<RequestMedia, Long>();
			for (RequestMedia rm : openRequests) {
				if (rm.isRequestFulfilled()
						&& rm.getFulfilledDate().getTime() > threeHoursAgo) {
					unsorted.put(rm, rm.getScheduleDate().getTime());
					try {
						RequestMedia tmp = (RequestMedia) rm.clone();
						tmp.setMarkAsRead(true);
						requestsToSave.add(tmp);
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			super.mapper().batchSave(requestsToSave);

			List<RecurringRequestMedia> recurringRequestsToSave = new ArrayList<RecurringRequestMedia>();
			RequestMediaResource rmr = new RequestMediaResource();
			for (RecurringRequestMedia rrm : openRecRequests) {
				RequestMedia rm = rmr.convertToRM(rrm);
				unsorted.put(rm, rm.getScheduleDate().getTime());
				try {
					RecurringRequestMedia tmp = (RecurringRequestMedia) rrm
							.clone();
					tmp.setMarkAsRead(true);
					recurringRequestsToSave.add(tmp);
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			super.mapper().batchSave(recurringRequestsToSave);

			for (Entry<RequestMedia, Long> entry : entriesSortedByValues(unsorted)) {
				// check to see if request is fulfilled
				RequestMedia rm = entry.getKey();

				/*
				 * if (rm.getProviderId() != null && rm.getLocationId() != null)
				 * { rm.setNameOfLocation(new Locator().reverseLookUp(
				 * rm.getProviderId(), rm.getLocationId()).getName()); }
				 */

				if (rm.isRequestFulfilled()) {

					DynamoDBQueryExpression mediaQuery = new DynamoDBQueryExpression(
							new AttributeValue().withS(rm.getRequestId()));
					PaginatedQueryList<Media> media = super.mapper().query(
							Media.class, mediaQuery);

					List<MediaLite> mediaL = new ArrayList<MediaLite>();
					for (Media m : media) {

						MediaLite ml = new MediaLite();
						ml.setMediaURL(m.getMediaURL());
						ml.setRequestId(m.getRequestId());
						ml.setMediaId(m.getMediaId());
						ml.setAccepted(m.isAccepted());
						ml.setExpiryDate(getExpiryDate(m.getUploadedDate()
								.getTime()));
						ml.setType(getMediaType(m.getMediaType()));
						ml.setText(m.getText());
						if (m.getMediaType() == 3) {
							ml.setExpiryDate(null);
							mediaL.add(ml);
						} else {

							if (m.getUploadedDate().getTime() >= threeHoursAgo) {
								mediaL.add(ml);
							}
						}
					}

					// Add media list to results
					rm.setMedia(mediaL);
					results.add(rm);

				}
			}
		}
		return results;
	}

	
	public HashMap<String, Integer> getCounts(String eMailAddress) {

		HashMap<String, Integer> results = new HashMap<String, Integer>();
		HashMap<RequestMedia, Long> unsorted = new HashMap<RequestMedia, Long>();
		long threeHoursAgo = (new Date()).getTime() - (3L * 60L * 60L * 1000L);
		int openCount = 0;
		int fulfilledReadCount = 0;
		int fulfilledUnreadCount = 0;
		int assignedReadCount = 0;
		int assignedUnreadCount = 0;

		Object oCount = super.getFromCache("OPENCOUNT:" + eMailAddress);
		Object fUCount = super.getFromCache("FULFILLEDUNREADCOUNT:"
				+ eMailAddress);
		Object fRCount = super.getFromCache("FULFILLEDREADCOUNT:"
				+ eMailAddress);
		Object aRCount = super
				.getFromCache("ASSIGNEDREADCOUNT:" + eMailAddress);
		Object aUCount = super
				.getFromCache("ASSIGNEDREADCOUNT:" + eMailAddress);
		if (oCount == null || fUCount == null || fRCount == null) {

			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			PaginatedQueryList<RequestMedia> openRequests = super.mapper()
					.query(RequestMedia.class, queryExpression);

			// Recurring requests
			DynamoDBQueryExpression queryExpression2 = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			PaginatedQueryList<RecurringRequestMedia> openRecRequests = super
					.mapper().query(RecurringRequestMedia.class,
							queryExpression2);

			for (RequestMedia rm : openRequests) {
				unsorted.put(rm, rm.getScheduleDate().getTime());

			}

			RequestMediaResource rmr = new RequestMediaResource();
			for (RecurringRequestMedia rrm : openRecRequests) {
				RequestMedia rm = rmr.convertToRM(rrm);
				unsorted.put(rm, rm.getScheduleDate().getTime());

			}

			for (Entry<RequestMedia, Long> entry : entriesSortedByValues(unsorted)) {
				// check to see if request is fulfilled
				RequestMedia rm = entry.getKey();
				if (rm.isRecurring()) {
					openCount++;
				} else {
					if (!rm.isRequestFulfilled()) {
						Date now = new Date();
						Date expiryDate = new Date();
						int duration = rm.getDuration(); // in minutes
						expiryDate.setTime(rm.getRequestDate().getTime()
								+ duration * 60000);
						if (now.getTime() > expiryDate.getTime()) {
							super.delete(rm, rm.geteMailAddress(), rm.getRequestId());
							// results.add(rm);
						} else {

							openCount++;
						}

					} else {

						if (rm.getFulfilledDate().getTime() < threeHoursAgo) {
							super.delete(rm, rm.geteMailAddress(), rm.getRequestId());
						}

					}

				}
			}
			super.storeInCache("OPENCOUNT:" + eMailAddress, 3600, openCount);

			HashMap<RequestMedia, Long> unsortedMedia = new HashMap<RequestMedia, Long>();

			for (Entry<RequestMedia, Long> entry : entriesSortedByValues(unsorted)) {
				RequestMedia rm = entry.getKey();

				if (rm.isRequestFulfilled()
						&& rm.getFulfilledDate().getTime() > threeHoursAgo) {
					unsortedMedia.put(rm, rm.getScheduleDate().getTime());
				}

			}
			for (Entry<RequestMedia, Long> entry : entriesSortedByValues(unsortedMedia)) {
				// check to see if request is fulfilled
				RequestMedia rm = entry.getKey();

				if (rm.isRequestFulfilled()) {
					if (rm.isMarkAsRead()) {
						fulfilledReadCount++;
					} else {
						fulfilledUnreadCount++;
					}

				}
			}

			super.storeInCache("FULFILLEDUNREADCOUNT:" + eMailAddress, 3600,
					fulfilledUnreadCount);
			super.storeInCache("FULFILLEDREADCOUNT:" + eMailAddress, 3600,
					fulfilledReadCount);

		} else {
			openCount = (Integer) oCount;
			fulfilledReadCount = (Integer) fRCount;
			fulfilledUnreadCount = (Integer) fUCount;
		}

		results.put("openrequests", openCount);
		results.put("fulfilledReadCount", fulfilledReadCount);
		results.put("fulfilledUnreadCount", fulfilledUnreadCount);

		// TODO add recurring requests to this list
		// ALSO ADD FILTERING!!!!!!!!!!!!

		if (aRCount == null || aUCount == null) {
			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));
			PaginatedQueryList<AssignedRequest> assignedToMe = super.mapper()
					.query(AssignedRequest.class, queryExpression);

			HashMap<AssignedRequest, Long> unsortedAssigned = new HashMap<AssignedRequest, Long>();
			for (AssignedRequest rm : assignedToMe) {
				unsortedAssigned.put(rm, rm.getAssignedDate().getTime());

			}

			for (Entry<AssignedRequest, Long> entry : entriesSortedByValues(unsortedAssigned)) {
				AssignedRequest assReq = entry.getKey();
				switch (assReq.getRequestType()) {
				case 0:
					RequestMedia rm = (RequestMedia) super.load(
							RequestMedia.class, assReq.getRequestorEmail(),
							assReq.getRequestId());
					if (rm == null) {
						super.delete(assReq, assReq.geteMailAddress(),
								assReq.getRequestId());
					} else {
						Date now = new Date();
						if (now.getTime() > assReq.getExpiryDate().getTime()) {
							super.delete(assReq, assReq.geteMailAddress(),
									assReq.getRequestId());
						} else {
							if (assReq.isMarkAsRead())
								assignedReadCount++;
							else
								assignedUnreadCount++;
						}
					}

					break;
				case 1:

					RecurringRequestMedia rrm = (RecurringRequestMedia) super
							.load(RecurringRequestMedia.class,
									assReq.getRequestorEmail(),
									assReq.getRequestId());
					if (rrm == null) {
						super.delete(assReq, assReq.geteMailAddress(),
								assReq.getRequestId());
					} else {
						// TODO if the window for this request has expired,
						// remove
						// it from the users assigned queue
						if (assReq.isMarkAsRead())
							assignedReadCount++;
						else
							assignedUnreadCount++;
					}
					break;
				}

			}
		} else {
			assignedReadCount = (Integer) aRCount;
			assignedUnreadCount = (Integer) aUCount;
		}
		results.put("assignedReadRequests", assignedReadCount);
		results.put("assignedUnreadRequests", assignedUnreadCount);
		return results;
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
		// long threedays = 3L * 24L * 60L * 60L * 1000L;
		long threehours = 3L * 60L * 60L * 1000L;
		Date expiryDate = new Date();
		expiryDate.setTime(uploadDate + threehours);
		return expiryDate;
	}

	public List<AssignedRequest> getAssignedRequests(String eMailAddress) {

		List<AssignedRequest> results = new ArrayList<AssignedRequest>();
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(eMailAddress));
		PaginatedQueryList<AssignedRequest> assignedToMe = super.mapper()
				.query(AssignedRequest.class, queryExpression);

		List<AssignedRequest> requestsToSave = new ArrayList<AssignedRequest>();
		for (AssignedRequest assReq : assignedToMe) {
			if (assReq.getExpiryDate().getTime() < new Date().getTime()) {
				super.delete(assReq, assReq.geteMailAddress(), assReq.getRequestId());
			} else {
				switch (assReq.getRequestType()) {
				case 0:
					RequestMedia rm = (RequestMedia) super.load(RequestMedia.class,
							assReq.getRequestorEmail(), assReq.getRequestId());
					assReq.setNameOfLocation(rm.getNameOfLocation());
					results.add(assReq);

					break;
				case 1:
					RecurringRequestMedia rrm = (RecurringRequestMedia) super.load(
							RecurringRequestMedia.class,
							assReq.getRequestorEmail(), assReq.getRequestId());
					assReq.setNameOfLocation(rrm.getNameOfLocation());
					results.add(assReq);
					break;
				}
				AssignedRequest newReq;
				try {
					newReq = (AssignedRequest) assReq.clone();
					newReq.setMarkAsRead(true);
					requestsToSave.add(newReq);
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		super.mapper().batchSave(requestsToSave);
		return results;
	}

}
