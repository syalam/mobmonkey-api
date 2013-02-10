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

import org.apache.log4j.Logger;

import com.MobMonkey.Helpers.EmailValidator;
import com.MobMonkey.Helpers.Mailer;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.User;
import com.MobMonkey.Models.Verify;

@Path("/user")
public class UserResource extends ResourceHelper {

	static final Logger LOG = Logger.getLogger(UserResource.class);

	@PUT
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(User User, @Context HttpHeaders headers) {
		//when { tw, fb, other } user,  gather DOB,



		// TODO - update partnerID with last activity
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId")
				.get(0).toLowerCase();

		if (null == User.getPassword() || null == User.geteMailAddress()) {
			Status s = new Status();
			s.setStatus("Missing required fields");
			s.setDescription("Email or password is missing");
			return Response.status(500).entity(s).build();
		}
		// TODO regex on email address

		if(!EmailValidator.validate(User.geteMailAddress())){
			return Response.status(500).entity(new Status("Failure", "Invalid email address: (" + User.geteMailAddress() + ") specified.", "")).build();
		}

		User.setPartnerId(partnerId);
		User.setVerified(false);
		User.setDateRegistered(new Date());
		super.save(User, User.geteMailAddress(), User.getPartnerId());
		// Let's save the user's device Id as well
		// TODO in signin API send the users deviceId so we will keep adding to
		// their list of devices..


        new SignInResource().addDevice(User.geteMailAddress(), User.getDeviceId(), User.getDeviceType());

		Verify v = new Verify(UUID.randomUUID().toString(),
				User.getPartnerId(), User.geteMailAddress(),
				User.getDateRegistered());

		super.save(v, v.getVerifyID(), v.getPartnerId());

		Mailer mail = new Mailer();
		mail.sendMail(
				User.geteMailAddress(),
				"registration e-mail.",
				"Thank you for registering!  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/user/"
						+ v.getPartnerId()
						+ "/"
						+ v.getVerifyID()
						+ "\">clicking here.</a>");

		Status s = new Status();
		s.setStatus("Success");
		s.setDescription("User " + User.geteMailAddress()
				+ " successfully signed up");
		s.setId(User.geteMailAddress());
		return Response.status(201).entity(s).build();
	}

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpHeaders headers) {
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId")
				.get(0).toLowerCase();
		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0)
				.toLowerCase();

		User user = (User) load(User.class, eMailAddress, partnerId);
		user.setPassword(null);

		LOG.error(">>> get: " + user.geteMailAddress() );

		return Response.ok().entity(user).build();
	}

	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(User User, @Context HttpHeaders headers) {

		//sudo impl

		//be able to pull up a twitter user, fb user or any other...

		//if found <update, update, update>

		//super.save(whatever)...

		//else return error

		// TODO - update partnerID with last activity
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId")
				.get(0).toLowerCase();

		if (null == User.getPassword() || null == User.geteMailAddress()) {
			Status s = new Status();
			s.setStatus("Missing required fields");
			s.setDescription("Email or password is missing");
			return Response.status(500).entity(s).build();
		}
		// TODO regex on email address

		if(!EmailValidator.validate(User.geteMailAddress())){
			return Response.status(500).entity(new Status("Failure", "Invalid email address: (" + User.geteMailAddress() + ") specified.", "")).build();
		}
		
		User.setPartnerId(partnerId);
		User.setVerified(false);
		User.setDateRegistered(new Date());
		super.save(User, User.geteMailAddress(), User.getPartnerId());
		// Let's save the user's device Id as well
		// TODO in signin API send the users deviceId so we will keep adding to
		// their list of devices..
	
		
        new SignInResource().addDevice(User.geteMailAddress(), User.getDeviceId(), User.getDeviceType());

		Verify v = new Verify(UUID.randomUUID().toString(),
				User.getPartnerId(), User.geteMailAddress(),
				User.getDateRegistered());

		super.save(v, v.getVerifyID(), v.getPartnerId());

		Mailer mail = new Mailer();
		mail.sendMail(
				User.geteMailAddress(),
				"registration e-mail.",
				"Thank you for registering!  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/user/"
						+ v.getPartnerId()
						+ "/"
						+ v.getVerifyID()
						+ "\">clicking here.</a>");

		Status s = new Status();
		s.setStatus("Success");
		s.setDescription("User " + User.geteMailAddress()
				+ " successfully signed up");
		s.setId(User.geteMailAddress());
		return Response.status(201).entity(s).build();
	}


}
