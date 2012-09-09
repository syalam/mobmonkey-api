package com.MobMonkey.Filters;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.MobMonkey.Models.Partner;
import com.MobMonkey.Models.User;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.auth.AWSCredentials;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class RequestApiFilter implements ContainerRequestFilter {
	private AWSCredentials credentials;
	private AmazonDynamoDBClient ddb;
	private DynamoDBMapper mapper;

	public RequestApiFilter() {
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

	@Override
	public ContainerRequest filter(ContainerRequest req) {
		// Check to see if the request has the correct authorization info.
		boolean authorized = Authorize(req);
		if (!authorized) {
			//Basically if they arent authorized I send them to an error resource pool.
			//I plan to implement custom error messages based on what could have possibly gone wrong
			//but for now, everything is just 401 Unauthorized.
			req.setMethod("GET");
			URI u = req.getRequestUri();
			try {
				req.setUris(
						req.getBaseUri(),
						new URI(u.getScheme(), u.getUserInfo(), u.getHost(), u
								.getPort(), u.getPath().replaceFirst("rest/.+",
								"rest/error/unauthorized"), u.getQuery(), u
								.getFragment()));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return req;
	}

	private boolean Authorize(ContainerRequest req) {

		// Grab the authentication values from the request header
		String partnerId = req.getHeaderValue("MobMonkey-partnerId");
		String eMailAddress = req.getHeaderValue("MobMonkey-user");
		String password = req.getHeaderValue("MobMonkey-auth");

		//If the request path is to verify an email, let them on through
		if (req.getRequestUri().getPath().toLowerCase()
				.matches(".+/rest/verify.*$")) {
			return true;
		}
		
		//If the request path is to signup a partner, I let them through for now.
		if (req.getRequestUri().getPath().toLowerCase()
				.matches(".+/rest/partner.*$")) {
			return true;
		}

		try {

			// See if we have a valid partner ID.
			Partner p = mapper.load(Partner.class, partnerId.trim().toString());
			if (p.equals(null)) {
				return false;  // Quickly deny the request
			} else {

				// We have a valid partner ID, but maybe we are signing up a new user
				// If that's the case then we will let them through without user:pass creds.
				if (req.getRequestUri().getPath().toLowerCase()
						.matches(".+/rest/signup/user$")) {
					return true;
				}

			}
		}
		//Something happened, reject!
		catch (Exception e) {
			return false;
		}

		try {
			// Pull the user information
			User user = mapper.load(User.class, eMailAddress.trim(), partnerId.trim());

			//User doesnt exist, reject the request.
			if (null == user) {
				return false;
			}

			//Lets check to see if their password matches what we have in the DB.
			if (!password.equals(user.getPassword())) {
				return false;
			}

		} 
		// Something happened, abort!!
		catch (Exception e) {
			return false;
		}
		
		// Now, does the user have access to the resource?
		//TODO - Lock down administrative resources, like getting user lists
		
		
		
		// Passed all my tests?  I'll allow it.
		return true;

	}

}
