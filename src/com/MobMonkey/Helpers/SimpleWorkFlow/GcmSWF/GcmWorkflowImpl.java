package com.MobMonkey.Helpers.SimpleWorkFlow.GcmSWF;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;

/**
 * Implementation of the Gcm Workflow
 */
public class GcmWorkflowImpl implements GcmWorkflow{

	GcmActivitiesImpl client = new GcmActivitiesImpl();
	
	@Override
	@Execute(version = "1.5")
	public void sendNotification(String registration_id, String message, int badgeCount) {
		// TODO Auto-generated method stub
		try {
			client.sendNotification(registration_id, message, badgeCount);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
}