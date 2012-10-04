package com.MobMonkey.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.LocationCategory;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.factual.driver.*;

public class FactualHelper extends ResourceHelper {
	private Factual factual;
	private static String factual_providerId = "222e736f-c7fa-4c40-b78e-d99243441fae";

	public FactualHelper() {
		super();
		factual = new Factual("BEoV3TPDev03P6NJSVJPgTmuTNOegwRsjJN41DnM",
				"hwxVQz4lAxb5YpWhbLq10KhWiEw5k35WgFuoR2YI");

	}

	public Location reverseLookUp(String locationId) {
		
		//TODO Working for only factual, need to add MobMonkey
		Location results = new Location();
		Query query = new Query();
		query.field("factual_id").equal(locationId);

		ReadResponse resp = factual.fetch("places", query);

		List<Map<String, Object>> data = resp.getData();
		for (Map<String, Object> map : data) {
	
			String country = (map.containsKey("country") == true) ? map.get(
					"country").toString() : "";
			String latitude = (map.containsKey("latitude") == true) ? map.get(
					"latitude").toString() : "";
			String longitude = (map.containsKey("longitude") == true) ? map
					.get("longitude").toString() : "";
			String locality = (map.containsKey("locality") == true) ? map.get(
					"locality").toString() : "";
			String name = (map.containsKey("name") == true) ? map.get("name")
					.toString() : "";
			String tel = (map.containsKey("tel") == true) ? map.get("tel")
					.toString() : "";
			String postcode = (map.containsKey("postcode") == true) ? map.get(
					"postcode").toString() : "";
			String region = (map.containsKey("region") == true) ? map.get(
					"region").toString() : "";
			String address = (map.containsKey("address") == true) ? map.get(
					"address").toString() : "";
			String website = (map.containsKey("website") == true) ? map.get(
					"website").toString() : "";
					
					results.setLocationId(locationId);
					results.setCountryCode(country);
					results.setLatitude(latitude);
					results.setLongitude(longitude);
					results.setLocality(locality);
					results.setName(name);
					results.setPhoneNumber(tel);
					results.setPostcode(postcode);
					results.setProviderId(factual_providerId);
					results.setRegion(region);
					results.setStreetAddress(address);
					results.setWebSite(website);	
		}
		return results;
	}

	public List<Location> GeoFilter(Location loc) {
		int radiusInMeters = (int) (Integer.parseInt(loc.getRadiusInYards()) * .9144); // convert
																						// yards
																						// to
																						// meters

		ReadResponse resp = factual
				.fetch("places", new Query().within(new Circle(Double
						.parseDouble(loc.getLatitude()), Double.parseDouble(loc
						.getLongitude()), radiusInMeters)));
		List<Map<String, Object>> data = resp.getData();

		List<Location> results = new ArrayList<Location>();
		for (Map<String, Object> map : data) {
			Location returnedLoc = new Location();
			String factualCategory = "";
			try {
				factualCategory = categoryFactory(map.get("category")
						.toString());
				returnedLoc.setCategoryId(factualCategory);
				returnedLoc.setCategory(map.get("category").toString());
			} catch (Exception exc) {
				// TODO what do I do when factual doesnt have a category for me?
			}

			String locationId = (map.containsKey("factual_id") == true) ? map
					.get("factual_id").toString() : "";
			String country = (map.containsKey("country") == true) ? map.get(
					"country").toString() : "";
			String latitude = (map.containsKey("latitude") == true) ? map.get(
					"latitude").toString() : "";
			String longitude = (map.containsKey("longitude") == true) ? map
					.get("longitude").toString() : "";
			String locality = (map.containsKey("locality") == true) ? map.get(
					"locality").toString() : "";
			String name = (map.containsKey("name") == true) ? map.get("name")
					.toString() : "";
			String tel = (map.containsKey("tel") == true) ? map.get("tel")
					.toString() : "";
			String postcode = (map.containsKey("postcode") == true) ? map.get(
					"postcode").toString() : "";
			String region = (map.containsKey("region") == true) ? map.get(
					"region").toString() : "";
			String address = (map.containsKey("address") == true) ? map.get(
					"address").toString() : "";
			String website = (map.containsKey("website") == true) ? map.get(
					"website").toString() : "";

			returnedLoc.setLocationId(locationId);
			returnedLoc.setCountryCode(country);
			returnedLoc.setLatitude(latitude);
			returnedLoc.setLongitude(longitude);
			returnedLoc.setLocality(locality);
			returnedLoc.setName(name);
			returnedLoc.setPhoneNumber(tel);
			returnedLoc.setPostcode(postcode);
			returnedLoc.setProviderId(factual_providerId);
			returnedLoc.setRegion(region);
			returnedLoc.setStreetAddress(address);
			returnedLoc.setWebSite(website);

			results.add(returnedLoc);

		}
		// need to return locations

		return results;
	}

