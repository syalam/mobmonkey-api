package com.MobMonkey.Resources;

import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.User;

@Path("/requestmedia")
public class RequestMediaResource extends ResourceHelper {

	public RequestMediaResource() {
		super();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public RequestMedia getRequestMediaInJSON() {

		return new RequestMedia();

	}

	@POST
	@Path("/{mediaType}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createRequestMediaInJSON(
			@PathParam("mediaType") String mediaType, RequestMedia r) {

		// Get username & PartnerId
		String username = r.geteMailAddress();
		String partnerId = r.getPartnerId();

		// Has user verified their email?
		User user = super.mapper().load(User.class, username, partnerId);
		try {
			if (!user.isVerified()) {
				return Response.status(401)
						.entity("User has not verified their email address")
						.build();
			}
		} catch (Exception e) {
			return Response.status(500)
					.entity("User was not found in the MobMonkey database.")
					.build();
		}

		r.setRequestId(UUID.randomUUID().toString());
		switch (mediaType) {
		case "image":
			// saving the request to DB
			r.setRequestType(1);
			super.mapper().save(r);
			return Response
					.ok()
					.entity("RequestID: " + r.getRequestId())
					.build();

		case "video":
			r.setRequestType(2);
			super.mapper().save(r);
			return Response
					.ok()
					.entity("RequestID: " + r.getRequestId())
					.build();

		default:
			return Response.status(500)
					.entity(mediaType + " is not supported.").build();
		}
	}

}
