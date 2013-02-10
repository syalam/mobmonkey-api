package com.MobMonkey.Helpers;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class MobMonkeyScheduler implements ServletContextListener {

	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			// Grab the Scheduler instance from the Factory
			
			StdSchedulerFactory factory = new StdSchedulerFactory();
			Scheduler scheduler = factory.getScheduler();
			// and start it off
			scheduler.start();
	
		
		} catch (SchedulerException se) {
			se.printStackTrace();
		}
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			// Grab the Scheduler instance from the Factory
			
			StdSchedulerFactory factory = new StdSchedulerFactory();
			Scheduler scheduler = factory.getScheduler();
			// and start it off
			scheduler.shutdown();
	
		
		} catch (SchedulerException se) {
			se.printStackTrace();
		}
		
	}

}
