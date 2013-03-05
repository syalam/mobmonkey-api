package com.MobMonkey.Helpers.SimpleWorkFlow.ApnsSWF;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

/**
 * Contract for file processing activities
 */
@Activities(version="1.7")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 60, defaultTaskStartToCloseTimeoutSeconds = 60)
public interface ApnsActivities {
	
	@Activity(name = "ApnsSendNotification", version = "1.7")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    public void sendNotification(String[] deviceIds, String message, int badgeCount) throws Exception;
    
}