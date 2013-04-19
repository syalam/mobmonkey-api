package com.MobMonkey.Helpers;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public final class ZendeskHelper {

	static final String ZENDESK_USERNAME = "rashaad@tappforce.com";
	static final String ZENDESK_PASSWORD = "taptap85";
	static final String ZENDESK_RESOURCE_URI = "https://mobmonkey.zendesk.com";

	public ZendeskHelper() {

	}

	public boolean createTicket() {
		Client client = Client.create();
		WebResource webResource = client.resource(ZENDESK_RESOURCE_URI
				+ "/api/v2/tickets.json");

		client.addFilter(new HTTPBasicAuthFilter(ZENDESK_USERNAME,
				ZENDESK_PASSWORD));
		
		String input = "{\"ticket\":{\"subject\":\"Inappropriate Content\",\"comment\":{\"body\":\"The media uploaded has been deemed inappropriate by the requestor. Please review here.\"}}}";

		ClientResponse response = webResource.type(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.post(ClientResponse.class, input);
		return true;

	}
}
