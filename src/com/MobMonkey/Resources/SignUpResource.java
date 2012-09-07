package com.MobMonkey.Resources;

import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import com.MobMonkey.Models.SignUp;
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

		PaginatedScanList<SignUp> users = super.mapper().scan(SignUp.class, scan);
				
		return users.subList(0, users.size());
	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUserInJSON(SignUp signup) {

		super.mapper().save(signup);

		Mailer mail = new Mailer();
		mail.sendMail(signup.geteMailAddress());
		
		String result = "User successfully signed up";
		return Response.status(201).entity(result).build();

	}
}
