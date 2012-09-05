package com.MobMonkey.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.MobMonkey.Models.Media;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Path("/media")
public class MediaResource {

	AmazonS3Client s3cli;
	private AWSCredentials credentials;
	private AmazonDynamoDBClient ddb;
	private DynamoDBMapper mapper;
	
	public MediaResource() {
		try {
			credentials = new PropertiesCredentials(getClass().getClassLoader()
					.getResourceAsStream("AwsCredentials.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		s3cli = new AmazonS3Client();
		ddb = new AmazonDynamoDBClient(credentials);
		ddb.setEndpoint("https://dynamodb.us-west-1.amazonaws.com", "dynamodb",
				"us-west-1");

		mapper = new DynamoDBMapper(ddb);
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
		putObjectRequest.setRequestCredentials(credentials);
		
		putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
	
		s3cli.putObject(putObjectRequest);
	

		mapper.save(media);
		String result = "Successfully uploaded image. https://s3-us-west-1.amazonaws.com/mobmonkeyimages/" + keyName;
		return Response.status(201).entity(result).build();

	}
	
	

}
