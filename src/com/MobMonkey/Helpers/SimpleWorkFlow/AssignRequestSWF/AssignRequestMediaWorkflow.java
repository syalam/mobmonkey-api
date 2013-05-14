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
public interface AssignRequestMediaWorkflow {
    
    @Execute(version = "1.3")
    void assignRequestMedia(String origRequestor, RequestMediaLite rm);

}
