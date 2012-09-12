package com.MobMonkey.Models;

import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "CheckIn")
public class CheckIn extends Message {

	private String locationId;
	private String providerId;
	private String latitude;
	private String longitude;
	private Date dateCheckedIn;
	
	public CheckIn(){
		
	}
	
    @DynamoDBAttribute
	public String getPartnerId() {
			return super.getPartnerId();
	}
	public void setPartnerId(String PartnerId) {
	
		super.setPartnerId(PartnerId);
	}
	@DynamoDBHashKey
	public String geteMailAddress() {
	
		return super.geteMailAddress();
	}
	public void seteMailAddress(String eMailAddress) {
		
		super.seteMailAddress(eMailAddress);
	}

	@DynamoDBAttribute
	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	@DynamoDBAttribute
	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	@DynamoDBAttribute
	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	@DynamoDBAttribute
	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	@DynamoDBAttribute
	public Date getDateCheckedIn() {
		return dateCheckedIn;
	}
	public void setDateCheckedIn(Date dateCheckedIn) {
		this.dateCheckedIn = dateCheckedIn;
	}
	
	
	
	
}
