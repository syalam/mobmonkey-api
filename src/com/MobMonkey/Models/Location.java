package com.MobMonkey.Models;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Location")
public class Location {
	private String locationId;
	private String providerId;
	private String name;
	private String description;
	private String categoryId;
	private String latitude;
	private String longitude;
	private String radiusInYards;

	public Location() {
	}
	
	@DynamoDBHashKey
	public String getLocationId() {
		return locationId;
	}
	
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	@DynamoDBRangeKey
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

	@DynamoDBAttribute
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@DynamoDBAttribute
	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String category) {
		this.categoryId = category;
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
	public String getRadiusInYards() {
		return radiusInYards;
	}

	public void setRadiusInYards(String radiusInYards) {
		this.radiusInYards = radiusInYards;
	}

}
