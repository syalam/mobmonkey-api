package com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

/**
 * Contract for file processing activities
 */
@Activities(version="1.1")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 60, defaultTaskStartToCloseTimeoutSeconds = 60)
public interface AssignRequestActivities {
	
	@Activity(name = "AssignRequest", version = "1.1")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    public void assignRequest(String eMailAddress, String latitude, String longitude) throws Exception;
    
}