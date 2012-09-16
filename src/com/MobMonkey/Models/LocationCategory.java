package com.MobMonkey.Models;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName="LocationCategory")
public class LocationCategory {
	
	private String categoryId;
	private String name;
	private String parentId;
	
	public LocationCategory(){	
	}
	
	@DynamoDBHashKey
	public String getCategoryId() {
		return categoryId;
	}


	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	@DynamoDBAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@DynamoDBRangeKey
	public String getParentId() {
		return parentId;
	}
	
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
}
