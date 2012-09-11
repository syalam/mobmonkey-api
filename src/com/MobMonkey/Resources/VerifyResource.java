package com.MobMonkey.Resources;

import java.util.Date;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Helpers.Mailer;
import com.MobMonkey.Models.Partner;
import com.MobMonkey.Models.User;
import com.MobMonkey.Models.Verify;

@Path("/verify")
public class VerifyResource extends ResourceHelper {

	public VerifyResource() {
		super();
	}

	@GET
	@Path("/user/{partnerId}/{verifyId}")
	public Response verifyUserID(@PathParam("partnerId") String partnerId,
			@PathParam("verifyId") String verifyId) {
		try {
			Verify v = super.mapper().load(Verify.class, verifyId, partnerId);

			User u = super.mapper().load(User.class, v.geteMailAddress(),
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
	
	@GET
	@Path("/partner/{partnerId}/{verifyId}")
	public Response verifyPartnerID(@PathParam("partnerId") String partnerId,
			@PathParam("verifyId") String verifyId) {
		Partner p = null;
		
		try {
			Verify v = super.mapper().load(Verify.class, verifyId, partnerId);

			p = super.mapper().load(Partner.class, v.getPartnerId());

			p.setEnabled(true);
			super.mapper().save(p);
			
			v.setVerifyID(verifyId);
			v.setRecvDate(new Date());
			super.mapper().save(v);
		} catch (Exception e) {
			return Response.status(500).entity("TODO - HTML ERROR RESPONSE").build();
		}
		
		Mailer mail = new Mailer();
		mail.sendMail(p.getEmailAddress(), "confirmation e-mail.", "Thank you for confirming your e-mail address! You may now use the full functionality of MobMonkey's REST API's.</h1><p>Your MobMonkey API Key is: <b>" + partnerId.toString() + "</b>.<br>You will need to use this ID when invoking the MobMonkey RESTful API's.");

		
		return Response.status(200).entity("<html><head><title>Verification success.</title></head><body><center><h1>Thank you for registering! You may now use the full functionality of MobMonkey's REST API's.</h1><p>Your MobMonkey API Key is: <b>" + partnerId.toString() + "</b>.<br>You will need to use this ID when invoking the MobMonkey RESTful API's.</center></body></html>").build();

	}
	
}
