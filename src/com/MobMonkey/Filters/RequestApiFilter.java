package com.MobMonkey.Filters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.MobMonkey.Models.Oauth;
import com.MobMonkey.Models.Partner;
import com.MobMonkey.Models.User;
import com.MobMonkey.Resources.ResourceHelper;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class RequestApiFilter extends ResourceHelper implements
		ContainerRequestFilter {

	static Logger logger = Logger.getRootLogger();

	@Override
	public ContainerRequest filter(ContainerRequest req) {

		logger.debug(">>>>>>>>>>>>>>>>>>   	RequestApiFilter.filter(...)");

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

		} else {
			logger.debug("Unauthorized");
		}
		return req;
	}

	private boolean Authorize(ContainerRequest req) {

		// Grab the authentication values from the request header
		String partnerId = req.getHeaderValue("MobMonkey-partnerId");
		String eMailAddress = req.getHeaderValue("MobMonkey-user");
		String password = req.getHeaderValue("MobMonkey-auth");
		String oauthProviderUserName = req
				.getHeaderValue("OauthProviderUserName");
		// mString oauthToken = req.getHeaderValue("OauthToken");
		String oauthProvider = req.getHeaderValue("OauthProvider");

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
			// TODO Cache this
			Partner p = (Partner) super.load(Partner.class, partnerId.trim()
					.toString());
			if (p.equals(null) || !p.isEnabled()) {
				return false; // Quickly deny the request
			} else {

				// We have a valid partner ID, but maybe we are signing up a new
				// user
				// If that's the case then we will let them through without
				// user:pass creds.
				if ((req.getRequestUri().getPath().toLowerCase()
						.matches(".*/rest/user.*$") && req.getMethod()
						.toLowerCase().equals("put"))
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
			if (null != oauthProvider) {
				// TODO Cache this
				Oauth ou = (Oauth) super.load(Oauth.class, oauthProvider,
						oauthProviderUserName);

				if (ou != null) {
					if (ou.iseMailVerified() == false) {
						return false;
					}

					InBoundHeaders in = new InBoundHeaders();
					in.putAll(req.getRequestHeaders());

					List<String> eMailAddressHeader = new ArrayList<String>();
					List<String> providerUserName = new ArrayList<String>();
					List<String> oAuthPass = new ArrayList<String>();
					try {
						eMailAddressHeader.add(ou.geteMailAddress());
						providerUserName.add(eMailAddress);
					} catch (Exception exc) {
						return false;
					}
					oAuthPass.add("092C317848223D4810468E8EAAF280FA");
					in.put("MobMonkey-user", eMailAddressHeader);
					in.put("ProviderUserName", providerUserName);
					in.put("MobMonkey-auth", oAuthPass);
					req.setHeaders(in);
					return true; // we have a valid oauthtoken !!
				}

			}

			// No Oauth token.. lets see if we have a user & pass
			// Pull the user information
			// TODO Cache this
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
