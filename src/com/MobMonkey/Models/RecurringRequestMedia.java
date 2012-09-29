package com.MobMonkey.Models;

import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "RecurringRequestMedia")
public class RecurringRequestMedia  {
	private String partnerId;
	private String eMailAddress;
	private String requestId;
	private int requestType;
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
	private int frequencyInMS;
	
	public RecurringRequestMedia() {
	}

	@DynamoDBAttribute()
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

}
