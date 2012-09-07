package com.MobMonkey.Models;

import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Media")
public class Media extends Message {

	private String Id;
	private String mediaData;
	private String type;
	private double xCoordinate;
	private double yCoordinate;
	private Date uploadedDate;
	
	public Media(){
	}
	
	@DynamoDBRangeKey(attributeName = "partnerId")
	public String getPartnerId()
	{
		return this.getPartnerId();
	}

	@DynamoDBHashKey
	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	@DynamoDBAttribute()
	public String geteMailAddress() {
		return this.geteMailAddress();
	}

	public void seteMailAddress(String eMailAddress) {
		this.seteMailAddress(eMailAddress);
	}

	@DynamoDBIgnore
	public String getMediaData() {
		return mediaData;
	}

	public void setMediaData(String mediaData) {
		this.mediaData = mediaData;
	}

	@DynamoDBAttribute()
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@DynamoDBAttribute()
	public double getxCoordinate() {
		return xCoordinate;
	}

	public void setxCoordinate(double xCoordinate) {
		this.xCoordinate = xCoordinate;
	}
	
	@DynamoDBAttribute()
	public double getyCoordinate() {
		return yCoordinate;
	}


	public void setyCoordinate(double yCoordinate) {
		this.yCoordinate = yCoordinate;
	}
	
	@DynamoDBAttribute()
	public Date getUploadedDate() {
		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
	}
	
	
	
}
