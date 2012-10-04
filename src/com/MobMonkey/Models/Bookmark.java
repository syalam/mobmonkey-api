package com.MobMonkey.Models;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName = "Bookmark")
public class Bookmark {

	private String eMailAddress;
	@JsonIgnore private String locprovId;
	private String locationId;
	private String providerId;
	
	public Bookmark(){
		
	}

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

	public void setLocprovId(String locprovId) {
		this.locprovId = locprovId;
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

}
