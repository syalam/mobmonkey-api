package com.MobMonkey.Helpers;

import java.util.List;

import com.MobMonkey.Models.Device;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;

public class NotificationHelper extends ResourceHelper {

	public NotificationHelper(){
		super();
	}
	
	public String[] getUserDevices(String eMailAddress) {
		DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
				new AttributeValue().withS(eMailAddress));

		List<Device> scanResult = super.mapper().query(Device.class,
				queryExpression);

		String[] deviceIds = new String[scanResult.size()];

		for (int i = 0; i < deviceIds.length; i++) {
			deviceIds[i] = scanResult.get(i).getDeviceId().toString();
		}

		return deviceIds;
	}
	
}
