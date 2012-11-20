package com.MobMonkey.Resources;

import java.io.IOException;

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

		User user = mapper.load(User.class, eMailAddress, partnerId);
		return user;
	}

	public Object getFromCache(String key) {
		Object o = null;
		try {
			o = MobMonkeyCache.getInstace().getCache().get(key);

		} catch (Exception exc) {

		}

		return o;
	}
	
	public void storeInCache(String key, int duration, Object o){
		try {
			MobMonkeyCache
					.getInstace()
					.getCache()
					.set(key,
							duration, o);
		} catch (Exception exc) {

		}
	}
	
	public void deleteFromCache(String key){
		try {
			MobMonkeyCache
					.getInstace()
					.getCache()
					.delete(key);
		} catch (Exception exc) {

		}
	}
}
