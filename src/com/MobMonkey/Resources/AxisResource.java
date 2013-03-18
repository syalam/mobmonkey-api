package com.MobMonkey.Resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.HEAD;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.Mailer;
import com.MobMonkey.Models.Status;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Path("/axis")
public class AxisResource extends ResourceHelper {

	
	@PUT
	public Response cameraActionCreate(@Context HttpHeaders headers,
			@QueryParam("action") String action,
			@QueryParam("client_id") String client_id,
			@QueryParam("client_srcaddr") String client_srcaddr) {

		String server = super.getHeaderParam("X-Stserver-Id", headers);

		if (action.equals("server_up")) {
			new Mailer().sendMail("mannyahuerta@gmail.com", "Dispatch Server Up",
					"Axis dispatch server: " + server + " has come online.");
			return Response.ok().build();
		}
		if (action.equals("client_connect")) {
			new Mailer().sendMail("mannyahuerta@gmail.com", "Camera Client Connect",
					"Axis client: " + client_id + " has connected.");
			return Response.status(200).build();
		}
		// TODO if client_id is not in DB, send error status
		// if we determine server is overloaded, send 302 status and Location:
		// new_dispatch_server_ip
		// for now we just accept it with a 204

		return Response.ok().build();
	}

	@DELETE
	public Response cameraActionDelete(@Context HttpHeaders headers,
			@QueryParam("action") String action,
			@QueryParam("client_id") String client_id) {

		String server = super.getHeaderParam("X-Stserver-Id", headers);

		if (action.equals("server_down")) {
			new Mailer().sendMail("mannyahuerta@gmail.com", "Dispatch Server Down",
					"Axis dispatch server: " + server + " has went offline.");
		}
		
		if (action.equals("client_disconnect")) {
			new Mailer().sendMail("mannyahuerta@gmail.com",
					"Client Disconnect", "Axis client: " + client_id
							+ " has disconnected.");
			return Response.status(200).build();
		}


		return Response.ok().build();
	}

	@HEAD
	public Response cameraActionClientHello(@Context HttpHeaders headers,
			@QueryParam("action") String action,
			@QueryParam("client_id") String client_id) {

		String server = super.getHeaderParam("X-Stserver-Id", headers);

		if (action.equals("client_hello")) {
			new Mailer().sendMail("mannyahuerta@gmail.com", "Camera Client Hello",
					"Axis client: " + client_id + " is attempting to connect.");
			return Response.status(204).build();
		}
		// TODO if client_id is not in DB, send error status
		// if we determine server is overloaded, send 302 status and Location:
		// new_dispatch_server_ip
		// for now we just accept it with a 204

		return Response.ok().build();
	}

	@PUT
	@Path("/dispatch")
	public Response dispatchCamera(@Context HttpHeaders headers,
			@QueryParam("mac") String mac, @QueryParam("oak") String oak) {

		if(!mac.matches("\\w{12}") && !oak.matches("\\w{4}-\\w{4}-\\w{4}")){
			return Response.status(Response.Status.BAD_REQUEST).entity(new Status("FAILURE", "Incorrect mac or oak provided, format for mac should be 12 (A-Z0-9). The format for oak should be XXXX-XXXX-XXXX", "")).build();
		}
		
		ClientResponse response = dispatch(mac, oak);
		
		return Response.status(response.getStatus()).entity(response.getEntity(String.class)).build();
				
	}
	
	public ClientResponse dispatch(String mac, String oak){
		
		Client client = Client.create();
		WebResource webResource = client.resource("http://184.169.152.58:3129/admin/dispatch.cgi");
		
		ClientResponse response = webResource
				.queryParam("action", "register")
				.queryParam("user", "adp_mobmonkey_100")
				.queryParam("pass", "Mx3wLVyyz3Aof3niCh3b")
				.queryParam("mac", mac)
				.queryParam("oak", oak)
				.queryParam("server", "184.169.152.58:80")
				.post(ClientResponse.class);
		
		return response;
	}
}
