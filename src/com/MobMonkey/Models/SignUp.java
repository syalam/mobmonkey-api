package com.MobMonkey.Models;

import java.util.Date;
import com.amazonaws.services.dynamodb.datamodeling.*;

@DynamoDBTable(tableName = "User")
public class SignUp extends Message {
	
	public SignUp()
	{
	}
	String password;
	String firstName;
	String lastName;
	Date birthday;
	int gender;
	String phoneNumber;
	String city;
	String state;
	String zip;
	boolean verified;
	boolean acceptedtos;

	@DynamoDBRangeKey(attributeName = "partnerId")
	public String getPartnerId()
	{
		return super.getPartnerId();	
	}
	
	@DynamoDBHashKey(attributeName = "eMailAddress")
	public String geteMailAddress() {
		return super.geteMailAddress();
	}
	
	public void seteMailAddress(String eMailAddress) {
		super.seteMailAddress(eMailAddress);
	}
	
	@DynamoDBHashKey(attributeName = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@DynamoDBAttribute(attributeName = "firstName")
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@DynamoDBAttribute(attributeName = "lastName")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@DynamoDBAttribute(attributeName = "birthday")
	public Date getBirthday() {
		return birthday;
	}
	
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@DynamoDBAttribute(attributeName = "gender")
	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}
	
	@DynamoDBAttribute(attributeName = "phoneNumber")
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@DynamoDBAttribute(attributeName = "city")
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@DynamoDBAttribute(attributeName = "state")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@DynamoDBAttribute(attributeName = "zip")
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@DynamoDBAttribute(attributeName = "verified")
	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	@DynamoDBAttribute(attributeName = "acceptedtos")
	public boolean isAcceptedtos() {
		return acceptedtos;
	}

	public void setAcceptedtos(boolean acceptedtos) {
		this.acceptedtos = acceptedtos;
	}

	
	
}
