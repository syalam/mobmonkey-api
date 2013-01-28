package com.MobMonkey.Models;

import java.io.Serializable;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName = "LocationProvider")
public class LocationProvider implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6663493138936615783L;
	private String providerId;
	private String name;
	
	public LocationProvider(){
		
	}

	@DynamoDBHashKey
	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	@DynamoDBAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
