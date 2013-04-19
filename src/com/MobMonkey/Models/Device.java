package com.MobMonkey.Models;

import java.io.Serializable;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName="Device")
public class Device implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3505201573153901019L;
	private String eMailAddress;
	private String deviceId;
	private String deviceType;
	
	
	public Device() {
		this(null, null, null);
	}

	public Device(String email, String deviceId, String deviceTypeName){
		this.eMailAddress = email;
		this.deviceId = deviceId;
		this.deviceType = deviceTypeName;
	}

	@DynamoDBHashKey
	public String geteMailAddress() {
		return eMailAddress;
	}

	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	@DynamoDBRangeKey
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	@DynamoDBAttribute
	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}


}
