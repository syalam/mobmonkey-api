package com.MobMonkey.Resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.MobMonkey.Models.Media;
import com.MobMonkey.Models.RecurringRequestMedia;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.RequestMediaLite;
import com.MobMonkey.Models.Status;
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

	public List<RequestMedia> getRequests(String type, String eMailAddress) {
		List<RequestMedia> results = new ArrayList<RequestMedia>();

		if (type.toLowerCase().equals("openrequests")) {
			// TODO add recurring requests to this list
			// ALSO ADD FILTERING!!!!!!!!!!!!

			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			PaginatedQueryList<RequestMedia> openRequests = super.mapper()
					.query(RequestMedia.class, queryExpression);

			for (RequestMedia rm : openRequests) {
				// check to see if request is fulfilled
				if (!rm.isRequestFulfilled()) {
					Date now = new Date();
					Date expiryDate = new Date();
					int duration = rm.getDuration(); // in minutes
					expiryDate.setTime(rm.getRequestDate().getTime() + duration
							* 60000);
					if (now.getTime() > expiryDate.getTime()) {
						rm.setExpired(true);
					} else {
						rm.setExpired(false);
					}

					results.add(rm);
				}
			}

		}
		if (type.toLowerCase().equals("assignedrequests")) {
			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));
			PaginatedQueryList<AssignedRequest> assignedToMe = super.mapper()
					.query(AssignedRequest.class, queryExpression);

			for (AssignedRequest assReq : assignedToMe) {
				switch (assReq.getRequestType()) {
				case 0:
					RequestMedia rm = super.mapper().load(RequestMedia.class,
							assReq.getRequestorEmail(), assReq.getRequestId());
					assReq.setNameOfLocation(new Locator().reverseLookUp(
							rm.getProviderId(), rm.getLocationId()).getName());
					results.add(rm);
					break;
				case 1:
					// TODO convert rrm to a RequestMedia and add it to results
					RecurringRequestMedia rrm = super.mapper().load(
							RecurringRequestMedia.class,
							assReq.getRequestorEmail());
					assReq.setNameOfLocation(new Locator().reverseLookUp(
							rrm.getProviderId(), rrm.getLocationId()).getName());
					break;
				}

			}

		}
		if (type.toLowerCase().equals("fulfilledrequests")) {
			// TODO add recurring requests to this list
			// ALSO ADD FILTERING!!!!!!!!!!!!

			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			PaginatedQueryList<RequestMedia> openRequests = super.mapper()
					.query(RequestMedia.class, queryExpression);

			for (RequestMedia rm : openRequests) {
				if (rm.getProviderId() != null && rm.getLocationId() != null) {
					rm.setNameOfLocation(new Locator().reverseLookUp(
							rm.getProviderId(), rm.getLocationId()).getName());
				}

				if (rm.isRequestFulfilled()) {
					// TODO if the request is expired, or has no media to it, do
					// not add it!
					DynamoDBQueryExpression mediaQuery = new DynamoDBQueryExpression(
							new AttributeValue().withS(rm.getRequestId()));
					PaginatedQueryList<Media> media = super.mapper().query(
							Media.class, mediaQuery);
					rm.setMediaUrl(media.get(0).getMediaURL());
					results.add(rm);
				}
			}
		}
		return results;
	}

	public List<AssignedRequest> getAssignedRequests(String eMailAddress) {

		List<AssignedRequest> results = new ArrayList<AssignedRequest>();
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(eMailAddress));
		PaginatedQueryList<AssignedRequest> assignedToMe = super.mapper()
				.query(AssignedRequest.class, queryExpression);

		for (AssignedRequest assReq : assignedToMe) {
			switch (assReq.getRequestType()) {
			case 0:
				RequestMedia rm = super.mapper().load(RequestMedia.class,
						assReq.getRequestorEmail(), assReq.getRequestId());
				try {
					assReq.setNameOfLocation(new Locator().reverseLookUp(
							rm.getProviderId(), rm.getLocationId()).getName());

					results.add(assReq);
				} catch (Exception exc) {

				}
				break;
			case 1:
				RecurringRequestMedia rrm = super.mapper()
						.load(RecurringRequestMedia.class,
								assReq.getRequestorEmail());
				assReq.setNameOfLocation(new Locator().reverseLookUp(
						rrm.getProviderId(), rrm.getLocationId()).getName());
				results.add(assReq);
				break;
			}

		}
		return results;
	}
}
