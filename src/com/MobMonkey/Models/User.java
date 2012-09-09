package com.MobMonkey.Models;

import java.util.Date;
import com.amazonaws.services.dynamodb.datamodeling.*;

@DynamoDBTable(tableName = "User")
public class User extends Message {
	
	public User()
	{
	}
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
	
	@DynamoDBHashKey
	public String geteMailAddress() {
		return super.geteMailAddress();
	}
	
	public void seteMailAddress(String eMailAddress) {
		super.seteMailAddress(eMailAddress);
	}

	@DynamoDBRangeKey(attributeName = "partnerId")
	public String getPartnerId()
	{
		return super.getPartnerId();	
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

	
	
}
