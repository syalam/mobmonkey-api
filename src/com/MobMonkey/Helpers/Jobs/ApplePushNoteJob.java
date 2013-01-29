package com.MobMonkey.Helpers.Jobs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.MobMonkey.Helpers.ApplePNSHelper;
import org.bouncycastle.util.encoders.Base64;

public class ApplePushNoteJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		JobKey key = arg0.getJobDetail().getKey();
		JobDataMap dataMap = arg0.getJobDetail().getJobDataMap();
		String base64deviceIds = dataMap.getString("deviceIds");
		String message = dataMap.getString("message");

		String[] devIds = null;
		try {
			devIds = (String[]) fromString(base64deviceIds);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ApplePNSHelper.send(devIds, message);

	}

	private static Object fromString(String s) throws IOException,
			ClassNotFoundException {
		byte[] data = Base64.decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
				data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}
}
