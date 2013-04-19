package com.MobMonkey.Models;

import java.io.Serializable;
import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;
@DynamoDBTable( tableName = "Statistics")
public class Statistics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9029792149041702471L;

	private String statisticId;
	private Date date;
	
	@DynamoDBHashKey
	public String getStatisticId() {
		return statisticId;
	}
	public void setStatisticId(String statisticId) {
		this.statisticId = statisticId;
	}
	
	@DynamoDBRangeKey
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	
}