	private String categoryFactory(String cat) {
		String[] catArray = cat.split(">");
		String parentName = catArray[0].trim();
		String name = catArray[1].trim();

		// Create the parent category.. we should check to see if it exists
		DynamoDBScanExpression queryExpression = new DynamoDBScanExpression();

		queryExpression
				.addFilterCondition(
						"name",
						new Condition().withComparisonOperator(
								ComparisonOperator.EQ).withAttributeValueList(
								new AttributeValue().withS(parentName)));

		queryExpression.addFilterCondition("parentId", new Condition()
				.withComparisonOperator(ComparisonOperator.EQ)
				.withAttributeValueList(new AttributeValue().withS("null")));

		PaginatedScanList<LocationCategory> parents = super.mapper().scan(
				LocationCategory.class, queryExpression);

		if (parents.size() == 0) {
			LocationCategory parentCat = new LocationCategory();
			String parentId = UUID.randomUUID().toString();
			parentCat.setCategoryId(parentId);
			parentCat.setName(parentName);
			parentCat.setParentId("null");
			super.mapper().save(parentCat);

			//

			// the parent didnt exist, so the sub category definitely didnt
			// exist..
			LocationCategory subCat = new LocationCategory();
			String categoryId = UUID.randomUUID().toString();
			subCat.setCategoryId(categoryId);
			subCat.setName(name);
			subCat.setParentId(parentId);
			super.mapper().save(subCat);

			return categoryId;
		} else {
			DynamoDBScanExpression queryExpression2 = new DynamoDBScanExpression();

			queryExpression2.addFilterCondition(
					"name",
					new Condition().withComparisonOperator(
							ComparisonOperator.EQ).withAttributeValueList(
							new AttributeValue().withS(parentName)));

			queryExpression2.addFilterCondition("parentId", new Condition()
					.withComparisonOperator(ComparisonOperator.EQ)
					.withAttributeValueList(new AttributeValue().withS(null)));

			PaginatedScanList<LocationCategory> children = super.mapper().scan(
					LocationCategory.class, queryExpression);
			if (children.size() > 0) {
				return children.get(0).getCategoryId();

			} else {
				LocationCategory subCat = new LocationCategory();
				String categoryId = UUID.randomUUID().toString();
				subCat.setCategoryId(categoryId);
				subCat.setName(name);
				subCat.setParentId(parents.get(0).getCategoryId());
				super.mapper().save(subCat);
				return categoryId;
			}
		}

	}

	public List<Location> AddressFilter(Location loc) {

		Query query = new Query();
		query.field("locality").equal(loc.getLocality());
		query.field("region").equal(loc.getRegion());
		query.field("postcode").equal(loc.getPostcode());
		query.field("address").search(loc.getStreetAddress());

		query.limit(50);

		ReadResponse resp = factual.fetch("places", query);

		List<Map<String, Object>> data = resp.getData();

		List<Location> results = new ArrayList<Location>();
		for (Map<String, Object> map : data) {
			Location returnedLoc = new Location();
			String factualCategory = "";
			try {
				factualCategory = categoryFactory(map.get("category")
						.toString());
				returnedLoc.setCategoryId(factualCategory);
				returnedLoc.setCategory(map.get("category").toString());
			} catch (Exception exc) {
				// TODO what do I do when factual doesnt have a category for me?
			}

			// TODO make this into a helper method
			String country = (map.containsKey("country") == true) ? map.get(
					"country").toString() : "";
			String latitude = (map.containsKey("latitude") == true) ? map.get(
					"latitude").toString() : "";
			String longitude = (map.containsKey("longitude") == true) ? map
					.get("longitude").toString() : "";
			String locality = (map.containsKey("locality") == true) ? map.get(
					"locality").toString() : "";
			String name = (map.containsKey("name") == true) ? map.get("name")
					.toString() : "";
			String tel = (map.containsKey("tel") == true) ? map.get("tel")
					.toString() : "";
			String postcode = (map.containsKey("postcode") == true) ? map.get(
					"postcode").toString() : "";
			String region = (map.containsKey("region") == true) ? map.get(
					"region").toString() : "";
			String address = (map.containsKey("address") == true) ? map.get(
					"address").toString() : "";
			String website = (map.containsKey("website") == true) ? map.get(
					"website").toString() : "";

			returnedLoc.setCountryCode(country);
			returnedLoc.setLatitude(latitude);
			returnedLoc.setLongitude(longitude);
			returnedLoc.setLocality(locality);
			returnedLoc.setName(name);
			returnedLoc.setPhoneNumber(tel);
			returnedLoc.setPostcode(postcode);
			returnedLoc.setProviderId("factual");
			returnedLoc.setRegion(region);
			returnedLoc.setStreetAddress(address);
			returnedLoc.setWebSite(website);

			results.add(returnedLoc);

		}
		// need to return locations

		return results;
	}
}
