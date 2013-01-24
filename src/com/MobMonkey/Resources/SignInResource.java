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
	public Response SignInInJSON(@Context HttpHeaders headers,
			@QueryParam("deviceId") String deviceId,
			@QueryParam("deviceType") String type,
			@DefaultValue("false") @QueryParam("useOAuth") boolean useOAuth,
			@QueryParam("provider") String provider,
			@QueryParam("oauthToken") String token,
			@QueryParam("providerUserName") String providerUserName) {

		if (useOAuth) {

			Oauth ou = super.mapper()
					.load(Oauth.class, providerUserName, token);

			if (provider.toLowerCase().equals("twitter")) {
				if (ou == null) {
					// we do not have a user, so we should create one

					ou = new Oauth();
					ou.setoAuthToken(token);
					ou.seteMailVerified(false);
					ou.setoAuthProvider(provider);
					ou.setProviderUserName(providerUserName);
					super.mapper().save(ou);
					return Response
							.status(404)
							.entity(new Status(
									"Success",
									"Email and token combination not found in DB. Setting up user with user & token, please present user with email registration screen",
									"404")).build();

				} else {
					if (ou.geteMailAddress() == null) {
						return Response
								.status(404)
								.entity(new Status(
										"Failure",
										"This user needs to register their email address",
										"404")).build();

					} else {
						if(!addDevice(providerUserName, deviceId, type)){
							return Response
									.status(500)
									.entity(new Status("Failure",
											"You must specify a device type (Android or iOS)",
											"500")).build();
						}
						return Response
								.ok()
								.entity(new Status("Success",
										"User successfully signed in", "200"))
								.build();
					}
				}
			}
			else if(provider.toLowerCase().equals("facebook")){
				if (ou == null) {
					// we do not have a user, so we should create one

					ou = new Oauth();
					ou.setoAuthToken(token);
					ou.seteMailVerified(true);
					ou.setoAuthProvider(provider);
					ou.setProviderUserName(providerUserName);
					ou.seteMailAddress(providerUserName);
					
					super.mapper().save(ou);
					if(!addDevice(providerUserName, deviceId, type)){
						return Response
								.status(500)
								.entity(new Status("Failure",
										"You must specify a device type (Android or iOS)",
										"500")).build();
					}

					return Response
							.ok()
							.entity(new Status(
									"Success",
									"Successfully added email & token to DB.",
									"200")).build();

				} else {
					if (ou.geteMailAddress() == null) {
						return Response
								.status(404)
								.entity(new Status(
										"Failure",
										"This user needs to register their email address",
										"404")).build();

					} else {
						return Response
								.ok()
								.entity(new Status("Success",
										"User successfully signed in", "200"))
								.build();
					}
				}
			}
			else{
				//Not supported
				return Response
						.status(500)
						.entity(new Status("Failure",
								"There is no support for " + provider + " at this time.", "500"))
						.build();
			}

		} else {

		
			return Response
					.ok()
					.entity(new Status("Success",
							"User successfully signed in", "200")).build();

		}

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/registeremail")
	public Response registerEmailInJSON(@Context HttpHeaders headers,
			@QueryParam("providerUserName") String providerUserName,
			@QueryParam("oauthToken") String token,
			@QueryParam("deviceType") String type,
			@QueryParam("deviceId")	String deviceId,
			@QueryParam("eMailAddress") String eMailAddress) {

	
		boolean validEmail = new EmailValidator().validate(eMailAddress);
		if (!validEmail) {
			return Response
					.status(500)
					.entity(new Status("Failure",
							"Invalid email address specified", "500")).build();
		}

		if(!addDevice(eMailAddress, deviceId, type)){
			return Response
					.status(500)
					.entity(new Status("Failure",
							"You must specify a device type (Android or iOS)",
							"500")).build();
		}

		Oauth ou = super.mapper().load(Oauth.class, providerUserName, token);
		if (ou == null) {
			return Response
					.status(404)
					.entity(new Status(
							"Failure",
							"The username & token specified is not found in the DB",
							"404")).build();
		}
	
		ou.seteMailAddress(eMailAddress);
		ou.setoAuthToken(token);
		super.mapper().save(ou);
		

		return Response
				.ok()
				.entity(new Status("Success", "Registered email address in DB",
						"200")).build();
	}
	
	public boolean addDevice(String eMailAddress, String deviceId, String type){
		Device d = new Device();
		d.seteMailAddress(eMailAddress);
		if (type.toLowerCase().equals("ios")) {
			d.setDeviceType("iOS");
		} else if (type.toLowerCase().equals("android")) {
			d.setDeviceType("Android");
		} else {
			return false;
		}

		d.setDeviceId(deviceId);

		super.mapper().save(d);
		return true;
	}

}
