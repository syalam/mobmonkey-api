package com.MobMonkey.Resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Helpers.Mailer;
import com.MobMonkey.Models.Partner;

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

		

		Partner p = super.mapper().load(Partner.class, partnerId.trim());

		return p;
	}
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPartnerInJSON(Partner p) {
		//TODO - validation, email, etc
		try {
			p.setpartnerId(UUID.randomUUID().toString());
			p.setEnabled(true);
			p.setLastActivity(new Date());
			super.mapper().save(p);
		} catch (Exception e) {
			return Response.status(500).entity(e.toString()).build();
		}
		Mailer mail = new Mailer();
		mail.sendMail(p.getEmail(), "partner ID created.", "Congratulations, you now have a partner ID setup with MobMonkey.  You will use this ID to access the MobMonkey API endpoints.");

		String result = "Successfully created partner: " + p.getpartnerId();

		return Response.status(201).entity(result).build();
	}

}
