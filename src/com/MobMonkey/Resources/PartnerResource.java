package com.MobMonkey.Resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Helpers.Mailer;
import com.MobMonkey.Models.Partner;
import com.MobMonkey.Models.Verify;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;

@Path("/partner")
public class PartnerResource extends ResourceHelper {

	public PartnerResource() {
		super();
	}

	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Partner> getPartnerInJSON() {
		
		DynamoDBScanExpression scan = new DynamoDBScanExpression();

		PaginatedScanList<Partner> partners = super.mapper().scan(Partner.class,
				scan);

		return partners.subList(0, partners.size());
	}

	@Path("/{partnerid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Partner getPartnerInJSON(@PathParam("partnerid") String partnerId) {

		Partner p = (Partner) super.load(Partner.class, partnerId.trim());

		return p;
	}
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPartnerInJSON(Partner p) {
		
	//TODO - validation, email, etc
		try {
			p.setPartnerId(UUID.randomUUID().toString());
			p.setEnabled(false);
			p.setLastActivity(new Date());
			super.save(p, p.getPartnerId());
		} catch (Exception e) {
			return Response.status(500).entity(e.toString()).build();
		}
		
		Verify v = new Verify(UUID.randomUUID().toString(), p.getPartnerId(), p.getEmailAddress(), p.getDateRegistered());
	
		super.save(v, v.getVerifyID(), v.getPartnerId());

		Mailer mail = new Mailer();
		mail.sendMail(p.getEmailAddress(), "registration e-mail.", "Thank you for registering as a partner!  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/partner/" + v.getPartnerId() + "/" + v.getVerifyID() + "\">clicking here.</a>");

		String result = "Successfully created partner: " + p.getPartnerId();

		return Response.status(201).entity(result).build();
	}

}
