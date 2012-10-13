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

		User result = super.mapper().load(User.class, eMailAddress, partnerId);
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

		User.setPartnerId(partnerId);
		User.setVerified(false);
		User.setDateRegistered(new Date());
		super.mapper().save(User);
		// Let's save the user's device Id as well
		// TODO in signin API send the users deviceId so we will keep adding to
		// their list of devices..

		Device d = new Device();
		d.seteMailAddress(User.geteMailAddress());
		d.setDeviceId(User.getDeviceId());
		d.setDeviceType(User.getDeviceType());
		super.mapper().save(d);

		Verify v = new Verify(UUID.randomUUID().toString(),
				User.getPartnerId(), User.geteMailAddress(),
				User.getDateRegistered());

		super.mapper().save(v);

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

	@POST
	@Path("/user/oauth/{oauthprovider}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUserWithOauthInJSON(Oauth ou,
			@PathParam("oauthprovider") String oauthprov,
			@Context HttpHeaders headers) {
		// TODO need to create list of valid oauthproviders: facebook, twitter,
		// etc.
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId")
				.get(0).toLowerCase();

		// We should check to see if we have a user with that email address
		// already assigned
		if (super.mapper().count(
				User.class,
				new DynamoDBQueryExpression(new AttributeValue().withS(ou
						.geteMailAddress()))) > 0) {
			// we have a user with this email address.
			// lets just add the Oauth token to the db
			try {
				ou.seteMailVerified(false);
				super.mapper().save(ou);

				Verify v = new Verify(UUID.randomUUID().toString(), partnerId,
						ou.geteMailAddress(), new Date());

				super.mapper().save(v);

				Mailer mail = new Mailer();
				mail.sendMail(
						ou.geteMailAddress(),
						"registration e-mail.",
						"Thank you for signing into MobMonkey using your "
								+ oauthprov
								+ " account.  Please validate that this is your email by <a href=\"http://api.mobmonkey.com/rest/verify/user/"
								+ v.getPartnerId() + "/" + v.getVerifyID()
								+ "\">clicking here.</a>");

				return Response
						.ok()
						.entity(new Status("Success", "Your " + oauthprov
								+ " account is linked to "
								+ ou.geteMailAddress()
								+ ". Check your email to verify your address.",
								"")).build();
			} catch (Exception exc) {
				return Response
						.ok()
						.entity(new Status("Failure",
								"Unable to create your MobMonkey account", ""))
						.build();
			}
		} else {
			try {
				User u = new User();
				u.setPartnerId(partnerId);
				u.seteMailAddress(ou.geteMailAddress());
				u.setVerified(false);

				super.mapper().save(u);
			} catch (Exception exc) {
				return Response
						.ok()
						.entity(new Status("Failure",
								"Unable to create your MobMonkey account", ""))
						.build();
			}
			try {
				ou.seteMailVerified(false);
				super.mapper().save(ou);
			} catch (Exception exc) {
				return Response
						.ok()
						.entity(new Status("Failure",
								"Unable to create your MobMonkey account", ""))
						.build();
			}

			Verify v = new Verify(UUID.randomUUID().toString(), partnerId,
					ou.geteMailAddress(), new Date());
			v.setOauthToken(ou.getoAuthToken());

			super.mapper().save(v);

			Mailer mail = new Mailer();
			mail.sendMail(
					ou.geteMailAddress(),
					"registration e-mail.",
					"Thank you for signing into MobMonkey using your "
							+ oauthprov
							+ " account.  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/user/"
							+ v.getPartnerId() + "/" + v.getVerifyID() + "/"
							+ ou.getoAuthToken() + "\">clicking here.</a>");

			return Response
					.ok()
					.entity(new Status("Success", "Your " + oauthprov
							+ " account is linked to " + ou.geteMailAddress()
							+ ". Check your email to verify your address.", ""))
					.build();

		}

	}
}
