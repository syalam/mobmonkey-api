package com.MobMonkey.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.LocationCategory;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.factual.driver.*;

public class FactualHelper extends ResourceHelper {
	private Factual factual;

	public FactualHelper() {
		super();
		factual = new Factual("BEoV3TPDev03P6NJSVJPgTmuTNOegwRsjJN41DnM",
				"hwxVQz4lAxb5YpWhbLq10KhWiEw5k35WgFuoR2YI");

	}

	public String GeoFilter(Location loc) {
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

			returnedLoc.setCategoryId(map.get("category").toString());
			returnedLoc.setRegion(map.get("region").toString());

			results.add(returnedLoc);

		}
		// need to return locations

		return resp.getJson();
	}

	private String categoryFactory(String cat) {
		String[] catArray = cat.split(">");
		String parentName = catArray[0].trim();
		String name = catArray[1].trim();

		// Create the parent category.. we should check to see if it exists
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue(parentName));
		Condition rangeKeyCondition = new Condition().withComparisonOperator(
				ComparisonOperator.EQ.toString()).withAttributeValueList(
				new AttributeValue().withS(null));
		queryExpression.setRangeKeyCondition(rangeKeyCondition);
		if (super.mapper().count(LocationCategory.class, queryExpression) == 0) {
			LocationCategory parentCat = new LocationCategory();
			parentCat.setName(name);
			parentCat.setParentName(null);
			super.mapper().save(parentCat);
			
			//the parent didnt exist, so the sub category definitely didnt exist..
			LocationCategory subCat = new LocationCategory();
			subCat.setName(name);
			subCat.setParentName(parentName);
			
		}

	}
}
