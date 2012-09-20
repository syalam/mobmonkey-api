package com.MobMonkey.Models;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName = "Oauth")
public class Oauth {
	private String eMailAddress;
	private String oAuthToken;
	private String oAuthTokenSecret;
	private String oAuthProvider;
	private boolean eMailVerified;
	
	public Oauth(){
		
	}

	@DynamoDBHashKey
	public String geteMailAddress() {
		return eMailAddress;
	}

	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	@DynamoDBRangeKey
	public String getoAuthToken() {
		return oAuthToken;
	}

	public void setoAuthToken(String oauthToken) {
		oAuthToken = oauthToken;
	}

	@DynamoDBAttribute
	public String getoAuthTokenSecret() {
		return oAuthTokenSecret;
	}

	public void setoAuthTokenSecret(String oauthTokenSecret) {
		oAuthTokenSecret = oauthTokenSecret;
	}

	@DynamoDBAttribute
	public String getoAuthProvider() {
		return oAuthProvider;
	}

	public void setoAuthProvider(String oauthProvider) {
		oAuthProvider = oauthProvider;
	}

	@DynamoDBAttribute
	public boolean iseMailVerified() {
		return eMailVerified;
	}

	public void seteMailVerified(boolean eMailVerified) {
		this.eMailVerified = eMailVerified;
	}
	
}