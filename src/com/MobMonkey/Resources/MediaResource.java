package com.MobMonkey.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Helpers.ApplePNSHelper;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.Media;
import com.MobMonkey.Models.RequestMedia;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;
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
	public Response uploadImageInJSON(Media media, @Context HttpHeaders headers)
			throws IOException {

		String username = headers.getRequestHeader("MobMonkey-user").get(0);
		media.setMediaId(UUID.randomUUID().toString());
		media.seteMailAddress(username);
		media.setRequestId(media.getRequestId());

		// Save the file to S3
		String keyName = "img-" + media.getMediaId() + ".png";
		byte[] btDataFile = DatatypeConverter.parseBase64Binary(media
				.getMediaData());
		ByteArrayInputStream bais = new ByteArrayInputStream(btDataFile);
		ObjectMetadata objmeta = new ObjectMetadata();
		objmeta.setContentLength(btDataFile.length);
		objmeta.setContentType("image/png");
		objmeta.setExpirationTimeRuleId("images");
		PutObjectRequest putObjectRequest = new PutObjectRequest(
				"mobmonkeyimages", keyName, bais, objmeta);
		putObjectRequest.setRequestCredentials(super.credentials());
		putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
		super.s3cli().putObject(putObjectRequest);

		super.mapper().save(media);

		RequestMedia rm = super.mapper().load(RequestMedia.class,
				media.getRequestId());
		rm.setRequestId(media.getRequestId());
		rm.setRequestFulfilled(true);
		super.mapper().save(rm);

		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(rm.geteMailAddress()));

		List<Device> scanResult = super.mapper().query(Device.class,
				queryExpression);
		
		
		String[] deviceIds = new String[scanResult.size()];
		
		for(int i = 0; i < deviceIds.length; i++){
			deviceIds[i] = scanResult.get(i).getDeviceId().toString();
		}

		ApplePNSHelper.send(deviceIds,
				"https://s3-us-west-1.amazonaws.com/mobmonkeyimages/"
						+ keyName);
		
		String result = "Successfully uploaded image. https://s3-us-west-1.amazonaws.com/mobmonkeyimages/"
				+ keyName;
		return Response.status(201).entity(result).build();

	}

}
