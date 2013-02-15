	package com.MobMonkey.Resources;

import java.util.Date;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

	static final Logger LOG = Logger.getLogger(UserResource.class);

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@Context HttpHeaders headers) {
		//can u handle oauth accounts
		User user = getUserWithHeaders(headers);

		if (null == user.getPassword() || null == user.geteMailAddress()) {
			Status s = new Status();
			s.setStatus("Missing required fields");
			s.setDescription("Email or password is missing");
			return Response.status(500).entity(s).build();
		}
		// TODO regex on email address

		if(!EmailValidator.validate(user.geteMailAddress())){
			return Response.status(500).entity(new Status("Failure", "Invalid email address: (" + user.geteMailAddress() + ") specified.", "")).build();
		}

		user.setPartnerId(user.getPartnerId());
		user.setVerified(false);
		user.setDateRegistered(new Date());
		super.save(user, user.geteMailAddress(), user.getPartnerId());
		// Let's save the user's device Id as well
		// TODO in signin API send the users deviceId so we will keep adding to
		// their list of devices..


        new SignInResource().addDevice(user.geteMailAddress(), user.getDeviceId(), user.getDeviceType());

		Verify v = new Verify(UUID.randomUUID().toString(),
				user.getPartnerId(), user.geteMailAddress(),
				user.getDateRegistered());

		super.save(v, v.getVerifyID(), v.getPartnerId());

		Mailer mail = new Mailer();
		mail.sendMail(
				user.geteMailAddress(),
				"registration e-mail.",
				"Thank you for registering!  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/user/"
						+ v.getPartnerId()
						+ "/"
						+ v.getVerifyID()
						+ "\">clicking here.</a>");

		Status s = new Status();
		s.setStatus("Success");
		s.setDescription("User " + user.geteMailAddress()
				+ " successfully signed up");
		s.setId(user.geteMailAddress());
		return Response.status(201).entity(s).build();
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpHeaders headers) {
		User user = getUserWithHeaders(headers);
		
		ResponseBuilder builder = Response.noContent();
		if (user != null) {
			builder.status(Response.Status.OK).entity(user);
		} else {
			builder.status(Response.Status.BAD_REQUEST);
		}
		return builder.build();
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@Context HttpHeaders headers) {

		User user = getUserWithHeaders(headers);

		// TODO - update partnerID with last activity
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId")
				.get(0).toLowerCase();

		if (null == user.getPassword() || null == user.geteMailAddress()) {
			Status s = new Status();
			s.setStatus("Missing required fields");
			s.setDescription("Email or password is missing");
			return Response.status(500).entity(s).build();
		}
		// TODO regex on email address

		if(!EmailValidator.validate(user.geteMailAddress())){ 
			return Response.status(500).entity(new Status("Failure", "Invalid email address: (" + user.geteMailAddress() + ") specified.", "")).build();
		}
		
		user.setPartnerId(partnerId);
		user.setVerified(false);
		user.setDateRegistered(new Date());
		super.save(User.class, user.geteMailAddress(), user.getPartnerId());
		// Let's save the user's device Id as well
		// TODO in signin API send the users deviceId so we will keep adding to
		// their list of devices..
	
		
        new SignInResource().addDevice(user.geteMailAddress(), user.getDeviceId(), user.getDeviceType());

		Verify v = new Verify(UUID.randomUUID().toString(),
				user.getPartnerId(), user.geteMailAddress(),
				user.getDateRegistered());

		super.save(v, v.getVerifyID(), v.getPartnerId());

		new Mailer().sendMail(
				user.geteMailAddress(),
				"registration e-mail.",
				"Thank you for registering!  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/user/"
						+ v.getPartnerId()
						+ "/"
						+ v.getVerifyID()
						+ "\">clicking here.</a>");

		Status s = new Status();
		s.setStatus("Success");
		s.setDescription("User " + user.geteMailAddress()
				+ " successfully signed up");
		s.setId(user.geteMailAddress());
		return Response.status(201).entity(s).build();
	}

	protected User getUserWithHeaders(HttpHeaders headers) {
		return (User) load(User.class,
				getHeaderParam(MobMonkeyApiConstants.PARTNER_ID, headers),
				getHeaderParam(MobMonkeyApiConstants.USER, headers));
	}
	
	protected String getHeaderParam(String key, HttpHeaders headers) {
		return headers.getRequestHeader(key).get(0); //?
	}
	
	
}
