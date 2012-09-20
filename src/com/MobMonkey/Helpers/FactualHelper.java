package com.MobMonkey.Helpers;

import java.util.List;
import java.util.Map;

import com.MobMonkey.Models.Location;
import com.factual.driver.*;

public class FactualHelper {
	private Factual factual;

	public FactualHelper() {
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

		for (Map<String, Object> map : data) {

		}

		return resp.getJson();
	}
}
