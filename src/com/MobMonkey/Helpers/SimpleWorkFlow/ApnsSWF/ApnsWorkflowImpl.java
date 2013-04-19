package com.MobMonkey.Helpers.SimpleWorkFlow.ApnsSWF;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;

/**
 * Implementation of the Gcm Workflow
 */
public class ApnsWorkflowImpl implements ApnsWorkflow{

	ApnsActivitiesImpl client = new ApnsActivitiesImpl();
	
	@Override
	@Execute(version = "1.7")
	public void sendNotification(String[] deviceIds, String message, int badgeCount) {
		// TODO Auto-generated method stub
		try {
			client.sendNotification(deviceIds, message, badgeCount);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
}