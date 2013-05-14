package com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF;

import com.MobMonkey.Models.RequestMediaLite;
import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;

/**
 * Implementation of the Gcm Workflow
 */
public class AssignRequestMediaWorkflowImpl implements AssignRequestMediaWorkflow{

	AssignRequestActivitiesImpl client = new AssignRequestActivitiesImpl();
	
	@Override
	@Execute(version = "1.3")
	public void assignRequestMedia(String origRequestor, RequestMediaLite rm) {
		try {
			client.assignRequestMedia(origRequestor, rm);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
}