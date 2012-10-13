package com.MobMonkey.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Helpers.ApplePNSHelper;
import com.MobMonkey.Models.AssignedRequest;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.Media;
import com.MobMonkey.Models.RecurringRequestMedia;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.Status;
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadImageInJSON(Media media, @Context HttpHeaders headers)
			throws IOException {

		Date now = new Date();
		String requestorEmail = "";
		String originalRequestor = "";
		String username = headers.getRequestHeader("MobMonkey-user").get(0);
        
		String mediaId = UUID.randomUUID().toString();
	    String mediaFileName = "img-" + mediaId + ".png";
		String mediaUrl = "https://s3-us-west-1.amazonaws.com/mobmonkeyimages/"
				+ mediaFileName; 
		// lets get the assigned request
		AssignedRequest assReq = super.mapper().load(AssignedRequest.class,
				username, media.getRequestId());
		if(assReq == null){
			return Response.status(500).entity(new Status("Failure", "The request ID specified is no longer assigned to you", "")).build();
		}
		requestorEmail = assReq.getRequestorEmail();
		originalRequestor = assReq.getRequestorEmail();
		
		if (media.getRequestType().equals("1")) {
			RecurringRequestMedia rrm = super.mapper().load(
					RecurringRequestMedia.class, media.getRequestId());
			if(rrm.equals(null)){
				super.mapper().delete(assReq);
				
				return Response.status(500).entity(new Status("Failure", "The request has been removed by the requestor.", "")).build();
			}
			rrm.setRequestId(media.getRequestId());
			rrm.setRequestFulfilled(true);
			rrm.setFulfilledDate(now);
			//TODO rrm.setMediaUrl
			requestorEmail = rrm.geteMailAddress();
			super.mapper().save(rrm);
		} else if (media.getRequestType().equals("0")) {
			RequestMedia rrm = super.mapper().load(RequestMedia.class,
					requestorEmail, media.getRequestId());
			if(rrm == null){
				super.mapper().delete(assReq);
				return Response.status(500).entity(new Status("Failure", "The request has been removed by the requestor", "")).build();
			}
			rrm.setRequestFulfilled(true);
			rrm.setFulfilledDate(now);
			rrm.setMediaUrl(mediaUrl);
			super.mapper().save(rrm);
		}

		
		//unique id for media
		media.setMediaId(mediaId);
	
		// Save the file to S3
	
		byte[] btDataFile = DatatypeConverter.parseBase64Binary(media
				.getMediaData());
		ByteArrayInputStream bais = new ByteArrayInputStream(btDataFile);
		ObjectMetadata objmeta = new ObjectMetadata();
		objmeta.setContentLength(btDataFile.length);
		objmeta.setContentType("image/png");
		objmeta.setExpirationTimeRuleId("images");
		PutObjectRequest putObjectRequest = new PutObjectRequest(
				"mobmonkeyimages", mediaFileName, bais, objmeta);
		putObjectRequest.setRequestCredentials(super.credentials());
		putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
		super.s3cli().putObject(putObjectRequest);

		media.setMediaURL(mediaUrl);

		// "1" = recurring

		media.seteMailAddress(username);
		media.setUploadedDate(now);
		media.setOriginalRequestor(originalRequestor);
		
		super.mapper().save(media);
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(requestorEmail));

		List<Device> scanResult = super.mapper().query(Device.class,
				queryExpression);

		String[] deviceIds = new String[scanResult.size()];

		for (int i = 0; i < deviceIds.length; i++) {
			deviceIds[i] = scanResult.get(i).getDeviceId().toString();
		}

		ApplePNSHelper.send(deviceIds, media.getMediaURL());

		
		//finally delete the assigned request
		super.mapper().delete(assReq);
		
		return Response
				.status(201)
				.entity(new Status("Success", "Successfully uploaded image. "
						+ media.getMediaURL(), "")).build();

	}
	
}
