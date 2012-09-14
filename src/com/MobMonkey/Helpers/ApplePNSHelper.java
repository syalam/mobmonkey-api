package com.MobMonkey.Helpers;
import java.io.InputStream;

import javapns.*;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;

public final class ApplePNSHelper {

	private static String keyStoreFile = "Certificates.p12"; 
	private static String keyStorePass = "1MobMonkey23";
	public static void send(String deviceId, String msg){
	
		InputStream keyStore = ApplePNSHelper.class.getResourceAsStream(keyStoreFile);
		try {
			Push.alert(msg, keyStore, keyStorePass, true, deviceId);
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeystoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
