package com.MobMonkey.Resources;

import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Models.CheckIn;

@Path("/checkin")
public class CheckInResource extends ResourceHelper {

	public CheckInResource(){
		super();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createCheckInInJSON(CheckIn c, @Context HttpHeaders headers){
		
		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0);
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId").get(0);
		c.seteMailAddress(eMailAddress);
		c.setPartnerId(partnerId);
		c.setDateCheckedIn(new Date());
		try{
		super.mapper().save(c);
		}catch(Exception exc){
			return Response.status(500).entity("An error has occured").build();
		}
		
		//so i have checked in the user at a specific x,y
		//i should check to see if there are any requests in the area
		ArrayList<String> reqsNearBy = new Locator().findRequestsNearBy(c.getLatitude(), c.getLongitude());
		
		return Response.ok().entity(reqsNearBy.size() + " requests near by.").build();
		
	}
}
