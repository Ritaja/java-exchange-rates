package com.ritaja.xchangerate.storage;

import org.json.JSONObject;

/**
 * Created by rsengupta on 03/09/15.
 */
public class MongoStore extends DiskStore {
	@Override
	public void saveRates(JSONObject exchangeRates) throws StorageException {

	}

	@Override
	public JSONObject loadRates() throws StorageException {
		return null;
	}

	@Override
	public void setRefreshrateSeconds(int refreshrateSeconds) {

	}

	@Override
	public boolean resourceExists() {
		return false;
	}
}
