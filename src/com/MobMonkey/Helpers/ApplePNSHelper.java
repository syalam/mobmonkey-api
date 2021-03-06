package com.MobMonkey.Helpers;

import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.MobMonkey.Models.Device;
import com.MobMonkey.Resources.ResourceHelper;

import javapns.*;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushedNotification;
import javapns.notification.ResponsePacket;

public final class ApplePNSHelper {

	private static String keyStoreFile = "ProdCertificates.p12";
	private static String keyStorePass = "mm";

	private static Logger logger = Logger.getRootLogger();

	public static void send(String eMailAddress, String[] devices, String msg, int badge) {

		// TODO sanitize the Id's before sending them

		InputStream keyStore = ApplePNSHelper.class
				.getResourceAsStream(keyStoreFile);
		try {

			// List<PushedNotification> notifications = Push.alert(msg,
			// keyStore,
			// keyStorePass, false, devices);

			List<PushedNotification> notifications = Push.combined(msg, badge,
					"", keyStore, keyStorePass, false, devices);
       
			for (PushedNotification notification : notifications) {
				if (notification.isSuccessful()) {
					/* Apple accepted the notification and should deliver it */
					logger.info("Push notification sent successfully to: "
							+ notification.getDevice().getToken());
					/* Still need to query the Feedback Service regularly */
				} else {
					String invalidToken = notification.getDevice().getToken();
					/* Add code here to remove invalidToken from your database */

					/* Find out more about what the problem was */
					Exception theProblem = notification.getException();
					logger.warn("Exception from Apple: "
							+ theProblem.getMessage());

					ResourceHelper rh = new ResourceHelper();
					Device d = (Device) rh.load(Device.class, eMailAddress, invalidToken);
					if(d != null){
						rh.delete(d, eMailAddress, invalidToken);
					}
					/*
					 * If the problem was an error-response packet returned by
					 * Apple, get it
					 */
					ResponsePacket theErrorResponse = notification
							.getResponse();
					if (theErrorResponse != null) {
						logger.warn("Unable to send apple push notification: "
								+ theErrorResponse.getMessage());
					}
				}
			}

		} catch (KeystoreException e) {
			/* A critical problem occurred while trying to use your keystore */
			e.printStackTrace();

		} catch (CommunicationException e) {
			/*
			 * A critical communication error occurred while trying to contact
			 * Apple servers
			 */
			e.printStackTrace();
		}
	}

	public static String testSend(String[] devices, String msg) {
		String result = "";
		InputStream keyStore = ApplePNSHelper.class
				.getResourceAsStream(keyStoreFile);
		try {

			List<PushedNotification> notifications = Push.alert(msg, keyStore,
					keyStorePass, false, devices);

			for (PushedNotification notification : notifications) {
				if (notification.isSuccessful()) {
					/* Apple accepted the notification and should deliver it */
					result = "Push notification sent successfully to: "
							+ notification.getDevice().getToken();
					/* Still need to query the Feedback Service regularly */
				} else {
					String invalidToken = notification.getDevice().getToken();
					/* Add code here to remove invalidToken from your database */

					/* Find out more about what the problem was */
					Exception theProblem = notification.getException();
					logger.warn("Exception from Apple: "
							+ theProblem.getMessage());

					/*
					 * If the problem was an error-response packet returned by
					 * Apple, get it
					 */
					ResponsePacket theErrorResponse = notification
							.getResponse();
					if (theErrorResponse != null) {
						logger.warn("Unable to send apple push notification: "
								+ theErrorResponse.getMessage());
					}
				}
			}

		} catch (KeystoreException e) {
			/* A critical problem occurred while trying to use your keystore */
			result = e.getMessage();

		} catch (CommunicationException e) {
			/*
			 * A critical communication error occurred while trying to contact
			 * Apple servers
			 */
			result = e.getMessage();
		}
		return result;
	}

}
