package com.MobMonkey.Models;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName="LocationCategory")
public class LocationCategory {
	
	private String name;
	private String parentName;
	
	public LocationCategory(){	
	}
	

	@DynamoDBHashKey
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@DynamoDBRangeKey
	public String getParentName() {
		return parentName;
	}
	
	public void setParentName(String parentId) {
		this.parentName = parentId;
	}
	
}
