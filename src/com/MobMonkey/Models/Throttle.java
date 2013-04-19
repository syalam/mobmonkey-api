package com.MobMonkey.Models;
import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.*;

@DynamoDBTable( tableName = "Throttle")
public class Throttle {

	private String Id;	
	private Date hitDate;
	private String hitType;
	
	@DynamoDBHashKey
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	@DynamoDBRangeKey
	public Date getHitDate() {
		return hitDate;
	}
	public void setHitDate(Date hitDate) {
		this.hitDate = hitDate;
	}
	@DynamoDBAttribute
	public String getHitType() {
		return hitType;
	}
	public void setHitType(String hitType) {
		this.hitType = hitType;
	}
	
}
