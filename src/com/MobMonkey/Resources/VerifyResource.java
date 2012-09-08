package com.MobMonkey.Resources;

import java.util.Date;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Models.SignUp;
import com.MobMonkey.Models.Verify;

@Path("/verify")
public class VerifyResource extends ResourceHelper {

	public VerifyResource() {
		super();
	}

	@GET
	@Path("/{partnerId}/{verifyId}")
	public Response verifyID(@PathParam("partnerId") String partnerId,
			@PathParam("verifyId") String verifyId) {
		try {
			Verify v = super.mapper().load(Verify.class, verifyId, partnerId);

			SignUp u = super.mapper().load(SignUp.class, v.geteMailAddress(),
					v.getPartnerId());

			u.setVerified(true);
			super.mapper().save(u);

			v.setVerifyID(verifyId);
			v.setRecvDate(new Date());
			super.mapper().save(v);
		} catch (Exception e) {
			return Response.status(500).entity("TODO - HTML ERROR RESPONSE").build();
		}
		
		return Response.status(200).entity("<html><head><title>Verification success.</title></head><body><center><h1>Thank you for registering! You may now use the full functionality of MobMonkey.</h1></center></body></html>").build();

	}
}
