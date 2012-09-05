package com.MobMonkey.Resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Path("/vod")
public class VODResource {

	@POST
	@Consumes("application/json")
	public void post() {

		AmazonS3Client test = new AmazonS3Client();
		ObjectMetadata objmeta = new ObjectMetadata();

		PutObjectRequest mediaFile = new PutObjectRequest("mobmonkeyvod", null,
				null, objmeta);
		test.putObject(mediaFile);

	}

}
