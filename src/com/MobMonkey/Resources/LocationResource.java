package com.MobMonkey.Resources;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.LocationProvider;

@Path("/location")
public class LocationResource extends ResourceHelper {

	public LocationResource() {
		super();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createLocationInJSON(Location loc) {

		LocationProvider locprov = super.mapper().load(LocationProvider.class,
				loc.getProviderId());

		// Right now we are only accepting new locations for MobMonkey
		if (!locprov.getName().toLowerCase().equals("mobmonkey"))
			return Response
					.status(500)
					.entity("Cannot accept location data for the provider you specified.")
					.build();

		try {
			loc.setLocationId(UUID.randomUUID().toString());
			super.mapper().save(loc);

		} catch (Exception exc) {
			return Response
					.status(500)
					.entity("Error creating the location specified: "
							+ exc.getMessage().toString() + ".").build();
		}

		return Response.ok().entity(loc).build();

	}
}
