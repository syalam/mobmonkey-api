package com.MobMonkey.Resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.SearchHelper;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.Status;

@Path("/search")
public class SearchResource extends ResourceHelper {

	public SearchResource() {
		super();

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findLocationsInJSON(Location loc) {

		if (loc.getLatitude() != null && loc.getLongitude() != null) {
			// TODO validate lat/long with regex
			List<Location> locations = SearchHelper.getLocationsByGeo(loc);
			return Response.ok().entity(locations).build();
		}
		if (loc.getLocality() != null && loc.getRegion() != null
				&& loc.getPostcode() != null) {
			// TODO validate postcode with regex, make sure locality and region
			// are sane
			List<Location> locations = SearchHelper.getLocationsByAddress(loc);
			return Response.ok().entity(locations).build();

		}
		return Response
				.status(500)
				.entity(new Status(
						"Failure",
						"You need to specify either (lat & long) OR (address, locality, region & zip)",
						"")).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/glob")
	public Response findLotsOfLocationsInJSON(List<Location> locs) {

		List<Location> locations = new ArrayList<Location>();
		for (Location loc : locs) {
			if (loc.getLatitude() != null && loc.getLongitude() != null) {
				// TODO validate lat/long with regex
				List<Location> locList = SearchHelper.getLocationsByGeo(loc);
				locations.addAll(locList);
				
			}
			if (loc.getLocality() != null && loc.getRegion() != null
					&& loc.getPostcode() != null) {
				// TODO validate postcode with regex, make sure locality and
				// region are sane
				List<Location> locList = SearchHelper
						.getLocationsByAddress(loc);
				locations.addAll(locList);

			}
		}
		
		
		return Response
				.ok()
				.entity(locations).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocationsInJSON() {
		return Response.ok().entity(new Location()).build();
	}
}
