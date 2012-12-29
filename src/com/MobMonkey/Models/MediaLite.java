package com.MobMonkey.Models;

import java.util.Date;

public class MediaLite {

	private String MediaURL;
	private String mediaId;
	private String requestId;
	private Date expiryDate;
	private Date uploadedDate;
	private String type;
	private String contentType;
	private boolean accepted;
	
	public MediaLite(){
		
	}
	
	public String getMediaURL() {
		return MediaURL;
	}

	public void setMediaURL(String mediaURL) {
		MediaURL = mediaURL;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public Date getUploadedDate() {
		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
	}
	
}
