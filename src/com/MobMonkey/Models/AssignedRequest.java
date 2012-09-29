package com.MobMonkey.Models;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName = "AssignedRequest")
public class AssignedRequest {
	@JsonIgnore private String eMailAddress;
	private String requestId;
	private String message;
	private int mediaType;
	private int requestType;
	private Date expiryDate;
	private Date assignedDate;
	private Date fulFilledDate;
	@JsonIgnore private String requestorEmail;

	public AssignedRequest() {

	}

	@DynamoDBHashKey
	public String geteMailAddress() {
		return eMailAddress;
	}

	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	@DynamoDBRangeKey
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

	@DynamoDBAttribute
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

	@DynamoDBAttribute
	public Date getFulFilledDate() {
		return fulFilledDate;
	}

	public void setFulFilledDate(Date fulFilledDate) {
		this.fulFilledDate = fulFilledDate;
	}

	@DynamoDBAttribute
	public String getRequestorEmail() {
		return requestorEmail;
	}

	public void setRequestorEmail(String requestorEmail) {
		this.requestorEmail = requestorEmail;
	}

}
