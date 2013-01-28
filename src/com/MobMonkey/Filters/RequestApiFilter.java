package com.MobMonkey.Filters;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.MobMonkey.Models.Partner;
import com.MobMonkey.Models.User;
import com.MobMonkey.Models.Oauth;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.auth.AWSCredentials;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class RequestApiFilter extends ResourceHelper implements ContainerRequestFilter{
	private AWSCredentials credentials;
	private AmazonDynamoDBClient ddb;
	private DynamoDBMapper mapper;

	public RequestApiFilter() {
		super();
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
			// Basically if they arent authorized I send them to an error
			// resource pool.
			// I plan to implement custom error messages based on what could
			// have possibly gone wrong
			// but for now, everything is just 401 Unauthorized.
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
		String oauthToken = req.getHeaderValue("OauthToken");

		// If the request path is to verify an email, let them on through
		if (req.getRequestUri().getPath().toLowerCase()
				.matches(".*/rest/verify.*$")) {
			return true;
		}
		if (req.getRequestUri().getPath().toLowerCase()
				.matches(".*/rest/search/location$")) {
			return true;
		}

		// If the request path is to signup a partner, I let them through for
		// now.
		// TODO - I plan to clean up the exception rules to make it an iterative
		// list coming from some config file
		if (req.getRequestUri().getPath().toLowerCase()
				.matches(".*/rest/partner.*$")) {
			return true;
		}

		try {

			// See if we have a valid partner ID, and that it is enabled (User
			// verified email)
			//TODO Cache this
			Partner p = (Partner) super.load(Partner.class, partnerId.trim().toString());
			if (p.equals(null) || !p.isEnabled()) {
				return false; // Quickly deny the request
			} else {

				// We have a valid partner ID, but maybe we are signing up a new
				// user
				// If that's the case then we will let them through without
				// user:pass creds.
				if (req.getRequestUri().getPath().toLowerCase()
						.matches(".*/rest/signup/user.*$")
						|| req.getRequestUri().getPath().toLowerCase()
								.matches(".*/rest/signin.*$")) {
					return true;
				}

			}
		}
		// Something happened, reject!
		catch (Exception e) {
			return false;
		}

		try {
			// Before with auth the user using email and pass, lets see if we
			// have an oauth header
			if (null != oauthToken) {
				//TODO Cache this
				Oauth ou = (Oauth) super.load(Oauth.class, eMailAddress, oauthToken);

				if (ou != null) {
					InBoundHeaders in = new InBoundHeaders();
					in.putAll(req.getRequestHeaders());

					List<String> eMailAddressHeader = new ArrayList<String>();
					List<String> providerUserName = new ArrayList<String>();
					try {
						eMailAddressHeader.add(ou.geteMailAddress());
						providerUserName.add(eMailAddress);
					} catch (Exception exc) {
						return false;
					}
					in.put("MobMonkey-user", eMailAddressHeader);
					in.put("ProviderUserName", providerUserName);
					req.setHeaders(in);
					return true; // we have a valid oauthtoken !!
				}

			}

			// No Oauth token.. lets see if we have a user & pass
			// Pull the user information
			//TODO Cache this
			User user = (User) super.load(User.class, eMailAddress.trim(),
					partnerId.trim());

			// User doesnt exist, reject the request.
			if (null == user) {
				return false;
			}

			// Lets check to see if their password matches what we have in the
			// DB.

			if (!password.equals(user.getPassword())) {

				return false;
			}

		}
		// Something happened, abort!!
		catch (Exception e) {
			return false;
		}

		// Now, does the user have access to the resource?
		// TODO - Lock down administrative resources, like getting user lists

		// Passed all my tests? I'll allow it.
		return true;

	}
}
