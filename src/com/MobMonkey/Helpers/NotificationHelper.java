package com.MobMonkey.Helpers;

import java.util.ArrayList;
import java.util.List;

import com.MobMonkey.Models.Device;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;

public class NotificationHelper extends ResourceHelper {

	public NotificationHelper() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Device[] getUserDevices(String eMailAddress) {

		List<Device> results = new ArrayList<Device>();
		results = (List<Device>) super.getFromCache("DEV" + eMailAddress);

		if (results == null) {
			DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression(
					new AttributeValue().withS(eMailAddress));

			results = super.mapper().query(Device.class, queryExpression);

			super.storeInCache("DEV" + eMailAddress, 259200,
					results.subList(0, results.size()));
		}

		
		Device[] deviceIds = results.toArray(new Device[results.size()]);

		return deviceIds;
	}

}
