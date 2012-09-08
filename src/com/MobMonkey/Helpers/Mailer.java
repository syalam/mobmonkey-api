package com.MobMonkey.Helpers;


import java.util.*;


import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.auth.*;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

public class Mailer {

	private AmazonSimpleEmailServiceClient ses;
	private BasicAWSCredentials credentials;

	public Mailer() {

		String keyID = "AKIAIIA57O567JMQJL4A";
        String secretKey = "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";
        BasicAWSCredentials credentials = new BasicAWSCredentials(keyID, secretKey);
       
        ses = new AmazonSimpleEmailServiceClient(credentials);
      
	}

	public void sendMail(String to, String subject, String body) {

		SendEmailRequest sendEmailRequest = constructMail(to, subject, body);

		ses.sendEmail(sendEmailRequest);
	}

	private SendEmailRequest constructMail(String to, String subject, String bdy) {

		//Construct email request and set creds
		SendEmailRequest msg = new SendEmailRequest();
		msg.setRequestCredentials(credentials);

		
		msg.setSource("The Big Chimp <bigchimp@mobmonkey.com>");
		
		// Setup the To: field
		Destination dest = new Destination();
		List<String> toAddresses = new ArrayList<String>();
		toAddresses.add(to);
		dest.setToAddresses(toAddresses);
		msg.setDestination(dest);

		// Setup BCC to the team
		List<String> bccAddresses = new ArrayList<String>();
		bccAddresses.add("mannyahuerta@gmail.com");
		// bccAddresses.add("tim.baldin@mobmonkey.com");
		// bccAddresses.add("syalam@gmail.com");
		// bccAddresses.add("reyad.sidique@gmail.com");
		dest.setBccAddresses(bccAddresses);

		// Setup Reply-To
		List<String> replyToAddresses = new ArrayList<String>();
		replyToAddresses.add("mannyahuerta@gmail.com");
		// replyToAddresses.add("tim.baldin@mobmonkey.com");
		// replyToAddresses.add("syalam@gmail.com");
		// replyToAddresses.add("reyad.sidique@gmail.com");
		//msg.setReplyToAddresses(replyToAddresses);

		// Create the message
		Message message = new Message();
		Body body = new Body();
		Content html = new Content();
		html.setData("<html><body><center><h1>Header - Official MobMonkey Parcel</h1><p>" + bdy + "<p><font size=\"2\">MobMonkey Footer, &copy; 2012. All rights reserved.</font><center></body></html>");
		body.setHtml(html);
		message.setBody(body);
		Content sub = new Content();
		sub.setData("MobMonkey " + subject);
		message.setSubject(sub);
		msg.setMessage(message);

		return msg;
	}
}