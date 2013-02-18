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

	static final String FAIL_STAT = "Failure", SUCCESS = "Success";
	static final String INVALID_PARAM = "Invalid value: [%s]";
	static final String THANK_YOU_FOR_REGISTERING = "Thank you for registering!  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/user/%s/%s\">clicking here.</a>";
	static final String CREATING_USER_SUBJECT = "registration e-mail.", UPDATE_USER_SUBJECT = "Updated user account";
	static final Logger LOG = Logger.getLogger(UserResource.class);

	public static final String DOB_FORMAT = "MMMM d, yyyy";
	public static final SimpleDateFormat DOB_FORMATTER = new SimpleDateFormat(DOB_FORMAT, Locale.ENGLISH); //August 1 1960
	public static final int[] MALE_FEMALE_RANGE = { 0, 1 };
	
	final Mailer mailer;
	
	public UserResource() {
		mailer = new Mailer();
	}
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@QueryParam("fname") String fName,
			@QueryParam("lname") String lName, @QueryParam("dob") String dob,
			@QueryParam("gender") int gender, @Context HttpHeaders headers) {
		
		ResponseBuilder response = Response.noContent();

		String partnerId = getHeaderParam(MobMonkeyApiConstants.PARTNER_ID, headers);
		String email = getHeaderParam(MobMonkeyApiConstants.USER, headers);
		String password = getHeaderParam(MobMonkeyApiConstants.AUTH, headers);

		if (!isValidString(email, partnerId, password)) {
			if  (!EmailValidator.validate(email)) {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Status(FAIL_STAT, String.format(INVALID_PARAM, email), ""));
			} else {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Status(FAIL_STAT, String.format(INVALID_PARAM, "partnerid or password"), ""));
			}
		} else {
			Date dobDate = extractDob(dob);
			if (dobDate != null && isValidString(fName, lName) && isInRange(MALE_FEMALE_RANGE, gender)) {
				User user = (User) load(User.class, partnerId, email);
				if (user == null) {
					//creating new user
					user = new User();
					user.setPartnerId(partnerId);
					user.setDateRegistered(new Date());
					user.setVerified(false);
					
					//required params
					user.setFirstName(fName);
					user.setLastName(lName);
					user.setGender(gender);
					user.setBirthday(dobDate);
					
					super.save(user, email, partnerId);

					//TODO ? is the below needed for creation of new user?
					new SignInResource().addDevice(email, user.getDeviceId(), user.getDeviceType());
					Verify verify = new Verify(UUID.randomUUID().toString(),
							partnerId, email,
							user.getDateRegistered());
					super.save(verify, verify.getVerifyID(), verify.getPartnerId());
					//TODO

					mailer.sendMail(
							email,
							CREATING_USER_SUBJECT,
							String.format(THANK_YOU_FOR_REGISTERING,
									verify.getPartnerId(), verify.getVerifyID()));

					String statusDescription = String.format("User [%s] successfully signed up", email);
					response.status(Response.Status.CREATED).entity(new Status(SUCCESS, statusDescription, email));
				} else {
					//user exists
					String statusDescription = String.format("User [%s] already exists.", email);
					response.status(Response.Status.OK).entity(new Status(SUCCESS, statusDescription, email));
				}
			} else {
				requiredParamsMissing(response, email);
			}
		}
		return response.build();
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpHeaders headers) {
		ResponseBuilder response = Response.noContent();
		
		String partnerId = getHeaderParam(MobMonkeyApiConstants.PARTNER_ID, headers);
		String email = getHeaderParam(MobMonkeyApiConstants.USER, headers);
		String password = getHeaderParam(MobMonkeyApiConstants.AUTH, headers);

		if (!isValidString(email, partnerId, password)) {
			if  (!EmailValidator.validate(email)) {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Status(FAIL_STAT, String.format(INVALID_PARAM, email), ""));
			} else {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Status(FAIL_STAT, String.format(INVALID_PARAM, "partnerid or password"), ""));
			}
		} else {
			User user = (User) load(User.class, partnerId, email);
			if (user != null) {
				response.status(Response.Status.OK).entity(user);
			} else {
				response.status(Response.Status.BAD_REQUEST).entity(new Status(FAIL_STAT, String.format("Unable to get user for %s", email), ""));
			}
		} 
		return response.build();
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@QueryParam("fname") String fName,
			@QueryParam("lname") String lName, @QueryParam("dob") String dob,
			@QueryParam("gender") int gender, @Context HttpHeaders headers) {
		ResponseBuilder response = Response.noContent();

		String partnerId = getHeaderParam(MobMonkeyApiConstants.PARTNER_ID, headers);
		String email = getHeaderParam(MobMonkeyApiConstants.USER, headers);
		String password = getHeaderParam(MobMonkeyApiConstants.AUTH, headers);

		if (!isValidString(email, partnerId, password)) {
			if  (!EmailValidator.validate(email)) {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Status(FAIL_STAT, String.format(INVALID_PARAM, email), ""));
			} else {
				response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Status(FAIL_STAT, String.format(INVALID_PARAM, "partnerid or password"), ""));
			}
		} else {
			//check required params
			Date dobDate = extractDob(dob);
			if (dobDate != null && isValidString(fName, lName) && isInRange(MALE_FEMALE_RANGE, gender)) {
				
				User user = (User) load(User.class, partnerId, email);

				if (user != null) {
					user.setPartnerId(partnerId);
					user.setDateRegistered(new Date());
					user.setVerified(false);
					
					//fname, lname, dob, gender
					user.setFirstName(fName);
					user.setLastName(lName);
					user.setBirthday(dobDate);
					user.setGender(gender);
					
					super.save(user, email, partnerId);

					//TODO ? is the below needed for update of user?
//					new SignInResource().addDevice(email, user.getDeviceId(), user.getDeviceType());
//					Verify verify = new Verify(UUID.randomUUID().toString(),
//							partnerId, email,
//							user.getDateRegistered());
//					super.save(verify, verify.getVerifyID(), verify.getPartnerId());
					//TODO

					String statusDescription = String.format("User [%s] details updated", email);
					mailer.sendMail(email, UPDATE_USER_SUBJECT, statusDescription);

					response.status(Response.Status.OK).entity(new Status(SUCCESS, statusDescription, email));
				} else {
					String statusDescription = String.format("User [%s] exists, nothing updated.", email);
					response.status(Response.Status.BAD_REQUEST).entity(new Status(FAIL_STAT, statusDescription, email));
				}
			} else {
				requiredParamsMissing(response, email);
			}
		}
		return response.build();
	}

	public static Date extractDob(String dob) {
		Date dobDate = null;
		try {
			dobDate = DOB_FORMATTER.parse(dob);
		} catch (ParseException e) {
			LOG.error("Unable to extract dob", e);
		}
		return dobDate;
	}

	protected User getUserWithHeaders(HttpHeaders headers) {
		return (User) load(User.class,
				getHeaderParam(MobMonkeyApiConstants.PARTNER_ID, headers),
				getHeaderParam(MobMonkeyApiConstants.USER, headers));
	}

	public static String getHeaderParam(String key, HttpHeaders headers) {
		return headers.getRequestHeader(key).get(0); //?
	}

	public static boolean isInRange(int[] range, int param) {
		return param >= range[0] && param <= range[1];
	}
	
	public static void requiredParamsMissing(ResponseBuilder response, String email) {
		String statusDescription = String.format("One or more params invalid [%s]", String.format("First(String), Last(String), Date of birth(%s) or Gender(1 or 0)", DOB_FORMAT));
		response.status(Response.Status.OK).entity(new Status(SUCCESS, statusDescription, email));
	}
	
	/**
	 * @return	string is not null or equals to ""
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
