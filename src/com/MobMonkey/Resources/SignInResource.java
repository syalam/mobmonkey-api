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

import android.os.Build;

import com.MobMonkey.Helpers.EmailValidator;
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
			@QueryParam("oauthToken") String token,
			@QueryParam("providerUserName") String providerUserName) {

		if (useOAuth) {

			Oauth ou = super.mapper()
					.load(Oauth.class, providerUserName, token);

			if (ou == null) {
				// we do not have a user, so we should create one

				ou = new Oauth();
				ou.setoAuthToken(token);
				ou.seteMailVerified(false);
				ou.setoAuthProvider(provider);
				ou.setProviderUserName(providerUserName);
				super.mapper().save(ou);
				return Response
						.ok()
						.entity(new Status(
								"Success",
								"Email and token combination not found in DB. Setting up user with user & token, please present user with email registration screen",
								"300")).build();

			} else {
				if (ou.geteMailAddress() == null) {
					return Response
							.ok()
							.entity(new Status(
									"Failure",
									"This user needs to register their email address",
									"300")).build();

				} else {
					return Response
							.ok()
							.entity(new Status("Success",
									"User successfully signed in", "")).build();
				}
			}
		} else {

			User user = super.getUser(headers);
			user.setLastSignIn(new Date());
			super.mapper().save(user);
			return Response
					.ok()
					.entity(new Status("Success",
							"User successfully signed in", "")).build();

		}

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/registeremail/{providerUserName}/{token}")
	public Response registerEmailInJSON(@Context HttpHeaders headers,
			@PathParam("providerUserName") String providerUserName,
			@PathParam("token") String token,
			@QueryParam("deviceType") String type,
			@QueryParam("deviceId") String deviceId) {

		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0);
		boolean validEmail = new EmailValidator().validate(eMailAddress);
		if (!validEmail) {
			return Response
					.status(500)
					.entity(new Status("Failure",
							"Invalid email address specified", "")).build();
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

		Oauth ou = super.mapper().load(Oauth.class, providerUserName, token);
		if (ou == null) {
			return Response
					.status(500)
					.entity(new Status(
							"Failure",
							"The username & token specified is not found in the DB",
							"")).build();
		}
		ou.seteMailAddress(eMailAddress);
		super.mapper().save(ou);

		return Response
				.ok()
				.entity(new Status("Success", "Registered email address in DB",
						"")).build();
	}

}
