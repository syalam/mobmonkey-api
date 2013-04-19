package com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF;
import com.MobMonkey.Models.RequestMediaLite;
import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;

/**
 * Contract of the hello world workflow
 */
@Workflow
@WorkflowRegistrationOptions(defaultExecutionStartToCloseTimeoutSeconds = 60)
public interface AssignRequestWorkflow {

	
    @Execute(version = "1.1")
    void assignRequest(String eMailAddress, String latitude,
			String longitude);


}
