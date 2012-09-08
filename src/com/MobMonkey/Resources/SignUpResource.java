package com.MobMonkey.Resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import com.MobMonkey.Models.SignUp;
import com.MobMonkey.Models.Verify;
import com.MobMonkey.Helpers.*;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import javax.enterprise.context.RequestScoped;

@Path("/signup")
@RequestScoped
public class SignUpResource extends ResourceHelper {

	public SignUpResource() {
		super();
	}

	@GET
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SignUp> getUserInJSON() {

		
		
		DynamoDBScanExpression scan = new DynamoDBScanExpression();

		PaginatedScanList<SignUp> users = super.mapper().scan(SignUp.class,
				scan);

		return users.subList(0, users.size());
	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUserInJSON(SignUp signup) {

		//TODO - update partnerID with last activity
		
		
		if(null == signup.getPassword() || null == signup.geteMailAddress())
		{
			return Response.status(500).entity("Email and password are required fields").build();
		}
		
		signup.setVerified(false);
		signup.setDateRegistered(new Date());
		super.mapper().save(signup);
		
		Verify v = new Verify(UUID.randomUUID().toString(), signup.getPartnerId(), signup.geteMailAddress(), signup.getDateRegistered());
	
		super.mapper().save(v);

		Mailer mail = new Mailer();
		mail.sendMail(signup.geteMailAddress(), "registration e-mail.", "Thank you for registering!  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/" + v.getPartnerId() + "/" + v.getVerifyID() + "\">clicking here.</a>");

		String result = "User successfully signed up";
		return Response.status(201).entity(result).build();

	}
}
