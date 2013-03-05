package com.MobMonkey.Helpers.SimpleWorkFlow.GcmSWF;

import javax.ws.rs.core.MediaType;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class GcmActivitiesImpl implements GcmActivities {
	static final String GCM_SERVICE_ENDPOINT = "https://android.googleapis.com/gcm/send";
	static final String GCM_API_KEY = "AIzaSyAiFXbr23CSWIxXE33CeIsQQN4gQXDIJdM";
	@Override
	public void sendNotification(String registration_id, String message, int badgeCount)
			throws Exception {
		Client client = Client.create();
		WebResource webResource = client.resource(GCM_SERVICE_ENDPOINT);
		webResource.header("Authorization", GCM_API_KEY);
		
		String input = "{\"registration_Id\":[\"" + registration_id + "\"],\"data\":{\"body\":\"" + message + "\",\"badgeCount\":" + badgeCount + "}}";

		ClientResponse response = webResource.type(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.post(ClientResponse.class, input);
		if(response.getStatus() != 200){
			//Something bad happened
		}else{
			System.out.println(response.getEntity(String.class));
		}
	
		
		
	}

}
