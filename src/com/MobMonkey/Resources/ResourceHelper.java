package com.MobMonkey.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;

import org.apache.log4j.Logger;
import com.MobMonkey.Helpers.MobMonkeyCache;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.User;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.elasticache.AmazonElastiCacheClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.flow.DataConverter;
import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;

public class ResourceHelper {
	private AmazonS3Client s3cli;
	private AWSCredentials credentials;
	private AmazonDynamoDBClient ddb;
	private DynamoDBMapper mapper;
	private AmazonElastiCacheClient ecCli;
	private AmazonSimpleWorkflow swfClient;
	static final Logger logger = Logger.getRootLogger();
	static final String AWS_CREDENTIALS_FILE = "AwsCredentials.properties";
	static final String AWS_CRED_ERROR_NOT_FOUND = String.format(
			"\n\nCould not find %s\n\n\r", AWS_CREDENTIALS_FILE);
	static final String AWS_CRED_ERROR_READING = String.format(
			"\n\nUnable to read %s\n\n\r", AWS_CREDENTIALS_FILE);
	static final String FAIL_STAT = "Failure", SUCCESS = "Success";
	
	public ResourceHelper() {
		InputStream credentialsStream = getClass().getClassLoader()
				.getResourceAsStream(AWS_CREDENTIALS_FILE);
		if (credentialsStream != null) {
			try {
				credentials = new PropertiesCredentials(credentialsStream);
			} catch (IOException e) {
				logger.error(AWS_CRED_ERROR_READING);
			}

			s3cli = new AmazonS3Client();
			ddb = new AmazonDynamoDBClient(credentials);
			ddb.setEndpoint("https://dynamodb.us-west-1.amazonaws.com",
					"dynamodb", "us-west-1");
			mapper = new DynamoDBMapper(ddb);

			swfClient = new AmazonSimpleWorkflowClient(credentials);
			swfClient.setEndpoint("http://swf.us-west-1.amazonaws.com");

		} else {
			logger.error(AWS_CRED_ERROR_NOT_FOUND);
		}
	}

	public ResourceHelper(PropertiesCredentials credentials,
			AmazonS3Client awsClient, AmazonDynamoDBClient dbClient,
			DynamoDBMapper dynamoMapper) {
		this.credentials = credentials;
		this.s3cli = awsClient;
		this.ddb = dbClient;
		this.mapper = dynamoMapper;

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

	public AmazonSimpleWorkflow swfClient() {
		return swfClient;
	}

	public void sendAPNS(String[] deviceIds, String message, int badge)
			throws IOException {
		Object[] workflowInput = new Object[] { deviceIds, message, badge };
		DataConverter converter = new JsonDataConverter();
		StartWorkflowExecutionRequest startWorkflowExecutionRequest = new StartWorkflowExecutionRequest();
		startWorkflowExecutionRequest.setInput(converter.toData(workflowInput));
		startWorkflowExecutionRequest.setDomain("MobMonkey");
		TaskList tasks = new TaskList();
		tasks.setName("Apns");
		startWorkflowExecutionRequest.setTaskList(tasks);
		WorkflowType workflowType = new WorkflowType();
		workflowType.setName("ApnsWorkflow.sendNotification");
		workflowType.setVersion("1.7");
		startWorkflowExecutionRequest.setWorkflowType(workflowType);
		startWorkflowExecutionRequest.setWorkflowId(UUID.randomUUID()
				.toString());
		this.swfClient().startWorkflowExecution(startWorkflowExecutionRequest);

	}

	public void sendGCM(String registration_id, String message, int badge)
			throws IOException {

		Object[] workflowInput = new Object[] { registration_id, message, badge };
		DataConverter converter = new JsonDataConverter();
		StartWorkflowExecutionRequest startWorkflowExecutionRequest = new StartWorkflowExecutionRequest();
		startWorkflowExecutionRequest.setInput(converter.toData(workflowInput));
		startWorkflowExecutionRequest.setDomain("MobMonkey");
		TaskList tasks = new TaskList();
		tasks.setName("Gcm");
		startWorkflowExecutionRequest.setTaskList(tasks);
		WorkflowType workflowType = new WorkflowType();
		workflowType.setName("GcmWorkflow.sendNotification");
		workflowType.setVersion("1.5");
		startWorkflowExecutionRequest.setWorkflowType(workflowType);
		startWorkflowExecutionRequest.setWorkflowId(UUID.randomUUID()
				.toString());
		this.swfClient().startWorkflowExecution(startWorkflowExecutionRequest);

	}

	public void sendNotification(Device[] devices, String message,
			int badgeCount) {
		for (Device d : devices) {
			if (d.getDeviceType().toLowerCase().equals("ios")) {
				String[] deviceId = new String[1];
				deviceId[0] = d.getDeviceId();

				try {
					this.sendAPNS(deviceId, message, badgeCount);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
				}

			} else if (d.getDeviceType().toLowerCase().equals("android")) {

				try {
					this.sendGCM(d.getDeviceId(), message, badgeCount);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
				}
			}
		}
	}

	// private static String toBase64String(Serializable o) throws IOException {
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// ObjectOutputStream oos = new ObjectOutputStream(baos);
	// oos.writeObject(o);
	// oos.close();
	// return new String(Base64.encode(baos.toByteArray()));
	// }

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

	public static String getHeaderParam(String key, HttpHeaders headers) {
		return headers.getRequestHeader(key).get(0); //?
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

	public Object load(Class<?> c, Object hashKey) {
		Object o = null;
		o = this.getFromCache(c.getName() + hashKey);
		if (o == null) {
			try {
				o = this.mapper().load(c, hashKey);
			} catch (Exception exc) {
				return null;
			}
		}
		this.storeInCache(c.getName() + hashKey, 259200, o);
		return o;
	}

	public Object load(Class<?> c, Object hashKey, Object rangeKey) {
		Object o = null;
		o = this.getFromCache(c.getName() + hashKey + ":" + rangeKey);
		if (o == null) {
			try {
				o = this.mapper().load(c, hashKey, rangeKey);
			} catch (Exception exc) {
				return null;
			}
		}
		this.storeInCache(c.getName() + hashKey, 259200, o);
		return o;
	}

	public void delete(Object o, Object hashKey) {
		this.mapper().delete(o);
		this.deleteFromCache(o.getClass().getName() + hashKey);
	}

	public void delete(Object o, Object hashKey, Object rangeKey) {
		this.mapper().delete(o);
		this.deleteFromCache(o.getClass().getName() + hashKey + ":" + rangeKey);
	}

	public void save(Object o, Object hashKey) {
		this.mapper().save(o);
		this.storeInCache(o.getClass().getName() + hashKey, 259200, o);
	}

	public void save(Object o, Object hashKey, Object rangeKey) {
		this.mapper().save(o);
		this.storeInCache(o.getClass().getName() + hashKey + ":" + rangeKey,
				259200, o);
	}

	public void clearCountCache(Object eMailAddress) {
		this.deleteFromCache("OPENCOUNT:" + eMailAddress);
		this.deleteFromCache("FULFILLEDUNREADCOUNT:" + eMailAddress);
		this.deleteFromCache("FULFILLEDREADCOUNT:" + eMailAddress);
		this.deleteFromCache("ASSIGNEDREADCOUNT:" + eMailAddress);
		this.deleteFromCache("ASSIGNEDREADCOUNT:" + eMailAddress);
	}

}
