package com.MobMonkey.Resources;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.LocationProvider;

@Path("/locationprovider")
public class LocationProviderResource extends ResourceHelper {

	public LocationProviderResource() {
		super();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createLocationProviderInJSON(LocationProvider locprov) {
		try {
			locprov.setProviderId(UUID.randomUUID().toString());
			super.mapper().save(locprov);
		} catch (Exception exc) {
			return Response
					.status(500)
					.entity("Error creating location provider: "
							+ exc.getMessage() + ".").build();
		}

		return Response.ok().entity(locprov).build();
	}

}
