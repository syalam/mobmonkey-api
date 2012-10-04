package com.MobMonkey.Resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Helpers.Locator;
import com.MobMonkey.Models.Bookmark;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.Trending;
import com.MobMonkey.Models.User;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ConditionalCheckFailedException;

@Path("/bookmarks")
public class BookmarkResource extends ResourceHelper {

	public BookmarkResource() {
		super();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBookmarksInJSON(@Context HttpHeaders headers) {
		User user = super.getUser(headers);
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(user.geteMailAddress()));

		List<Location> results = new ArrayList();

		PaginatedQueryList<Bookmark> bookmarks = super.mapper().query(
				Bookmark.class, queryExpression);
		for (Bookmark b : bookmarks) {
			String[] locprov = b.getLocprovId().split(":");
			b.setLocationId(locprov[0]);
			b.setProviderId(locprov[1]);

			Location loc = new Locator().reverseLookUp(b.getProviderId(),
					b.getLocationId());
			results.add(loc);
		}
		return Response.ok().entity(results).build();

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createBookmarkInJSON(@Context HttpHeaders headers,
			Bookmark b) {
		User user = super.getUser(headers);

		b.setLocprovId(b.getLocationId() + ":" + b.getProviderId());

		b.seteMailAddress(user.geteMailAddress());
		try {
			super.mapper().save(b);
		} catch (ConditionalCheckFailedException exc) {
			return Response
					.status(500)
					.entity(new Status(
							"Failure",
							"This book mark is already present in the database",
							"")).build();
		}
		Trending t = new Trending();
		t.setType("Bookmark");
		t.setLocationId(b.getLocationId());
		t.setProviderId(b.getProviderId());
		t.setTimeStamp(new Date());
		super.mapper().save(t);

		return Response.ok()
				.entity(new Status("Success", "Added bookmark.", "")).build();

	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteBookmarkInJSON(@Context HttpHeaders headers,
			Bookmark b) {

		User user = super.getUser(headers);

		b.seteMailAddress(user.geteMailAddress());
		b.setLocprovId(b.getLocationId() + ":" + b.getProviderId());

		try {
			super.mapper().delete(b);
			return Response.ok()
					.entity(new Status("Success", "Deleted bookmark.", "")).build();
		} catch (Exception exc) {
			return Response.status(500)
					.entity(new Status("Failure", "Unable to delete bookmark.", "")).build();
		}
	

	}

}
