package com.MobMonkey.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;

import org.apache.log4j.Logger;
import com.MobMonkey.Helpers.MobMonkeyCache;
import com.MobMonkey.Models.Device;
import com.MobMonkey.Models.Partner;
import com.MobMonkey.Models.Throttle;
import com.MobMonkey.Models.User;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.elasticache.AmazonElastiCacheClient;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Region;
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
    private AmazonElasticTranscoderClient transClient;
	static final Logger logger = Logger.getRootLogger();
	static final String AWS_CREDENTIALS_FILE = "AwsCredentials.properties";
	static final String AWS_CRED_ERROR_NOT_FOUND = String.format(
			"\n\nCould not find %s\n\n\r", AWS_CREDENTIALS_FILE);
	static final String AWS_CRED_ERROR_READING = String.format(
			"\n\nUnable to read %s\n\n\r", AWS_CREDENTIALS_FILE);
	static final String FAIL_STAT = "Failure", SUCCESS = "Success";
	static final String INVALID_PARAM = "Invalid value: [%s]";
	static final String THANK_YOU_FOR_REGISTERING = "Thank you for registering!  Please validate your email by <a href=\"http://api.mobmonkey.com/rest/verify/user/%s/%s\">clicking here.</a>";
	static final String CREATING_USER_SUBJECT = "registration e-mail.",
			UPDATE_USER_SUBJECT = "Updated user account";
	static final Logger LOG = Logger.getLogger(UserResource.class);

	public static final String DOB_FORMAT = "MMMM d, yyyy";
	public static final SimpleDateFormat DOB_FORMATTER = new SimpleDateFormat(
			DOB_FORMAT, Locale.ENGLISH); // August 1 1960
	public static final int[] MALE_FEMALE_RANGE = { 0, 1 };
	static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	static final int MaxHitsIn24Hours = 5;
	static final int MaxHitsIn1Month = 30;
	final boolean isStaging = false;
	
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
			
			transClient = new AmazonElasticTranscoderClient(credentials);
	
			transClient.setEndpoint("elastictranscoder.us-west-1.amazonaws.com");
	
		

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

	public AmazonElasticTranscoderClient transClient(){
		return transClient;
	}
	public void sendAPNS(String eMailAddress, String[] deviceIds, String message, int badge)
			throws IOException {
		Object[] workflowInput = new Object[] { eMailAddress, deviceIds, message, badge };
		DataConverter converter = new JsonDataConverter();
		StartWorkflowExecutionRequest startWorkflowExecutionRequest = new StartWorkflowExecutionRequest();
		startWorkflowExecutionRequest.setInput(converter.toData(workflowInput));
		startWorkflowExecutionRequest.setDomain("MobMonkey");
		TaskList tasks = new TaskList();
		tasks.setName("Apns");
		startWorkflowExecutionRequest.setTaskList(tasks);
		WorkflowType workflowType = new WorkflowType();
		workflowType.setName("ApnsWorkflow.sendNotification");
		workflowType.setVersion("1.10");
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
		workflowType.setVersion("1.12");
		startWorkflowExecutionRequest.setWorkflowType(workflowType);
		startWorkflowExecutionRequest.setWorkflowId(UUID.randomUUID()
				.toString());
		this.swfClient().startWorkflowExecution(startWorkflowExecutionRequest);

	}

	public void sendNotification(String eMailAddress, Device[] devices, String message,
			int badgeCount) {
		for (Device d : devices) {
			if (d.getDeviceType().toLowerCase().equals("ios")) {
				String[] deviceId = new String[1];
				deviceId[0] = d.getDeviceId();

				try {
					this.sendAPNS(eMailAddress, deviceId, message, badgeCount);
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
		try {
			return headers.getRequestHeader(key).get(0);
		} catch (Exception exc) {
			return null;
		}
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

	protected static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
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

	public boolean validatePartnerId(String partnerId) {
		Partner p = (Partner) load(Partner.class, partnerId);
		if (p != null) {
			if (p.isEnabled()) {
				return true;
			}
		} else {
			return false;
		}
		return false;
	}

	public static String join(String separator, Object... items)
	{
		StringBuffer result = new StringBuffer();

		for (Object item : items)
		{
			if (result.length() > 0)
				result.append(separator);
			result.append(item);
		}

		return result.toString();
	}
	
	public boolean throttler(String eMailAddress, String partnerId){
		String _1MonthAgo = dateFormatter.format(new Date().getTime() - (30L * 24L * 60L * 60L * 1000L));
		Date _1DayAgo = new Date(new Date().getTime() - (24L * 60L * 60L * 1000L));
		String hashKey = eMailAddress + ":" + partnerId;
		String throttleKey = "throttle" + hashKey;
		DynamoDBQueryExpression query = new DynamoDBQueryExpression(new AttributeValue().withS(throttleKey));
		query.setRangeKeyCondition(new Condition().withComparisonOperator(ComparisonOperator.GT)
				.withAttributeValueList(new AttributeValue().withS(_1MonthAgo)));
		
		PaginatedQueryList<Throttle> results = mapper().query(Throttle.class, query);
			
		//check the cache first.. dont wanna go to dynamodb if we dont have to!
		Object o = this.getFromCache(throttleKey);
		if(o != null){
			return false;
		}
		if(results.size() > MaxHitsIn1Month){
			this.storeInCache(throttleKey, 60*60*12 , "true");
			return false;
		}
		int hitCount = 1;
		for(Throttle t : results){
			if(hitCount == MaxHitsIn24Hours){
				this.storeInCache(throttleKey, 60*30 , "true");
				return false;
			}
			if(t.getHitDate().compareTo(_1DayAgo) > 0){
				//within 24 hours, update hitcount
				hitCount++;
			}
		}
		return true;
			
	}

}
