package com.MobMonkey.Models;

import java.io.Serializable;
import java.util.Date;
import com.amazonaws.services.dynamodb.datamodeling.*;

@DynamoDBTable(tableName = "User")
public class User implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7724538794944214988L;

	private String partnerId;
	private String eMailAddress;
	private String notificationId;
	private String password;
	private String firstName;
	private String lastName;
	private Date birthday;
	private int gender;
	private String phoneNumber;
	private String city;
	private String state;
	private String zip;
	private boolean verified;
	private boolean acceptedtos;
	private Date dateRegistered;
	private int numberOfRequests;
	private String deviceId;
	private String deviceType;
	private Date lastSignIn;
	private int rank;
	private Date lastRankUpdate;
	private int inappropriateStrikes;
	private Date firstInappropriateStrike;
	private boolean admin;
	private boolean suspended;

	@DynamoDBHashKey
	public String geteMailAddress() {
		return eMailAddress;
	}
	
	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	@DynamoDBRangeKey(attributeName = "partnerId")
	public String getPartnerId()
	{
		return partnerId;
	}
	
	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}
	
	@DynamoDBAttribute
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@DynamoDBAttribute
	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}
	
	@DynamoDBAttribute
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@DynamoDBAttribute
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@DynamoDBAttribute
	public Date getBirthday() {
		return birthday;
	}
	
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@DynamoDBAttribute
	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}
	
	@DynamoDBAttribute
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@DynamoDBAttribute
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@DynamoDBAttribute
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@DynamoDBAttribute
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@DynamoDBAttribute
	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	@DynamoDBAttribute
	public boolean isAcceptedtos() {
		return acceptedtos;
	}

	public void setAcceptedtos(boolean acceptedtos) {
		this.acceptedtos = acceptedtos;
	}

	@DynamoDBAttribute
	public Date getDateRegistered() {
		return dateRegistered;
	}

	public void setDateRegistered(Date dateRegistered) {
		this.dateRegistered = dateRegistered;
	}
	
	@DynamoDBAttribute
	public int getNumberOfRequests() {
		return numberOfRequests;
	}

	public void setNumberOfRequests(int numberOfRequests) {
		this.numberOfRequests = numberOfRequests;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	@DynamoDBAttribute
	public Date getLastSignIn() {
		return lastSignIn;
	}

	public void setLastSignIn(Date lastSignIn) {
		this.lastSignIn = lastSignIn;
	}

	@DynamoDBAttribute
	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	@DynamoDBAttribute
	public Date getLastRankUpdate() {
		return lastRankUpdate;
	}

	public void setLastRankUpdate(Date lastRankUpdate) {
		this.lastRankUpdate = lastRankUpdate;
	}

	@DynamoDBAttribute
	public int getInappropriateStrikes() {
		return inappropriateStrikes;
	}

	public void setInappropriateStrikes(int inappropriateStrikes) {
		this.inappropriateStrikes = inappropriateStrikes;
	}

	@DynamoDBAttribute
	public Date getFirstInappropriateStrike() {
		return firstInappropriateStrike;
	}

	public void setFirstInappropriateStrike(Date firstInappropriateStrike) {
		this.firstInappropriateStrike = firstInappropriateStrike;
	}

	@DynamoDBAttribute
	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	@DynamoDBAttribute
	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}
	
}
