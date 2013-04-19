package com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF;

import com.MobMonkey.Models.RequestMediaLite;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

/**
 * Contract for file processing activities
 */
@Activities(version="1.2")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 60, defaultTaskStartToCloseTimeoutSeconds = 60)
public interface AssignRequestActivities {
	
	@Activity(name = "AssignRequest", version = "1.2")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    public void assignRequest(String eMailAddress, String latitude, String longitude) throws Exception;
    
	@Activity(name = "AssignRequestMedia", version = "1.6")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
	public void assignRequestMedia(String origRequestor, RequestMediaLite rm);
	
}