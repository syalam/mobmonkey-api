package com.MobMonkey.Models;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Media")
public class Media implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1707348759222416465L;
	private String mediaId;
	private String requestId;
	private int mediaType; // image = 1, video = 2, live streaming = 3
	private String contentType;
	private String requestType;
	private String eMailAddress;
	private String mediaData;
	private String mediaURL;
	private String thumbURL;
	private String text;
	private Date uploadedDate;
	private boolean accepted;
	private boolean flaggedAsInappropriate;
	private boolean confirmedInappropriate;
	@JsonIgnore private String originalRequestor;
	

	public Media() {
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

	@DynamoDBAttribute
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
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
	public String getThumbURL() {
		return thumbURL;
	}

	public void setThumbURL(String thumbURL) {
		this.thumbURL = thumbURL;
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

	@DynamoDBAttribute
	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	@DynamoDBAttribute
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@DynamoDBAttribute
	public boolean isFlaggedAsInappropriate() {
		return flaggedAsInappropriate;
	}

	public void setFlaggedAsInappropriate(boolean flaggedAsInappropriate) {
		this.flaggedAsInappropriate = flaggedAsInappropriate;
	}
	@DynamoDBAttribute
	public boolean isConfirmedInappropriate() {
		return confirmedInappropriate;
	}

	public void setConfirmedInappropriate(boolean confirmedInappropriate) {
		this.confirmedInappropriate = confirmedInappropriate;
	}

}
