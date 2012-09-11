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
	private String eMailAddress;
	private String password;
	private String acctNo;
	private String achRTNo;
	private boolean enabled;
	private boolean revShare;
	private Date lastActivity;
	private Date dateRegistered;
	
	
	@DynamoDBHashKey
	public String getPartnerId() {
		return partnerId;
	}
	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}
	
	@DynamoDBAttribute
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	@DynamoDBAttribute
	public String getEmailAddress() {
		return eMailAddress;
	}
	public void setEmailAddress(String email) {
		eMailAddress = email;
	}
	@DynamoDBAttribute
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@DynamoDBAttribute
	public String getAcctNo() {
		return acctNo;
	}
	public void setAcctNo(String acctNo) {
		this.acctNo = acctNo;
	}
	@DynamoDBAttribute
	public String getAchRTNo() {
		return achRTNo;
	}
	public void setAchRTNo(String achRTNo) {
		this.achRTNo = achRTNo;
	}
	@DynamoDBAttribute
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	@DynamoDBAttribute
	public boolean isRevShare() {
		return revShare;
	}
	public void setRevShare(boolean revShare) {
		this.revShare = revShare;
	}
	@DynamoDBAttribute
	public Date getLastActivity() {
		return lastActivity;
	}
	public void setLastActivity(Date lastActivity) {
		this.lastActivity = lastActivity;
	}
	@DynamoDBAttribute
	public Date getDateRegistered() {
		return dateRegistered;
	}
	public void setDateRegistered(Date registered) {
		this.dateRegistered = registered;
	}

}
