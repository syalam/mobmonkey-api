package com.MobMonkey.Models;

import java.util.Date;

public class MediaLite {

	private String MediaURL;
	private Date expiryDate;
	
	public MediaLite(){
		
	}
	
	public String getMediaURL() {
		return MediaURL;
	}

	public void setMediaURL(String mediaURL) {
		MediaURL = mediaURL;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	
}
