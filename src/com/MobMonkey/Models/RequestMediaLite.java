package com.MobMonkey.Models;

import java.util.Date;

public class RequestMediaLite {

	private String RequestId;
	private String message;
	private int mediaType;
	private int requestType;
	private Date expiryDate;
	
	public RequestMediaLite()
	{
		
	}

	public String getRequestId() {
		return RequestId;
	}

	public void setRequestId(String requestId) {
		RequestId = requestId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getMediaType() {
		return mediaType;
	}

	public void setMediaType(int type) {
		this.mediaType = type;
	}

	public int getRequestType() {
		return requestType;
	}

	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
}
