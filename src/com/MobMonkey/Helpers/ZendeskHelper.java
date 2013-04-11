package com.MobMonkey.Helpers;

import java.io.IOException;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.MobMonkey.Models.ZenDesk.TicketResponse;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public final class ZendeskHelper {

	static final String ZENDESK_USERNAME = "rashaad@tappforce.com";
	static final String ZENDESK_PASSWORD = "taptap85";
	static final String ZENDESK_RESOURCE_URI = "https://mobmonkey.zendesk.com";
    ObjectMapper mapper = new ObjectMapper();
	public ZendeskHelper() {

	}

	public String createTicket(String url, String mediaId, String requestId) {
		String result = "";
		Client client = Client.create();
		WebResource webResource = client.resource(ZENDESK_RESOURCE_URI
				+ "/api/v2/tickets.json");

		client.addFilter(new HTTPBasicAuthFilter(ZENDESK_USERNAME,
				ZENDESK_PASSWORD));
		
		String input = "{\"ticket\":{\"type\":\"question\",\"priority\":\"high\",\"name\":\"MobMonkey API\",\"subject\":\"Inappropriate Content\",\"comment\":{\"body\":\"The media uploaded has been deemed inappropriate by the requestor. Please review: " + url + "\"}}}";

		ClientResponse response = webResource.type(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.post(ClientResponse.class, input);
		String resp = response.getEntity(String.class);
		
	     try {
			TicketResponse tr = mapper.readValue(resp, TicketResponse.class);
			HashMap map = tr.getTicket();
			Object id = map.get("id");
			result = id.toString();
	
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;

	}
	
	public boolean updateTicket(String ticketId, String text) {
		Client client = Client.create();
		WebResource webResource = client.resource(ZENDESK_RESOURCE_URI
				+ "/api/v2/tickets/" + ticketId + ".json");

		client.addFilter(new HTTPBasicAuthFilter(ZENDESK_USERNAME,
				ZENDESK_PASSWORD));
		
		String input = "{\"ticket\":{\"status\":\"solved\",\"comment\":{\"body\":\"The media was reviewed and deemed " + text + "\"}}}";

		ClientResponse response = webResource.type(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.put(ClientResponse.class, input);

		String body = response.getEntity(String.class);
		return true;

	}
	
}
