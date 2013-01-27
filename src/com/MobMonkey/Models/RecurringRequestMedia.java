package com.MobMonkey.Models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "RecurringRequestMedia")
public class RecurringRequestMedia implements Serializable,Cloneable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6922348300565657566L;
	private String partnerId;
	private String eMailAddress;
	private String requestId;
	private Date requestDate;
	private int requestType;
	private int mediaType;
	private String message;
	private String locationId;
	private String providerId;
	private String latitude;
	private String longitude;
	private int radiusInYards; 
	private Date scheduleDate;
	private int duration;
	private boolean requestFulfilled;
	private Date fulfilledDate;
	private boolean recurring;
	private boolean expired;
	private int frequencyInMS;
	private String nameOfLocation;
	private boolean markAsRead;
	private List<MediaLite> media;
	
	public RecurringRequestMedia() {
	}

	@DynamoDBRangeKey
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String id) {
		requestId = id;
	}

	
	@DynamoDBHashKey
	public String geteMailAddress() {
	return eMailAddress;
	}

	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	@DynamoDBAttribute()
	public String getPartnerId()
	{
		return partnerId;
	}
	
	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	@DynamoDBAttribute()
	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	@DynamoDBAttribute()
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@DynamoDBAttribute()
	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	@DynamoDBAttribute()
	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	@DynamoDBAttribute()
	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	@DynamoDBAttribute()
	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	@DynamoDBAttribute()
	public int getRadiusInYards() {
		return radiusInYards;
	}

	public void setRadiusInYards(int radiusInYards) {
		this.radiusInYards = radiusInYards;
	}

	@DynamoDBAttribute()
	public int getRequestType() {
		return requestType;
	}

	@DynamoDBAttribute()
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}

	@DynamoDBAttribute()
	public int getMediaType() {
		return mediaType;
	}

	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}

	@DynamoDBAttribute()
	public Date getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(Date scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	@DynamoDBAttribute()
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}


	@DynamoDBAttribute()
	public boolean isRequestFulfilled() {
		return requestFulfilled;
	}

	public void setRequestFulfilled(boolean requestFulfilled) {
		this.requestFulfilled = requestFulfilled;
	}

	@DynamoDBAttribute()
	public Date getFulfilledDate() {
		return fulfilledDate;
	}

	public void setFulfilledDate(Date fulfilledDate) {
		this.fulfilledDate = fulfilledDate;
	}

	@DynamoDBAttribute()
	public boolean isRecurring() {
		return recurring;
	}

	public void setRecurring(boolean recurring) {
		this.recurring = recurring;
	}

	@DynamoDBAttribute()
	public int getFrequencyInMS() {
		return frequencyInMS;
	}

	public void setFrequencyInMS(int frequencyInMS) {
		this.frequencyInMS = frequencyInMS;
	}

	@DynamoDBIgnore
	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}
	
	@DynamoDBAttribute
	public String getNameOfLocation() {
		return nameOfLocation;
	}

	public void setNameOfLocation(String nameOfLocation) {
		this.nameOfLocation = nameOfLocation;
	}
	
	@DynamoDBIgnore
	public List<MediaLite> getMedia() {
		return media;
	}

	public void setMedia(List<MediaLite> media) {
		this.media = media;
	}

	@DynamoDBAttribute
	public boolean isMarkAsRead() {
		return markAsRead;
	}

	public void setMarkAsRead(boolean markAsRead) {
		this.markAsRead = markAsRead;
	}

	 public Object clone() throws CloneNotSupportedException {
	        return super.clone();
	  }
}
