package com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF;

import com.MobMonkey.Models.RequestMediaLite;
import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;

/**
 * Implementation of the Gcm Workflow
 */
public class AssignRequestWorkflowImpl implements AssignRequestWorkflow{

	AssignRequestActivitiesImpl client = new AssignRequestActivitiesImpl();
	

	@Override
	@Execute(version = "1.3")
	public void assignRequest(String eMailAddress, String latitude, String longitude) {

		try {
			client.assignRequest(eMailAddress, latitude, longitude);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
}