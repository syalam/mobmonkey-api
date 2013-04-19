package com.MobMonkey.Helpers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.LocationCategory;
import com.MobMonkey.Resources.ResourceHelper;
import com.MobMonkey.Helpers.MobMonkeyCache;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.factual.driver.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class FactualHelper extends ResourceHelper {
	private Factual factual;
	private static String factual_providerId = "222e736f-c7fa-4c40-b78e-d99243441fae";

	public FactualHelper() {
		super();
		factual = new Factual("BEoV3TPDev03P6NJSVJPgTmuTNOegwRsjJN41DnM",
				"hwxVQz4lAxb5YpWhbLq10KhWiEw5k35WgFuoR2YI");

	}

	public Location reverseLookUp(String locationId) {

		// TODO Working for only factual, need to add MobMonkey
		// Check to see if it's in the cache first

		Location result = null;

		Object o = super.load(Location.class, locationId, factual_providerId);
		if (o != null) {
			return (Location) o;
		}

		Query query = new Query();
		query.field("factual_id").equal(locationId);

		ReadResponse resp = factual.fetch("places-v3", query);

		List<Map<String, Object>> data = resp.getData();
		for (Map<String, Object> map : data) {
			// Add to the catch when I create mobmonkey location
			result = this.createMobMonkeyLocation(map);
			super.storeInCache(locationId + ":" + factual_providerId, 259200,
					result);

		}
		return result;
	}

	public List<Location> GeoFilter(Location loc) {
		int radiusInMeters = (int) (Integer.parseInt(loc.getRadiusInYards()) * .9144); // convert
																						// yards
																						// to
																						// meters
		Query query = new Query();
		if (loc.getLongitude() != null && loc.getLatitude() != null
				&& radiusInMeters > 0) {
			query = new Query().within(new Circle(Double.parseDouble(loc
					.getLatitude()), Double.parseDouble(loc.getLongitude()),
					radiusInMeters));
		}
		if (loc.getName() != null) {
			query.field("name").search(loc.getName());
		}
		if (loc.getCategoryIds() != null) {
			query.field("category_ids").search(loc.getCategoryIds());
		}
		if (radiusInMeters == 0) {
			query.search(loc.getName());
		}
		query.limit(25);
		ReadResponse resp = factual.fetch("places-v3", query);
		List<Map<String, Object>> data = resp.getData();

		List<Location> results = new ArrayList<Location>();
		for (Map<String, Object> map : data) {

			Location returnedLoc = this.createMobMonkeyLocation(map);
			results.add(returnedLoc);

		}
		// need to return locations

		return results;
	}

	public List<Location> AddressFilter(Location loc) {

		Query query = new Query();
		query.field("locality").equal(loc.getLocality());
		query.field("region").equal(loc.getRegion());
		query.field("postcode").equal(loc.getPostcode());
		query.field("address").search(loc.getAddress());

		query.limit(20);

		ReadResponse resp = factual.fetch("places-v3", query);

		List<Map<String, Object>> data = resp.getData();

		List<Location> results = new ArrayList<Location>();
		for (Map<String, Object> map : data) {
			Location returnedLoc = this.createMobMonkeyLocation(map);

			results.add(returnedLoc);
		}
		// need to return locations

		return results;
	}

	private Location createMobMonkeyLocation(Map<String, Object> map) {
		Location returnedLoc = new Location();

		String locationId = (map.containsKey("factual_id") == true) ? map.get(
				"factual_id").toString() : "";
		String country = (map.containsKey("country") == true) ? map.get(
				"country").toString() : "";
		String latitude = (map.containsKey("latitude") == true) ? map.get(
				"latitude").toString() : "";
		String longitude = (map.containsKey("longitude") == true) ? map.get(
				"longitude").toString() : "";
		String locality = (map.containsKey("locality") == true) ? map.get(
				"locality").toString() : "";
		String name = (map.containsKey("name") == true) ? map.get("name")
				.toString() : "";
		String tel = (map.containsKey("tel") == true) ? map.get("tel")
				.toString() : "";
		String postcode = (map.containsKey("postcode") == true) ? map.get(
				"postcode").toString() : "";
		String region = (map.containsKey("region") == true) ? map.get("region")
				.toString() : "";
		String address = (map.containsKey("address") == true) ? map.get(
				"address").toString() : "";
		String address_ext = (map.containsKey("address_ext") == true) ? map
				.get("address").toString() : "";
		String website = (map.containsKey("website") == true) ? map.get(
				"website").toString() : "";
		String category_ids = (map.containsKey("category_ids") == true) ? map
				.get("category_ids").toString() : "";
		String category_labels = (map.containsKey("category_labels") == true) ? map
				.get("category_labels").toString() : "";
		String neighborhood = (map.containsKey("neighborhood") == true) ? map
				.get("neighborhood").toString() : "";
		String distance = (map.containsKey("$distance") == true) ? map.get(
				"$distance").toString() : "";

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
		returnedLoc.setAddress(address);
		returnedLoc.setAddress_ext(address_ext);
		returnedLoc.setWebSite(website);
		returnedLoc.setCategoryIds(category_ids);
		returnedLoc.setCategoryLabels(this.FixCategoryLabel(category_labels));
		returnedLoc.setNeighborhood(neighborhood);
		returnedLoc.setDistance(distance);

		return returnedLoc;
	}

	private String FixCategoryLabel(String catLabel) {
		String result = "";
		String test = catLabel.replaceAll("^\\W", "").replaceAll("\\W$", "");
		String[] categories = test.split("\\],\\[");

		int count = 1;
		for (String cat : categories) {
			String tmp = "";
			String[] tmp2 = cat.replaceAll("\\[", "").replaceAll("\\]", "")
					.split("\",\"");
			int cnt = 1;
			for (String catH : tmp2) {
				catH = catH.replaceAll("\"", "");
				if (cnt == tmp2.length) {
					tmp += catH;
				} else {
					tmp += catH + " > ";
				}
				cnt++;
			}

			if (count == categories.length) {
				result += tmp;
			} else {
				result += tmp + " | ";
			}
			count++;
		}
		return result;

	}

	public String LoadCategories() {
		List<LocationCategory> catsToSave = new ArrayList<LocationCategory>();

		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(
				"https://raw.github.com/Factual/places/master/categories/factual_taxonomy.json");

		try {
			HttpResponse resp = client.execute(httpGet);
			HttpEntity entity = resp.getEntity();

			StringWriter writer = new StringWriter();
			IOUtils.copy(entity.getContent(), writer, "UTF-8");
			String theString = writer.toString();

			catsToSave = this.JsonToCategory(theString);
			try {

				EntityUtils.consume(entity);
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}

		super.mapper().batchSave(catsToSave);
		return "Saved " + catsToSave.size() + " categories to the database.";

	}

	private List<LocationCategory> JsonToCategory(String theString) {
		List<LocationCategory> results = new ArrayList<LocationCategory>();
		JsonParser parser = new JsonParser();
		JsonObject jo = parser.parse(theString).getAsJsonObject();

		for (int i = 1; i < 500; i++) {
			LocationCategory locCat = new LocationCategory();
			JsonElement elem = jo.get(String.valueOf(i));
			JsonObject job = new JsonObject();
			try {
				job = parser.parse(elem.toString()).getAsJsonObject();
			} catch (Exception exc) {

			}
			try {
				locCat.setCategoryId(String.valueOf(i));
				String parents = (job.has("parents") == true) ? job.get(
						"parents").toString() : "0";

				locCat.setParents(parents.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", ""));

				JsonObject labels = parser.parse(job.get("labels").toString())
						.getAsJsonObject();

				locCat.setEn(labels.get("en").toString().replaceAll("\"", ""));
				locCat.setDe(labels.get("de").toString().replaceAll("\"", ""));
				locCat.setIt(labels.get("it").toString().replaceAll("\"", ""));
				locCat.setEs(labels.get("es").toString().replaceAll("\"", ""));
				locCat.setFr(labels.get("fr").toString().replaceAll("\"", ""));
				locCat.setKr(labels.get("kr").toString().replaceAll("\"", ""));
				locCat.setJp(labels.get("jp").toString().replaceAll("\"", ""));
				locCat.setZh(labels.get("zh").toString().replaceAll("\"", ""));
				locCat.setZh_hant(labels.get("zh-hant").toString()
						.replaceAll("\"", ""));

				results.add(locCat);
			} catch (Exception exc) {

			}
		}
		return results;
	}
}
