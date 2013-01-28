package com.MobMonkey.Models;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "LocationMessage")
public class LocationMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3758474678223983980L;
	@JsonIgnore private String locprovId;
	private String messageId;
	private String message;
	private Date modifiedDate;
	
	public LocationMessage(){
		
	}

	@DynamoDBHashKey
	public String getLocprovId() {
		return locprovId;
	}

	public void setLocprovId(String locprovId) {
		this.locprovId = locprovId;
	}

	@DynamoDBRangeKey
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@DynamoDBAttribute
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@DynamoDBAttribute
	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}
