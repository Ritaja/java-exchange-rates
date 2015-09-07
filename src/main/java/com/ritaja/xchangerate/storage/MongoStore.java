package com.ritaja.xchangerate.storage;

import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

/**
 * Created by rsengupta on 03/09/15.
 */
public class MongoStore extends DiskStore {
	MongoClient mongoClient = new MongoClient("localhost", 27017);
	DB database = mongoClient.getDB("currencyConverter");
	DBCollection collection = database.getCollection("xchangeRate");

	@Override
	public void saveRates(JSONObject exchangeRates) throws StorageException {
		collection.insert((DBObject) JSON.parse(exchangeRates.toString()));
	}

	@Override
	public JSONObject loadRates() throws StorageException {
		return null;
	}

	@Override
	public boolean resourceExists() {
		return true;
	}
}
