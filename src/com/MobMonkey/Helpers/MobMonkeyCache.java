package com.MobMonkey.Helpers;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.*;

public class MobMonkeyCache {
	private static MobMonkeyCache instance = null;
	private MemcachedClient[] m;

	protected MobMonkeyCache() {
		try {

			m = new MemcachedClient[21];

			for (int i = 0; i <= 20; i++) {

				try{
				MemcachedClient c = new MemcachedClient(
						new BinaryConnectionFactory(),
						AddrUtil.getAddresses("mobmonkey.otbiua.0001.usw1.cache.amazonaws.com:11211"));
				
				m[i] = c;
				}catch(Exception exc){
					
				}
			}

		} catch (Exception e) {

		}
	}

	public static MobMonkeyCache getInstance() {
		if (instance == null) {
			instance = new MobMonkeyCache();
		}
		return instance;
	}
	
	public Object getAsync(String key){
		java.util.concurrent.Future<Object> f = getCache().asyncGet(key);
		Object o = null;
		try {
			o = f.get(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}

	public MemcachedClient getCache() {
		MemcachedClient c = null;

		try {

			int i = (int) (Math.random() * 20);

			c = m[i];

		} catch (Exception e) {

		}

		return c;
	}

}
