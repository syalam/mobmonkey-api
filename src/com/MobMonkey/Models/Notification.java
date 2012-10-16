package com.MobMonkey.Models;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName = "Notification")
public class Notification {

	private String eMailAddress;
	@JsonIgnore private String locprovId;
	private String locationId;
	private String providerId;
	@JsonIgnore private String frequency;
	private Date startDate;
	private Date lastUpdateDate;
	
	@DynamoDBHashKey
	public String geteMailAddress() {
		return eMailAddress;
	}
	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}
	@DynamoDBRangeKey
	public String getLocprovId() {
		return locprovId;
	}
	public void setLocprovId(String locProvId) {
		this.locprovId = locProvId;
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
	@DynamoDBAttribute
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	@DynamoDBAttribute
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	@DynamoDBAttribute
	public Date getLastUpdateDate() {
		return this.lastUpdateDate;
	}
	public void setLastUpdateDate(Date lastUpdate) {
		this.lastUpdateDate = lastUpdate;
	}
	
	
}
