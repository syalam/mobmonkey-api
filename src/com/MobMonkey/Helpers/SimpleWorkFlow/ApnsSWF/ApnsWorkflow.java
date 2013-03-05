package com.MobMonkey.Helpers.SimpleWorkFlow.ApnsSWF;
import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;

/**
 * Contract of the hello world workflow
 */
@Workflow
@WorkflowRegistrationOptions(defaultExecutionStartToCloseTimeoutSeconds = 60)
public interface ApnsWorkflow {

	
    @Execute(version = "1.7")
    void sendNotification(String[] deviceIds, String message, int badgeCount);

}
