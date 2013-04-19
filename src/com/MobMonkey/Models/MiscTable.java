package com.MobMonkey.Models;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName = "MiscTable")
public class MiscTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8671887992894178741L;
	@JsonIgnore private String miscId;
	private String miscRange;
	private String Value;
	
	@DynamoDBHashKey
	public String getMiscId() {
		return miscId;
	}
	public void setMiscId(String miscId) {
		this.miscId = miscId;
	}
	@DynamoDBRangeKey
	public String getMiscRange() {
		return miscRange;
	}
	public void setMiscRange(String miscRange) {
		this.miscRange = miscRange;
	}
	@DynamoDBAttribute
	public String getValue() {
		return Value;
	}
	public void setValue(String value) {
		Value = value;
	}
	
	
	
}
