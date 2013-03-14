package com.MobMonkey.Resources;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.MobMonkey.Helpers.EmailValidator;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.MobMonkeyApiConstants;
import com.MobMonkey.Models.Oauth;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.User;
import com.MobMonkey.Models.Verify;

@Path("/signin")
public class SignInResource extends ResourceHelper {

	static final String FACEBOOK = "facebook", TWITTER = "twitter";
	Map<String, String> deviceTypeNames;

	public SignInResource() {
		deviceTypeNames = new HashMap<String, String>();
		deviceTypeNames.put("ios", "iOS");
		deviceTypeNames.put("android", "Android");
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response DeleteSignInInJSON(@Context HttpHeaders headers,
			@QueryParam("provider") String provider,
			@QueryParam("providerUserName") String providerUserName,
			@QueryParam("oauthToken") String token,
			@QueryParam("partnerId") String partnerId) {

		Oauth ou = (Oauth) load(Oauth.class, provider, providerUserName);

		String result = "";
		if (ou.getoAuthToken().equals(token)) {
			try {
				delete(ou, provider, providerUserName);
				result += "Successfully deleted Oauth signin information.";
			} catch (Exception exc) {
				result += "Warning, unable to find Oauth signin information.";
			}
		} else {
			result = "Tokens do not match.";
		}

		return Response.ok().entity(new Status("Success", result, "200"))
				.build();

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response SignInInJSON(
			@Context HttpHeaders headers,
			@QueryParam("deviceId") String deviceId,
			@QueryParam("deviceType") String type,
			//
			@DefaultValue("false") @QueryParam("useOAuth") boolean useOAuth,
			@QueryParam("provider") String provider,
			@QueryParam("oauthToken") String token,
			@QueryParam("providerUserName") String providerUserName,
			//
			@QueryParam("firstName") String firstName,
			@QueryParam("lastName") String lastName, @QueryParam("birthday") String birthday,
			@QueryParam("gender") int gender) {

		Date dob = UserResource.extractDob(birthday);
		String email = UserResource.getHeaderParam(MobMonkeyApiConstants.USER,
				headers);
		String partnerId = UserResource.getHeaderParam(
				MobMonkeyApiConstants.PARTNER_ID, headers);
		String password = UserResource.getHeaderParam(
				MobMonkeyApiConstants.AUTH, headers);
		if (birthday == null
				|| !UserResource.isValidString(firstName, lastName)
				|| !UserResource.isInRange(UserResource.MALE_FEMALE_RANGE,
						gender)) {
			// fail if params missing or invalid
			ResponseBuilder responseBuilder = Response.noContent();
			UserResource.requiredParamsMissing(responseBuilder, email);
			return responseBuilder.build();
		}

		if (useOAuth) {
			Oauth ou = (Oauth) super.load(Oauth.class, provider,
					providerUserName);

			if (TWITTER.equalsIgnoreCase(provider)) {
				if (ou == null) {
					// we do not have a user, so we should create one

					ou = new Oauth();
					ou.setoAuthToken(token);
					ou.seteMailVerified(false);
					ou.setoAuthProvider(provider);
					ou.setProviderUserName(providerUserName);

					super.save(ou, provider, providerUserName);

					User user = new User();
					user.seteMailAddress(provider + ":" + providerUserName);
					user.setPartnerId(partnerId);
					user.setBirthday(dob);
					user.setFirstName(firstName);
					user.setLastName(lastName);
					user.setGender(gender);
					user.setVerified(false);
					save(user, provider + ":" + providerUserName,
							user.getPartnerId());

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
										"This user needs to register their email address at /rest/signin/registeremail",
										"404")).build();

					} else {
						// we have a user, update them
						// if they pass the test

						if (birthday != null
								|| UserResource.isValidString(firstName, lastName)
								|| UserResource.isInRange(
										UserResource.MALE_FEMALE_RANGE, gender)) {

							User user = (User) super.load(User.class,
									ou.geteMailAddress(), partnerId);

							user.setBirthday(dob);
							user.setFirstName(firstName);
							user.setLastName(lastName);
							user.setGender(gender);
							user.setVerified(false);
							super.save(user, ou.geteMailAddress(), partnerId);
						}
						if (!addDevice(providerUserName, deviceId, type)) {
							return Response
									.status(500)
									.entity(new Status(
											"Failure",
											"You must specify a device type (Android or iOS)",
											"500")).build();
						}
						return Response
								.ok()
								.entity(new Status("Success",
										"User successfully signed in & updated", "200"))
								.build();
					}
				}
			} else if (FACEBOOK.equalsIgnoreCase(provider)) {
				if (ou == null) {
					// we do not have a user, so we should create one

					if (!EmailValidator.validate(providerUserName)) {
						return Response
								.status(Response.Status.INTERNAL_SERVER_ERROR)
								.entity(new Status(FAIL_STAT, String.format(
										INVALID_PARAM, email), "")).build();
					}
					ou = new Oauth();
					ou.setoAuthToken(token);
					ou.seteMailVerified(true);
					ou.setoAuthProvider(provider);
					ou.setProviderUserName(providerUserName);
					ou.seteMailAddress(providerUserName);

					super.save(ou, provider, providerUserName);

					User user = (User) load(User.class, ou.geteMailAddress(),
							partnerId);
					if (user == null) {
						user = new User();
						user.seteMailAddress(providerUserName);
						user.setPartnerId(partnerId);
						user.setBirthday(dob);
						user.setFirstName(firstName);
						user.setLastName(lastName);
						user.setGender(gender);
						user.setVerified(true);
						save(user, user.geteMailAddress(), user.getPartnerId());
					}

					if (!addDevice(providerUserName, deviceId, type)) {
						return Response
								.status(500)
								.entity(new Status(
										"Failure",
										"You must specify a device type (Android or iOS)",
										"500")).build();
					}

					return Response
							.ok()
							.entity(new Status("Success",
									"Successfully added email & token to DB.",
									"200")).build();

				} else {

					if (birthday != null
							|| UserResource.isValidString(firstName, lastName)
							|| UserResource.isInRange(
									UserResource.MALE_FEMALE_RANGE, gender)) {

						User user = (User) super.load(User.class,
								ou.geteMailAddress(), partnerId);

						user.setBirthday(dob);
						user.setFirstName(firstName);
						user.setLastName(lastName);
						user.setGender(gender);
						user.setVerified(false);
						super.save(user, ou.geteMailAddress(), partnerId);
					}
					
					if (!addDevice(providerUserName, deviceId, type)) {
						return Response
								.status(500)
								.entity(new Status(
										"Failure",
										"You must specify a device type (Android or iOS)",
										"500")).build();
					}
					return Response
							.ok()
							.entity(new Status("Success",
									"User successfully signed in", "200"))
							.build();

				}
			} else {
				// Not supported
				return Response
						.status(500)
						.entity(new Status("Failure",
								"There is no support for " + provider
										+ " at this time.", "500")).build();
			}

		} else {
			// TODO we have a regular MobMonkey signin, need to authenticate

			User user = (User) super.load(User.class, email, partnerId);

			if (user == null || !user.getPassword().equals(password)) {
				return Response
						.status(401)
						.entity(new Status(
								"Failure",
								"Unauthorized.  Please provide valid credentials.",
								"500")).build();
			}
			if (!addDevice(user.geteMailAddress(), deviceId, type)) {
				return Response
						.status(500)
						.entity(new Status(
								"Failure",
								"You must specify a device type (Android or iOS)",
								"500")).build();
			}
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
			@QueryParam("provider") String provider,
			@QueryParam("oauthToken") String token,
			@QueryParam("deviceType") String type,
			@QueryParam("deviceId") String deviceId,
			@QueryParam("eMailAddress") String eMailAddress

	) {

		String partnerId = UserResource.getHeaderParam(
				MobMonkeyApiConstants.PARTNER_ID, headers);

		if (!EmailValidator.validate(eMailAddress)) {
			return Response
					.status(500)
					.entity(new Status("Failure",
							"Invalid email address specified", "500")).build();
		}

		Oauth ou = (Oauth) super.load(Oauth.class, provider, providerUserName);
		if (ou == null) {
			return Response
					.status(404)
					.entity(new Status(
							"Failure",
							"The username & token specified is not found in the DB",
							"404")).build();
		} else {

			ou.seteMailAddress(eMailAddress);
			ou.seteMailVerified(false);
			ou.setoAuthToken(token);
			ou.setoAuthProvider(provider);
			super.save(ou, provider, providerUserName);

			User user = (User) load(User.class, ou.geteMailAddress(), partnerId);

			if (user == null) {
				user = (User) load(User.class, provider + ":"
						+ providerUserName, partnerId);
				delete(user, provider + ":" + providerUserName, partnerId);
				user.seteMailAddress(eMailAddress);
				user.setVerified(false);
				save(user, user.geteMailAddress(), user.getPartnerId());

				Verify verify = new Verify(UUID.randomUUID().toString(),
						partnerId, eMailAddress, user.getDateRegistered());
				super.save(verify, verify.getVerifyID(), verify.getPartnerId());

				new UserResource().mailer.sendMail(
						eMailAddress,
						CREATING_USER_SUBJECT,
						String.format(THANK_YOU_FOR_REGISTERING,
								verify.getPartnerId(), verify.getVerifyID()));

			}

			if (!addDevice(eMailAddress, deviceId, type)) {
				return Response
						.status(500)
						.entity(new Status(
								"Failure",
								"You must specify a device type (Android or iOS)",
								"500")).build();
			}
			return Response
					.ok()
					.entity(new Status("Success",
							"Registered email address in DB", "200")).build();

		}
	}

	public boolean addDevice(String eMailAddress, String deviceId, String type) {
		boolean ableToAddDevice = deviceId != null;
		if (ableToAddDevice) {
			String deviceType = type == null ? "" : type.toLowerCase();
			String deviceTypeName = deviceTypeNames.get(deviceType);
			ableToAddDevice = deviceTypeName != null;

			if (ableToAddDevice) {
				save(new Device(eMailAddress, deviceId, deviceTypeName),
						eMailAddress, deviceId);
				deleteFromCache("DEV" + eMailAddress);
			}
		}
		return ableToAddDevice;
	}

}
