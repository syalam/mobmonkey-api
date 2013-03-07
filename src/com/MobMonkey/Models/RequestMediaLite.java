package com.MobMonkey.Models;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

public class RequestMediaLite implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 407073572639653011L;
	private String RequestId;
	private String message;
	private String locationName;
	private int mediaType;
	private int requestType;
	private Date expiryDate;
	private String providerId;
	private String locationId;
	private String longitude;
	private String latitude;
	private int radiusInYards;
	private Date requestDate;
	@JsonIgnore private String requestorEmail;
	
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

	public String getRequestorEmail() {
		return requestorEmail;
	}

	public void setRequestorEmail(String requestorEmail) {
		this.requestorEmail = requestorEmail;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public int getRadiusInYards() {
		return radiusInYards;
	}

	public void setRadiusInYards(int radiusInYards) {
		this.radiusInYards = radiusInYards;
	}
}
