package com.MobMonkey.Models;

import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;


//TODO extend the Message class here, git rid of constructor with params to see if that fixes the DynamoDB loader


@DynamoDBTable(tableName = "Verify")
public class Verify {

	private String VerifyID;
	private String partnerId;
	private String eMailAddress;
	private String oauthToken;
	private Date sentDate;
	private Date recvDate;
	
	public Verify(){
		
	}
	public Verify(String id, String partnerId, String eMailAddress, Date sentDate){
		this.VerifyID = id;
		this.partnerId = partnerId;
		this.eMailAddress = eMailAddress;
		this.sentDate = sentDate;
	}

	@DynamoDBHashKey
	public String getVerifyID() {
		return VerifyID;
	}

	public void setVerifyID(String verifyID) {
		VerifyID = verifyID;
	}

	@DynamoDBRangeKey
	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	@DynamoDBAttribute
	public String geteMailAddress() {
		return eMailAddress;
	}

	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	@DynamoDBAttribute
	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	@DynamoDBAttribute
	public Date getRecvDate() {
		return recvDate;
	}

	public void setRecvDate(Date recvDate) {
		this.recvDate = recvDate;
	}
	@DynamoDBAttribute
	public String getOauthToken() {
		return oauthToken;
	}
	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}
	
}
