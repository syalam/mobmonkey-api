package com.MobMonkey.Resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.Location;
import com.MobMonkey.Helpers.FactualHelper;

@Path("/search")
public class SearchResource extends ResourceHelper {


	public SearchResource() {
		super();
		
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String findLocationsInJSON(Location loc) {
		FactualHelper factual = new FactualHelper();
		
	
		return factual.GeoFilter(loc);
	}
}
