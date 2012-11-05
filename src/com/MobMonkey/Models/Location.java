package com.MobMonkey.Models;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Location")
public class Location {
	private String locationId;
	private String providerId;
	private String name;
	private String categoryIds;
	private String categoryLabels;
	private String distance;
	private String neighborhood;
	private String latitude;
	private String longitude;
	private String radiusInYards;
	private String address;
	private String address_ext;
	private String locality;
	private String region;
	private String postcode;
	private String countryCode;
	private String phoneNumber;
	private String webSite;
	private boolean bookmark;
	private int monkeys;
	private int images;
	private int videos;
	private int livestreaming;

	public Location() {
	}
	
	@DynamoDBHashKey
	public String getLocationId() {
		return locationId;
	}
	
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	@DynamoDBRangeKey
	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	@DynamoDBAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@DynamoDBAttribute
	public String getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(String categoryIds) {
		this.categoryIds = categoryIds;
	}

	@DynamoDBIgnore
	public String getCategoryLabels() {
		return categoryLabels;
	}

	public void setCategoryLabels(String categoryLabels) {
		this.categoryLabels = categoryLabels;
	}

	@DynamoDBIgnore
	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	@DynamoDBAttribute
	public String getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}

	@DynamoDBAttribute
	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	@DynamoDBAttribute
	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	@DynamoDBAttribute
	public String getRadiusInYards() {
		return radiusInYards;
	}

	public void setRadiusInYards(String radiusInYards) {
		this.radiusInYards = radiusInYards;
	}
	
	
	@DynamoDBAttribute
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@DynamoDBAttribute
	public String getAddress_ext() {
		return address_ext;
	}

	public void setAddress_ext(String address_ext) {
		this.address_ext = address_ext;
	}

	@DynamoDBAttribute
	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}
	@DynamoDBAttribute
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
	@DynamoDBAttribute
	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	@DynamoDBAttribute
	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	@DynamoDBAttribute
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	@DynamoDBAttribute
	public String getWebSite() {
		return webSite;
	}

	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}

	@DynamoDBIgnore
	public boolean isBookmark() {
		return bookmark;
	}

	public void setBookmark(boolean bookmark) {
		this.bookmark = bookmark;
	}

	@DynamoDBIgnore
	public int getMonkeys() {
		return monkeys;
	}

	public void setMonkeys(int monkeys) {
		this.monkeys = monkeys;
	}
	@DynamoDBIgnore
	public int getImages() {
		return images;
	}

	public void setImages(int images) {
		this.images = images;
	}
	@DynamoDBIgnore
	public int getVideos() {
		return videos;
	}

	public void setVideos(int videos) {
		this.videos = videos;
	}
	@DynamoDBIgnore
	public int getLivestreaming() {
		return livestreaming;
	}

	public void setLivestreaming(int livestreaming) {
		this.livestreaming = livestreaming;
	}

}
