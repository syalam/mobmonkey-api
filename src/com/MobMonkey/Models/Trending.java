package com.MobMonkey.Models;

import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName = "Trending")
public class Trending {

	private String type;
	private String ProviderId;
	private String LocationId;
	private Date timeStamp;
	
	public Trending(){
		
	}

	@DynamoDBHashKey
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@DynamoDBAttribute
	public String getProviderId() {
		return ProviderId;
	}

	public void setProviderId(String providerId) {
		ProviderId = providerId;
	}

	@DynamoDBAttribute
	public String getLocationId() {
		return LocationId;
	}

	public void setLocationId(String locationId) {
		LocationId = locationId;
	}

	@DynamoDBRangeKey
	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
}
