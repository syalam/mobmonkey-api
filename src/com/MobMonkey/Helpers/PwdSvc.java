package com.MobMonkey.Helpers;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.codec.binary.*;

public final class PwdSvc {
	private static PwdSvc instance;

	private PwdSvc() {
	}

	public synchronized String encrypt(String plaintext) throws Exception {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA"); // step 2
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e.getMessage());
		}
		try {
			md.update(plaintext.getBytes("UTF-8")); // step 3
		} catch (UnsupportedEncodingException e) {
			throw new Exception(e.getMessage());
		}

		byte raw[] = md.digest(); // step 4
		String hash = Base64.encodeBase64(raw, true).toString();
	
		return hash; // step 6
	}

	   public synchronized String decrypt(String string){
	        
	        //
	        // Decode a previously encoded string using decodeBase64 method and
	        // passing the byte[] of the encoded string.
	        //
	        byte[] decoded = Base64.decodeBase64(string.getBytes());
	 
	        //
	        // Print the decoded array
	        //
	       return Arrays.toString(decoded);
	 
	     
	    }
	
	public static synchronized PwdSvc getInstance() {
		if (instance == null) {
			return new PwdSvc();
		} else {
			return instance;
		}
	}

}
