package com.MobMonkey.Models;

import java.io.Serializable;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable( tableName="LocationCategory")
public class LocationCategory implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8971134676313436098L;
	private String categoryId;
	private String parents;
	private String en;
	private String de;
	private String it;
	private String es;
	private String fr;
	private String kr;
	private String jp;
	private String zh;
	private String zh_hant;
	
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
	public String getParents() {
		return parents;
	}

	public void setParents(String parents) {
		this.parents = parents;
	}

	@DynamoDBAttribute
	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en;
	}
	@DynamoDBAttribute
	public String getDe() {
		return de;
	}

	public void setDe(String de) {
		this.de = de;
	}
	@DynamoDBAttribute
	public String getIt() {
		return it;
	}

	public void setIt(String it) {
		this.it = it;
	}
	@DynamoDBAttribute
	public String getEs() {
		return es;
	}

	public void setEs(String es) {
		this.es = es;
	}
	@DynamoDBAttribute
	public String getFr() {
		return fr;
	}

	public void setFr(String fr) {
		this.fr = fr;
	}
	@DynamoDBAttribute
	public String getKr() {
		return kr;
	}

	public void setKr(String kr) {
		this.kr = kr;
	}
	@DynamoDBAttribute
	public String getJp() {
		return jp;
	}

	public void setJp(String jp) {
		this.jp = jp;
	}
	@DynamoDBAttribute
	public String getZh() {
		return zh;
	}

	public void setZh(String zh) {
		this.zh = zh;
	}
	@DynamoDBAttribute
	public String getZh_hant() {
		return zh_hant;
	}

	public void setZh_hant(String zh_hant) {
		this.zh_hant = zh_hant;
	}

	
}