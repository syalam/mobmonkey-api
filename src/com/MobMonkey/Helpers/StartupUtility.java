package com.MobMonkey.Helpers;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.MobMonkey.Helpers.SimpleWorkFlow.ApnsSWF.ApnsActivities;
import com.MobMonkey.Helpers.SimpleWorkFlow.ApnsSWF.ApnsActivitiesImpl;
import com.MobMonkey.Helpers.SimpleWorkFlow.ApnsSWF.ApnsWorkflowImpl;
import com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF.AssignRequestActivities;
import com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF.AssignRequestActivitiesImpl;
import com.MobMonkey.Helpers.SimpleWorkFlow.AssignRequestSWF.AssignRequestWorkflowImpl;
import com.MobMonkey.Helpers.SimpleWorkFlow.GcmSWF.GcmActivities;
import com.MobMonkey.Helpers.SimpleWorkFlow.GcmSWF.GcmActivitiesImpl;
import com.MobMonkey.Helpers.SimpleWorkFlow.GcmSWF.GcmWorkflowImpl;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;

public class StartupUtility implements ServletContextListener {
	private AWSCredentials credentials;
	private ActivityWorker worker = null;
	private WorkflowWorker workflowWorker = null;
	private AmazonSimpleWorkflow swfClient;
	static final String AWS_CREDENTIALS_FILE = "AwsCredentials.properties";
	static final String AWS_CRED_ERROR_NOT_FOUND = String.format(
			"\n\nCould not find %s\n\n\r", AWS_CREDENTIALS_FILE);
	static final String AWS_CRED_ERROR_READING = String.format(
			"\n\nUnable to read %s\n\n\r", AWS_CREDENTIALS_FILE);
	private static Logger logger = Logger.getRootLogger();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		InputStream credentialsStream = getClass().getClassLoader()
				.getResourceAsStream(AWS_CREDENTIALS_FILE);
		if (credentialsStream != null) {
			try {
				credentials = new PropertiesCredentials(credentialsStream);
			} catch (IOException e) {
				logger.error(AWS_CRED_ERROR_READING);
			}
		}

		swfClient = new AmazonSimpleWorkflowClient(credentials);
		swfClient.setEndpoint("http://swf.us-west-1.amazonaws.com");

		// Setup GCM activities
		worker = new ActivityWorker(swfClient, "MobMonkey", "Gcm");
		GcmActivities gcmActImpl = new GcmActivitiesImpl();
		try {
			worker.addActivitiesImplementation(gcmActImpl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
		}
		worker.start();
		
		
		// Setup GCM workflow
		workflowWorker = new WorkflowWorker(swfClient, "MobMonkey", "Gcm");
		try {
			workflowWorker.addWorkflowImplementationType(GcmWorkflowImpl.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
		}
		workflowWorker.start();

		
		
		
		// Setup APNS activities
		worker = new ActivityWorker(swfClient, "MobMonkey", "Apns");
		ApnsActivities ApnsActImpl = new ApnsActivitiesImpl();
		try {
			worker.addActivitiesImplementation(ApnsActImpl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
		}
		worker.start();
		// Setup APNS workflow
		workflowWorker = new WorkflowWorker(swfClient, "MobMonkey", "Apns");
		try {
			workflowWorker
					.addWorkflowImplementationType(ApnsWorkflowImpl.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
		}
		workflowWorker.start();
		
		
		
		
		// Setup AssignRequest activities
		worker = new ActivityWorker(swfClient, "MobMonkey", "AssignRequest");
		AssignRequestActivities AssignRequestActivitiesActImpl = new AssignRequestActivitiesImpl();
		try {
			worker.addActivitiesImplementation(AssignRequestActivitiesActImpl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
		}
		worker.start();
		// Setup AssignRequest workflow
		workflowWorker = new WorkflowWorker(swfClient, "MobMonkey", "AssignRequest");
		try {
			workflowWorker
					.addWorkflowImplementationType(AssignRequestWorkflowImpl.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
		}
		workflowWorker.start();
		

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		worker.shutdown();
		workflowWorker.shutdown();

	}

}
