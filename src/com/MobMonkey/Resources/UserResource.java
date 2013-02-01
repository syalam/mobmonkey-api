	package com.MobMonkey.Resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.User;
import com.MobMonkey.Models.Verify;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.Oauth;
import com.MobMonkey.Helpers.*;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;

@Path("/signup")
public class UserResource extends ResourceHelper {

	public UserResource() {
		super();
	}

	@GET
	@Path("/user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInJSON(@Context HttpHeaders headers) {
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId")
				.get(0).toLowerCase();
		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0)
				.toLowerCase();

		User result = (User) super.load(User.class, eMailAddress, partnerId);
		result.setPassword(null);
		return Response.ok().entity(result).build();
	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUserInJSON(User User, @Context HttpHeaders headers) {

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

		boolean validEmail = new EmailValidator().validate(User.geteMailAddress());
		if(!validEmail){
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
