package com.MobMonkey.Helpers.SimpleWorkFlow.GcmSWF;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

/**
 * Contract for file processing activities
 */
@Activities(version="1.5")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 60, defaultTaskStartToCloseTimeoutSeconds = 60)
public interface GcmActivities {
	
	@Activity(name = "GcmSendNotification", version = "1.5")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    public void sendNotification(String registration_id, String message, int badgeCount) throws Exception;
    
}