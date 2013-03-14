package com.MobMonkey.Helpers.SimpleWorkFlow.GcmSWF;
import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;

/**
 * Contract of the hello world workflow
 */
@Workflow
@WorkflowRegistrationOptions(defaultExecutionStartToCloseTimeoutSeconds = 60)
public interface GcmWorkflow {

	
    @Execute(version = "1.6")
    void sendNotification(String registration_id, String message, int badgeCount);

}
