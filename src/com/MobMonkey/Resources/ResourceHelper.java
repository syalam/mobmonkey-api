package com.MobMonkey.Resources;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.HttpHeaders;

import com.MobMonkey.Helpers.MobMonkeyCache;
import com.MobMonkey.Models.User;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.elasticache.AmazonElastiCacheClient;

public class ResourceHelper {
	private AmazonS3Client s3cli;
	private AWSCredentials credentials;
	private AmazonDynamoDBClient ddb;
	private DynamoDBMapper mapper;
	private AmazonElastiCacheClient ecCli;

	public ResourceHelper() {
		try {
			credentials = new PropertiesCredentials(getClass().getClassLoader()
					.getResourceAsStream("AwsCredentials.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		s3cli = new AmazonS3Client();
		ddb = new AmazonDynamoDBClient(credentials);
		ddb.setEndpoint("https://dynamodb.us-west-1.amazonaws.com", "dynamodb",
				"us-west-1");
		mapper = new DynamoDBMapper(ddb);

	}

	public DynamoDBMapper mapper() {
		return mapper;
	}

	public AWSCredentials credentials() {
		return credentials;
	}

	public AmazonS3Client s3cli() {
		return s3cli;
	}

	public AmazonElastiCacheClient ecCli() {
		return ecCli;
	}

	public User getUser(HttpHeaders headers) {

		String eMailAddress = headers.getRequestHeader("MobMonkey-user").get(0);
		String partnerId = headers.getRequestHeader("MobMonkey-partnerId").get(
				0);
		User user = new User();
		if (headers.getRequestHeader("OauthToken") != null) {
			user.seteMailAddress(eMailAddress);
			user.setPartnerId(partnerId);

		} else {

			user = mapper.load(User.class, eMailAddress, partnerId);
		}
		return user;
	}

	public Object getFromCache(String key) {
		Object o = null;
		try {
			o = MobMonkeyCache.getInstance().getAsync(key);

		} catch (Exception exc) {

		}

		return o;
	}

	public void storeInCache(String key, int duration, Object o) {
		try {
			MobMonkeyCache.getInstance().getCache().set(key, duration, o);
		} catch (Exception exc) {

		}
	}

	public void deleteFromCache(String key) {
		try {
			MobMonkeyCache.getInstance().getCache().delete(key);
		} catch (Exception exc) {

		}
	}

	static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
				new Comparator<Map.Entry<K, V>>() {
					@Override
					public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
						int res = e2.getValue().compareTo(e1.getValue());
						return res != 0 ? res : 1; // Special fix to preserve
													// items with equal values
					}
				});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	public String MediaType(int i) {
		if (i == 1) {
			return "image";
		}
		if (i == 2) {
			return "video";
		}
		if (i == 3) {
			return "live streaming";
		}
		if (i == 4) {
			return "text";
		} else {
			return "unknown";
		}
	}

	public Object load(Class<?> c, String hashKey) {
		Object o = null;
		o = this.getFromCache(hashKey);
		if (o == null) {
			try {
				o = this.mapper().load(c, hashKey);
			} catch (Exception exc) {
				return null;
			}
		}
		this.storeInCache(hashKey, 259200, o);
		return o;
	}

	public Object load(Class<?> c, String hashKey, String rangeKey) {
		Object o = null;
		o = this.getFromCache(hashKey + ":" + rangeKey);
		if (o == null) {
			try {
				o = this.mapper().load(c, hashKey, rangeKey);
			} catch (Exception exc) {
				return null;
			}
		}
		this.storeInCache(hashKey, 259200, o);
		return o;
	}
	
	public void delete(Object o, String hashKey){
		this.mapper().delete(o);
		this.deleteFromCache(hashKey);
	}
	
	public void delete(Object o, String hashKey, String rangeKey){
		this.mapper().delete(o);
		this.deleteFromCache(hashKey + ":" + rangeKey);
	}
	
	public void save(Object o, String hashKey){
		this.mapper().save(o);
		this.storeInCache(hashKey, 259200, o);
	}
	
	public void save(Object o, String hashKey, String rangeKey){
		this.mapper().save(o);
		this.storeInCache(hashKey +":"+ rangeKey, 259200, o);
	}
}
