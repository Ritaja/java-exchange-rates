package com.ritaja.xchangerate.caching;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.ritaja.xchangerate.api.CurrencyNotSupportedException;
import com.ritaja.xchangerate.endpoint.EndpointException;
import com.ritaja.xchangerate.storage.DiskStore;
import com.ritaja.xchangerate.storage.StorageException;
import com.ritaja.xchangerate.util.Currency;

/**
 * Created by rsengupta on 07/09/15.
 */
public abstract class CachingXchangeRate {

	public int refreshRateSeconds = 86400;
	public JSONObject exchangeRates = null;
	private DiskStore diskStore;

	public CachingXchangeRate(DiskStore diskStore) {
		this.diskStore = diskStore;
	}

	public void setExchangeRates(JSONObject exchangeRates) {
		this.exchangeRates=exchangeRates;
	}

	/**
	 * checks if the rates have expired judging from the timestamp of
	 * the stored exchange rate resource file
	 *
	 * @return boolean truth value
	 * @throws EndpointException
	 * @throws CurrencyNotSupportedException
	 * @throws StorageException
	 */
	public boolean checkRatesUsable(Currency currency) throws JSONException, CurrencyNotSupportedException, StorageException {
		if (!diskStore.resourceExists()) {
			return false;
		} else if (exchangeRates == null) {
			setExchangeRates(diskStore.loadRates());
		}
		// calculate the difference in timestamp and return false if not expired
		long old = getTimestamp(currency);
		long now = new DateTime().getMillis();
		if (Math.abs((old - now) / 1000) < (refreshRateSeconds)) {
			return true;
		}
		// return true if the timestamp has expired
		return false;
	}

	/**
	 * get the timestamp of associated exchange rate
	 *
	 * @param currency
	 * @return timestamp
	 * @throws EndpointException
	 * @throws CurrencyNotSupportedException
	 */
	public abstract long getTimestamp(Currency currency) throws JSONException, CurrencyNotSupportedException;
}
