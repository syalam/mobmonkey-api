package com.MobMonkey.Resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.Notification;
import com.MobMonkey.Models.Status;

@Path("/notification")
public class NotificationResource extends ResourceHelper {

	public NotificationResource(){
		super();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNotificationInJSON(@QueryParam("freqInDays") Integer freqInDays, @Context HttpHeaders headers, Notification n){
		String key = n.getLocationId() + ":" + n.getProviderId();
		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0)
				.toLowerCase();
		
		
		//frequency
		if(freqInDays == null){
			return Response.status(500).entity(new Status("Failure", "You must include the query parameter freqInDays to specify the frequency of the notifications", "")).build();
		}
		n.setLocprovId(key);
	    Long frequencyInMS = (freqInDays * 24L * 60L * 60L * 1000L);
		n.setFrequency(frequencyInMS.toString());
		n.seteMailAddress(eMailAddress);
		
        super.save(n, n.geteMailAddress(), n.getLocationId());
		
		return Response.ok().entity(new Status("Success", "Added notification.", "")).build();
		
		
	}
	
}
