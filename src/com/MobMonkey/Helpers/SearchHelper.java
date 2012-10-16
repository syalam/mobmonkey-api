package com.MobMonkey.Helpers;

import java.util.ArrayList;
import java.util.List;

import com.MobMonkey.Helpers.FactualHelper;
import com.MobMonkey.Models.Location;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;

public final class SearchHelper extends ResourceHelper {

	public SearchHelper() {
		super();
	}

	public  List<Location> getLocationsByGeo(Location loc, String searchString) {
	   //TODO add limiting, and paging
		List<Location> results = new ArrayList<Location>();
		
		FactualHelper factual = new FactualHelper();
		List<Location> factualLocs = factual.GeoFilter(loc, searchString);
		List<Location> mobMonkeyLocs = getMobMonkeyLocationsByGeo(loc);
		
		results.addAll(mobMonkeyLocs);
		results.addAll(factualLocs);
		return results;
	}

	public List<Location> getMobMonkeyLocationsByGeo(Location loc) {

		List<Location> results = new ArrayList<Location>();

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

		PaginatedScanList<Location> locs = super.mapper().scan(Location.class,
				scanExpression);
		for (Location location : locs) {
			if (Locator.isInVicinity(location.getLatitude(),
					location.getLongitude(), loc.getLatitude(),
					loc.getLongitude(), Integer.parseInt(loc.getRadiusInYards()))) {
				results.add(location);
			}
		}
		return results;
	}

	public static List<Location> getLocationsByAddress(Location loc) {
		FactualHelper factual = new FactualHelper();
		return factual.AddressFilter(loc);
	}

}
