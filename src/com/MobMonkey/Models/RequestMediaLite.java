package com.MobMonkey.Models;

public class RequestMediaLite {

	private String RequestId;
	private String message;
	
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
}
