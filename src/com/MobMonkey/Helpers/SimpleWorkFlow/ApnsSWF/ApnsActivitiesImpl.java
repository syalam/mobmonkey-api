package com.MobMonkey.Helpers.SimpleWorkFlow.ApnsSWF;

import com.MobMonkey.Helpers.ApplePNSHelper;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;


public class ApnsActivitiesImpl implements ApnsActivities {
	
	@Override
	@Activity(name = "ApnsSendNotification", version = "1.10")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
	public void sendNotification(String eMailAddress, String[] deviceIds, String message,
			int badgeCount) throws Exception {
		
		ApplePNSHelper.send(eMailAddress, deviceIds, message, badgeCount);
	
	}

}
