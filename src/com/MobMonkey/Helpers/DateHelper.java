package com.MobMonkey.Helpers;

import java.util.Calendar;
import java.util.Date;

public class DateHelper {
	
	    public static Date addDays(Date date, int days)
	    {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(date);
	        cal.add(Calendar.DATE, days); //minus number would decrement the days
	        return cal.getTime();
	    }
	}

