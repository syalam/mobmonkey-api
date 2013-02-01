package com.MobMonkey.Resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Models.Status;

@Path("/error")
public class Error {

	public Error() {
	}

	@GET
	@Path("/unauthorized")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getError() {

		Status s = new Status();
		s.setStatus("Unauthorized or need to confirm email");
		s.setDescription("Please login to MobMonkey using authorized credentials or confirm your email address");
		return Response.status(401).entity(s).build();
	}

}
