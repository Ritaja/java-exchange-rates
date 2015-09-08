package com.ritaja.xchangerate.endpoint;

import java.math.BigDecimal;

import org.json.JSONException;

import com.ritaja.xchangerate.storage.DiskStore;
import com.ritaja.xchangerate.util.Currency;

/**
 * Created by rsengupta on 03/09/15.
 */
public class CurrencyLayerEndpoint extends EndpointFactory {
	public static final String BASE_URL = "http://apilayer.net/api/";
	public static final String ENDPOINT = "live";
	private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(CurrencyLayerEndpoint.class.getName());

	public CurrencyLayerEndpoint(DiskStore diskStore, String accessKey) {
		super(diskStore, Currency.USD, BASE_URL + ENDPOINT + "?access_key=" + accessKey);
	}

	public BigDecimal getRate(Currency currency) throws JSONException {
		return new BigDecimal(exchangeRates.getJSONObject("quotes").getDouble("USD" + currency));
	}

	/**
	 * Checks if the response from the web service
	 * is proper and can be cached/stored for offline
	 * use of currency conversion
	 *
	 * @return boolean truth value
	 * @throws EndpointException
	 * @throws JSONException
	 */
	public boolean checkResponse() throws EndpointException, JSONException {
		if (response.get("success").toString().equalsIgnoreCase("false")) {
			throw new EndpointException("Currency Layer request did not succeed, info: " + response.getJSONObject("error").get("info"));
		}
		return true;
	}

	/**
	 * Helper to retrieves the timestamp from the stored exchange rate
	 *
	 * @param currency (not used in this case)
	 * @return long timestamp in linux format
	 * @throws EndpointException
	 */
	public long getTimestamp(Currency currency) throws JSONException {
		return Long.parseLong(exchangeRates.get("timestamp").toString(), 10) * 1000;
	}
}
