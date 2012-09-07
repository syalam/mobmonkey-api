package com.MobMonkey.Models;

public class Message {
	private String PartnerId;
	private String eMailAddress;
	
	public String getPartnerId(){
		return PartnerId;
	}
	
	public void setPartnerId(String PartnerId){
		this.PartnerId = PartnerId;
	}

	public String geteMailAddress() {
		return eMailAddress;
	}

	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

}

