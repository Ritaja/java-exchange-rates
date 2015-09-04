package com.ritaja.xchangerate.storage;

import org.json.JSONObject;

/**
 * Created by rsengupta on 03/09/15.
 */
public abstract class DiskStore {
	/**
	 * saves the exchange rates in a stored resource file
	 *
	 * @throws StorageException
	 */
	public abstract void saveRates(JSONObject exchangeRates) throws StorageException;

	/**
	 * Parses the exchange rates from the stored resource file
	 * and stores them as a JSONObject
	 *
	 * @throws StorageException
	 */
	public abstract JSONObject loadRates() throws StorageException;

	/**
	 * set the refresh rate for checking when stored exchange rate is outdated
	 *
	 * @param refreshrateSeconds
	 */
	public abstract void setRefreshrateSeconds(int refreshrateSeconds);

	/**
	 * check if this resource resource exists
	 *
	 * @return boolean truth value
	 */
	public abstract boolean resourceExists();
}
