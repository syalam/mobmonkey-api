package com.MobMonkey.Resources;

import java.util.ArrayList;
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

		List<RequestMedia> results = new ArrayList<RequestMedia>();

		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0)
				.toLowerCase();

		if (type.toLowerCase().equals("openrequests")) {
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

				if (!rm.isRequestFulfilled()) {
					results.add(rm);
				}
			}

			return Response.ok().entity(results).build();
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
					break;
				case 1:
					RecurringRequestMedia rrm = super.mapper().load(RecurringRequestMedia.class, assReq.getRequestorEmail());
					assReq.setNameOfLocation(new Locator().reverseLookUp(
							rrm.getProviderId(), rrm.getLocationId()).getName());
					break;
				}
				
					
			}

			return Response.ok().entity(assignedToMe.toArray()).build();
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
					results.add(rm);
				}
			}

			return Response.ok().entity(results).build();
		}

		return Response
				.status(500)
				.entity(new Status("Failure", type
						+ " is not a valid inbox parameter", "")).build();
	}
}
