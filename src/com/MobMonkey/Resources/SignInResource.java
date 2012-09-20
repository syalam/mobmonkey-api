package com.MobMonkey.Resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/signin")
public class SignInResource extends ResourceHelper {

	public SignInResource(){
		super();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response SignInInJSON(){
		//TODO
		
		return Response.ok().build();
		
		
	}
}
