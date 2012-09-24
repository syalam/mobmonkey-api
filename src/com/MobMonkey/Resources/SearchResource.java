package com.MobMonkey.Resources;

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


@Path("/search")
public class SearchResource extends ResourceHelper {


	public SearchResource() {
		super();
		
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findLocationsInJSON(Location loc) {
		
		List<Location> locations = SearchHelper.getLocationsByGeo(loc);
	
		return Response.ok().entity(locations).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocationsInJSON(){
		return Response.ok().entity(new Location()).build();
	}
}
