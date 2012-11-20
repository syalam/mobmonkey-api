package com.MobMonkey.Helpers;

import java.io.IOException;

import net.spy.memcached.*;

public class MobMonkeyCache {
	private static MobMonkeyCache instance = null;
	private MemcachedClient[] m;

	protected MobMonkeyCache() {
		try {

			m = new MemcachedClient[21];

			for (int i = 0; i <= 20; i++) {

				MemcachedClient c = new MemcachedClient(
						new BinaryConnectionFactory(),
						AddrUtil.getAddresses("mobmonkey.otbiua.0001.usw1.cache.amazonaws.com:11211"));
				m[i] = c;
			}

		} catch (Exception e) {

		}
	}

	public static MobMonkeyCache getInstace() {
		if (instance == null) {
			instance = new MobMonkeyCache();
		}
		return instance;
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
