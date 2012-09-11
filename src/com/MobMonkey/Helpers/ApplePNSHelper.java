package com.MobMonkey.Helpers;
import javapns.*;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;

final class ApplePNSHelper {

	private String keyStoreFile = "Certificates.p12"; 
	private String keyStorePass = "1MobMonkey23";
	public ApplePNSHelper(String deviceId, String msg){
		
		try {
			Push.alert(msg, keyStoreFile, keyStorePass, false, deviceId);
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeystoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
