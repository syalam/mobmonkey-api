package com.MobMonkey.Models;

import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Partner")
public class Partner {
	public Partner(){
	}
	private String partnerId;
	private String Name;
	private String Email;
	private boolean enabled;
	private Date lastActivity;
	
	
	@DynamoDBHashKey
	public String getpartnerId() {
		return partnerId;
	}
	public void setpartnerId(String id) {
		partnerId = id;
	}
	
	@DynamoDBAttribute
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	@DynamoDBAttribute
	public String getEmail() {
		return Email;
	}
	public void setEmail(String email) {
		Email = email;
	}
	@DynamoDBAttribute
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	//@DynamoDBRangeKey
	public Date getLastActivity() {
		return lastActivity;
	}
	public void setLastActivity(Date lastActivity) {
		this.lastActivity = lastActivity;
	}

}
