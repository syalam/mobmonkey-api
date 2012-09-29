package com.MobMonkey.Resources;

import java.io.IOException;

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
	
		
		ecCli = new AmazonElastiCacheClient();
	//	ecCli.setEndpoint("mobmonkey.otbiua.0001.usw1.cache.amazonaws.com");
	
		


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
	public AmazonElastiCacheClient ecCli(){
		return ecCli;
	}

}
