/**
 * 
 */
package com.diquest.scopus;

import java.util.HashMap;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

/**
 * @author neon
 * @date 2013. 4. 19.
 * @Version 1.0
 */
public class MongoDBConnector {

	static HashMap<String, MongoClient> mcset = new HashMap<String, MongoClient>();

	static MongoClient mc = null;

	private MongoDBConnector(String ip, int port) throws Exception {
		try {
			mc = new MongoClient(ip, port);
			mc.setWriteConcern(WriteConcern.NORMAL);
		} catch (Exception e) {
			throw e;
		}
	}

	public static synchronized MongoClient getInstance(String ip, int port) throws Exception {
		return getInstance(ip, port, String.valueOf(System.nanoTime()));
	}

	public static synchronized MongoClient getInstance(String ip, int port, String name) throws Exception {
		String k = ip + "_" + port + ":" + name;
		mc = mcset.get(k);
		if (mc == null) {
			try {
				mc = new MongoClient(ip, port);
				mc.setWriteConcern(WriteConcern.NORMAL);
				mcset.put(k, mc);
			} catch (Exception e) {
				throw e;
			}
		}
		return mc;
	}

	public static void closeAll() {
		for (MongoClient client : mcset.values()) {
			if (client != null) {
				client.close();
			}
		}
		mcset.clear();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
