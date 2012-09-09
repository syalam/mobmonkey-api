package com.MobMonkey.Resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import com.MobMonkey.Models.User;
import com.MobMonkey.Models.Verify;
import com.MobMonkey.Helpers.*;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import javax.enterprise.context.RequestScoped;

@Path("/User")
@RequestScoped
public class UserResource extends ResourceHelper {

	public UserResource() {
		super();
	}

	@GET
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getUserInJSON() {

		
		
		DynamoDBScanExpression scan = new DynamoDBScanExpression();

		PaginatedScanList<User> users = super.mapper().scan(User.class,
				scan);

		return users.subList(0, users.size());
	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUserInJSON(User User) {

		//TODO - update partnerID with last activity
		
		
		if(null == User.getPassword() || null == User.geteMailAddress())
		{
			return Response.status(500).entity("Email and password are required fields").build();
		}
		
		User.setVerified(false);
		User.setDateRegistered(new Date());
		super.mapper().save(User);
		
		Verify v = new Verify(UUID.randomUUID().toString(), User.getPartnerId(), User.geteMailAddress(), User.getDateRegistered());
	
		super.mapper().save(v);

		Mailer mail = new Mailer();
		mail.sendMail(User.geteMailAddress(), "registration e-mail.", "Thank you for registering!  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/" + v.getPartnerId() + "/" + v.getVerifyID() + "\">clicking here.</a>");

		String result = "User successfully signed up";
		return Response.status(201).entity(result).build();

	}
}
