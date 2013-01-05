package com.MobMonkey.Resources;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.Bookmark;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.Oauth;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.User;

@Path("/signin")
public class SignInResource extends ResourceHelper {

	public SignInResource() {
		super();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{type}/{deviceId}")
	public Response SignInInJSON(@Context HttpHeaders headers,
			@PathParam("deviceId") String deviceId,
			@PathParam("type") String type,
			@DefaultValue("false") @QueryParam("useOAuth") boolean useOAuth,
			@QueryParam("provider") String provider,
			@QueryParam("oauthToken") String token) {
		
		String response = "";
		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0);
		
		
		if (useOAuth) {

			Oauth ou = super.mapper().load(Oauth.class, eMailAddress, token);

			if (ou == null) {
				// we do not have a user, so we should create one
				ou = new Oauth();
				ou.seteMailAddress(eMailAddress);
				ou.setoAuthToken(token);
				ou.seteMailVerified(true);
				ou.setoAuthProvider(provider);
				super.mapper().save(ou);
				response = "Email and token combination not found in DB. Setting up user with credentials specified";
				
			}else{
				response = "Email and token combination found in DB. User is successfully signed in.";
			}
		} else {
			
			User user = super.getUser(headers);
			user.setLastSignIn(new Date());
			super.mapper().save(user);
			response = "Successfully signed in user.";
		}

		Device d = new Device();
		d.seteMailAddress(eMailAddress);
		if (type.toLowerCase().equals("ios")) {
			d.setDeviceType("iOS");
		} else if (type.toLowerCase().equals("android")) {
			d.setDeviceType("Android");
		} else {
			return Response
					.status(500)
					.entity(new Status("Failure",
							"You must specify a device type (Android or iOS)",
							"")).build();
		}

		d.setDeviceId(deviceId);	

		super.mapper().save(d);
		
		return Response.ok()
				.entity(new Status("Success", response, ""))
				.build();

	}
}
