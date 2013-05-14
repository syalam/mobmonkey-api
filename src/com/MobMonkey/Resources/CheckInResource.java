package com.MobMonkey.Resources;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.ApplePNSHelper;
import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Helpers.NotificationHelper;
import com.MobMonkey.Models.AssignedRequest;
import com.MobMonkey.Models.CheckIn;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.MobMonkeyApiConstants;
import com.MobMonkey.Models.RequestMediaLite;
import com.MobMonkey.Models.Status;
import com.amazonaws.services.simpleworkflow.flow.DataConverter;
import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;

@Path("/checkin")
public class CheckInResource extends ResourceHelper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7922156377734286617L;

	public CheckInResource() {
		super();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createCheckInInJSON(CheckIn c, @Context HttpHeaders headers) {
	
		String partnerId = getHeaderParam(MobMonkeyApiConstants.PARTNER_ID, headers);
		String eMailAddress = getHeaderParam(MobMonkeyApiConstants.USER, headers);
		
	
		c.seteMailAddress(eMailAddress);
		c.setPartnerId(partnerId);
		c.setDateCheckedIn(new Date());

		String latitude = "";
		String longitude = "";

		// so i have checked in the user at a specific x,y
		// i should check to see if there are any requests in the area
		try {
			latitude = (!c.getLatitude().equals(null)) ? c.getLatitude() : "";
			longitude = (!c.getLongitude().equals(null)) ? c.getLongitude()
					: "";
		} catch (Exception exc) {

		}

		if (latitude != "" && longitude != "") {
			try {
				assignRequest(eMailAddress, c.getLatitude(), c.getLongitude());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		try {
			// Need to update our cache

			Object o = super.getFromCache("CheckInData");

			if (o != null) {
				try {
					@SuppressWarnings("unchecked")
					Map<String, CheckIn> checkIn = (HashMap<String, CheckIn>) o;

					checkIn.put(eMailAddress, c);

					super.storeInCache("CheckInData", 259200, checkIn);

				} catch (IllegalArgumentException e) {

				}
			}

			super.save(c, c.geteMailAddress());
		} catch (Exception exc) {
			return Response.status(500).entity("An error has occured").build();
		}
		
		String statusDescription = String.format("Successfully checked in.");

		return Response.ok().entity(new Status(SUCCESS, statusDescription, "200")).build();

	}
	
	public void assignRequest(String eMailAddress, String latitude, String longitude)
			throws IOException {
		Object[] workflowInput = new Object[] { eMailAddress, latitude, longitude };
		DataConverter converter = new JsonDataConverter();
		StartWorkflowExecutionRequest startWorkflowExecutionRequest = new StartWorkflowExecutionRequest();
		startWorkflowExecutionRequest.setInput(converter.toData(workflowInput));
		startWorkflowExecutionRequest.setDomain("MobMonkey");
		TaskList tasks = new TaskList();
		tasks.setName("AssignRequest");
		startWorkflowExecutionRequest.setTaskList(tasks);
		WorkflowType workflowType = new WorkflowType();
		workflowType.setName("AssignRequestWorkflow.assignRequest");
		workflowType.setVersion("1.3");
		startWorkflowExecutionRequest.setWorkflowType(workflowType);
		startWorkflowExecutionRequest.setWorkflowId(UUID.randomUUID()
				.toString());
		this.swfClient().startWorkflowExecution(startWorkflowExecutionRequest);

	}

	
}
