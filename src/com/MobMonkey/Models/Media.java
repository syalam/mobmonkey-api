package com.MobMonkey.Models;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Media")
public class Media {
	
	private String mediaId;
	private String requestId;
	private int mediaType; //image = 1, video = 2
	private String requestType; 
	private String eMailAddress;
	private String mediaData;
	private String mediaURL;
	private Date uploadedDate;
	@JsonIgnore private String originalRequestor;
	
	public Media(){
	}
	

	@DynamoDBRangeKey
	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String id) {
		mediaId = id;
	}

	
	@DynamoDBHashKey
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	@DynamoDBAttribute
	public int getMediaType() {
		return mediaType;
	}


	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}


	@DynamoDBAttribute()
	public String getRequestType() {
		return requestType;
	}


	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	@DynamoDBAttribute()
	public String geteMailAddress() {
		return eMailAddress;
	}

	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	@DynamoDBIgnore
	public String getMediaData() {
		return mediaData;
	}

	public void setMediaData(String mediaData) {
		this.mediaData = mediaData;
	}
	
	@DynamoDBAttribute()
	public String getMediaURL() {
		return mediaURL;
	}


	public void setMediaURL(String mediaURL) {
		this.mediaURL = mediaURL;
	}


	@DynamoDBAttribute()
	public Date getUploadedDate() {
		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
	}


	public String getOriginalRequestor() {
		return originalRequestor;
	}

	@DynamoDBAttribute
	public void setOriginalRequestor(String originalRequestor) {
		this.originalRequestor = originalRequestor;
	}
	
	
	
}
