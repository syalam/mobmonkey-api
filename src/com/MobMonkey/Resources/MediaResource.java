package com.MobMonkey.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Models.Media;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Path("/media")
public class MediaResource extends ResourceHelper {


	public MediaResource() {
		super();
	}

	@POST
	@Path("/image")
	@Consumes("application/json")
	public Response uploadImageInJSON(Media media) throws IOException {

		media.setId(UUID.randomUUID().toString());
		String keyName = "img-" + media.getId() + ".png";
		byte[] btDataFile = new sun.misc.BASE64Decoder().decodeBuffer(media
				.getMediaData());
		ByteArrayInputStream bais = new ByteArrayInputStream(btDataFile);
		
		ObjectMetadata objmeta = new ObjectMetadata();
		objmeta.setContentLength(btDataFile.length);
		objmeta.setContentType("image/png");
		
		objmeta.setExpirationTimeRuleId("images");
		
		PutObjectRequest putObjectRequest = new PutObjectRequest(
				"mobmonkeyimages", keyName , bais, objmeta);
		putObjectRequest.setRequestCredentials(super.credentials());
		
		putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
	
		super.s3cli().putObject(putObjectRequest);
	

		super.mapper().save(media);
		String result = "Successfully uploaded image. https://s3-us-west-1.amazonaws.com/mobmonkeyimages/" + keyName;
		return Response.status(201).entity(result).build();

	}
	
	

}
