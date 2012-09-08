package com.MobMonkey.Models;

import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "RequestMedia")
public class RequestMedia extends Message {
	private String RequestId;
	private String message;
	private String locationId;
	private String providerId;
	private int requestType;
	private int scheduleMins;  
	private Date scheduleDate;
	private int duration;
	private boolean recurringSchedule; 
	private boolean requestFulfilled;
	private int vicinity;

	public RequestMedia() {
	}

	@DynamoDBHashKey
	public String getRequestId() {
		return RequestId;
	}

	public void setRequestId(String id) {
		RequestId = id;
	}

	@DynamoDBAttribute()
	public String geteMailAddress() {
		return super.geteMailAddress();
	}

	public void seteMailAddress(String eMailAddress) {
		super.seteMailAddress(eMailAddress);
	}

	@DynamoDBAttribute()
	public String getPartnerId()
	{
		return super.getPartnerId();
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
	public int getRequestType() {
		return requestType;
	}

	@DynamoDBAttribute()
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}

	@DynamoDBAttribute()
	public int getScheduleMins() {
		return scheduleMins;
	}

	public void setScheduleMins(int scheduleMins) {
		this.scheduleMins = scheduleMins;
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
	public boolean isRecurringSchedule() {
		return recurringSchedule;
	}

	public void setRecurringSchedule(boolean recurringSchedule) {
		this.recurringSchedule = recurringSchedule;
	}

	@DynamoDBAttribute()
	public boolean isRequestFulfilled() {
		return requestFulfilled;
	}

	public void setRequestFulfilled(boolean requestFulfilled) {
		this.requestFulfilled = requestFulfilled;
	}

	@DynamoDBAttribute()
	public int getVicinity() {
		return vicinity;
	}

	public void setVicinity(int vicinity) {
		this.vicinity = vicinity;
	}

}
