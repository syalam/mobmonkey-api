package com.MobMonkey.Resources;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.Bookmark;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.User;

@Path("/signout")
public class SignOutResource extends ResourceHelper {

	public SignOutResource() {
		super();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{type}/{deviceId}")
	public Response SignOutInJSON(@Context HttpHeaders headers,
			@PathParam("deviceId") String deviceId,
			@PathParam("type") String type) {
		User user = super.getUser(headers);
		Device d = new Device();
		d.seteMailAddress(user.geteMailAddress());
		if (type.toLowerCase().equals("ios")) {
			d.setDeviceType("iOS");
		} else if (type.toLowerCase().equals("android")) {
			d.setDeviceType("Android");
		} else {
			return Response
					.status(500)
					.entity(new Status("Failure",
							"You must specify a device type (Android or iOS)",
							"")).build();
		}

		d.setDeviceId(deviceId);

		super.mapper().delete(d);

		// also delete cached list

		super.deleteFromCache("DEV" + user.geteMailAddress());

		return Response.ok()
				.entity(new Status("Success", "Successfully signed out", ""))
				.build();

	}

}
