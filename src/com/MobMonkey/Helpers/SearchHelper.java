package com.MobMonkey.Helpers;

import java.util.List;

import com.MobMonkey.Helpers.FactualHelper;
import com.MobMonkey.Models.Location;

public final class SearchHelper {
	
	public SearchHelper(){
		
	}
	
	public static List<Location> getLocationsByGeo(Location loc){
		FactualHelper factual = new FactualHelper();
		factual.GeoFilter(loc);
		
		return null;
		
	}

}
