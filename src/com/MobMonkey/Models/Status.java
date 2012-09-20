package com.MobMonkey.Models;

public class Status {
	private String status;
	private String description;
	private String id;
	
	public Status(){
		
	}
	
	public Status(String status, String description, String id){
		this.status = status;
		this.description = description;
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	

}
