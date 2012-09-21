package com.MobMonkey.Models;

import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName = "AssignedRequests")
public class AssignedRequest {
	private String eMailAddress;
	private String requestId;
	private String message;
	private int mediaType;
	private int requestType;
	private Date expiryDate;
	private Date assignedDate;

	public AssignedRequest() {

	}

	@DynamoDBHashKey
	public String geteMailAddress() {
		return eMailAddress;
	}

	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	@DynamoDBAttribute
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@DynamoDBAttribute
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@DynamoDBAttribute
	public int getMediaType() {
		return mediaType;
	}

	public void setMediaType(int type) {
		this.mediaType = type;
	}

	@DynamoDBAttribute
	public int getRequestType() {
		return requestType;
	}

	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}

	@DynamoDBRangeKey
	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	@DynamoDBAttribute
	public Date getAssignedDate() {
		return assignedDate;
	}

	public void setAssignedDate(Date assignedDate) {
		this.assignedDate = assignedDate;
	}

}
