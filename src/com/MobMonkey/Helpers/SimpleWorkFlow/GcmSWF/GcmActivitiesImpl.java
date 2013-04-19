package com.MobMonkey.Helpers.SimpleWorkFlow.GcmSWF;

import java.io.StringWriter;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.io.ChunkedInputStream;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class GcmActivitiesImpl implements GcmActivities {
	static final String GCM_SERVICE_ENDPOINT = "https://android.googleapis.com/gcm/send";
	static final String GCM_API_KEY = "AIzaSyAiFXbr23CSWIxXE33CeIsQQN4gQXDIJdM";

	@Override
	@Activity(name = "GcmSendNotification", version = "1.12")
	@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
	public void sendNotification(String registration_id, String message,
			int badgeCount) throws Exception {
		Client client = Client.create();
		WebResource webResource = client.resource(GCM_SERVICE_ENDPOINT);
		client.setChunkedEncodingSize(512);

		String input = "{\"registration_ids\":[\"" + registration_id
				+ "\"],\"data\":{\"body\":\"" + message + "\",\"badgeCount\":"
				+ badgeCount + "}}";

		ClientResponse response = webResource
				.header("Content-Type", MediaType.APPLICATION_JSON)
				.header("Authorization", "key=" + GCM_API_KEY)
				.post(ClientResponse.class, input);

		int status = response.getStatus();
		if (status != 200) {

			StringWriter writer = new StringWriter();
			IOUtils.copy(response.getEntityInputStream(), writer);

			String body = writer.toString();
		} else {
			String error = response.getEntity(String.class);
		}

	}

}
