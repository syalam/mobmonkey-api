package com.MobMonkey.Helpers;

import java.util.List;

import com.MobMonkey.Helpers.FactualHelper;
import com.MobMonkey.Models.Location;

public final class SearchHelper {

	public SearchHelper() {

	}

	public static List<Location> getLocationsByGeo(Location loc) {
		//TODO return MobMonkey locations by geo coordinates
		FactualHelper factual = new FactualHelper();
		return factual.GeoFilter(loc);

	}
	
	public static List<Location> getLocationsByAddress(Location loc){
		FactualHelper factual = new FactualHelper();
		return factual.AddressFilter(loc);
	}
	
	

}
