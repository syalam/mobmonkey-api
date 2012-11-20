package com.MobMonkey.Models;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "LocationMedia")
public class LocationMedia {

	@JsonIgnore
	private String locationProviderId;
	@JsonIgnore
	private Date uploadedDate;
	@JsonIgnore
	private String mediaId;
	@JsonIgnore
	private String requestId;
	private String locationId;
	private String providerId;
	private List<MediaLite> media;

	public LocationMedia() {

	}

	@DynamoDBHashKey
	public String getLocationProviderId() {
		return locationProviderId;
	}

	public void setLocationProviderId(String locationProviderId) {
		this.locationProviderId = locationProviderId;
	}

	@DynamoDBRangeKey
	public Date getUploadedDate() {
		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
	}

	@DynamoDBAttribute
	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	@DynamoDBAttribute
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	@DynamoDBIgnore
	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	@DynamoDBIgnore
	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	@DynamoDBIgnore
	public List<MediaLite> getMedia() {
		return media;
	}

	public void setMedia(List<MediaLite> media) {
		this.media = media;
	}

}
