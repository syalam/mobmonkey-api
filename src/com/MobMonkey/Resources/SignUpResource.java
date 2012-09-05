package com.MobMonkey.Resources;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import com.MobMonkey.Models.SignUp;
import com.MobMonkey.Helpers.*;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;

@Path("/signup")
public class SignUpResource {

	private AWSCredentials credentials;
	private AmazonDynamoDBClient ddb;
	private DynamoDBMapper mapper;

	public SignUpResource() {

		try {
			credentials = new PropertiesCredentials(getClass().getClassLoader()
					.getResourceAsStream("AwsCredentials.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ddb = new AmazonDynamoDBClient(credentials);
		ddb.setEndpoint("https://dynamodb.us-west-1.amazonaws.com", "dynamodb",
				"us-west-1");

		mapper = new DynamoDBMapper(ddb);

	}

	@GET
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SignUp> getUserInJSON() {

		DynamoDBScanExpression scan = new DynamoDBScanExpression();

		PaginatedScanList<SignUp> users = mapper.scan(SignUp.class, scan);
				
		return users.subList(0, users.size());
	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUserInJSON(SignUp signup) {

	
		mapper.save(signup);

		Mailer mail = new Mailer();
		mail.sendMail(signup.geteMailAddress());
		
		String result = "User successfully signed up";
		return Response.status(201).entity(result).build();

	}
}
