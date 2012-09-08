package com.MobMonkey.Resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/error")
public class Error {

	public Error() {
	}

	@GET
	@Path("/unauthorized")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getError() {

		return Response.status(401).entity("Unauthorized attempt").build();
	}

}
