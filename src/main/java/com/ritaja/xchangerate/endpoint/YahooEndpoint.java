package com.ritaja.xchangerate.endpoint;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ritaja.xchangerate.api.CurrencyNotSupportedException;
import com.ritaja.xchangerate.storage.DiskStore;
import com.ritaja.xchangerate.util.Currency;

/**
 * Created by rsengupta on 03/09/15.
 */
public class YahooEndpoint extends EndpointFactory {
	public static final String BASE_URL = "http://finance.yahoo.com/webservice/v1/symbols/allcurrencies/";
	public static final String ENDPOINT = "quote";
	private Map rate = new HashMap();

	public YahooEndpoint(DiskStore diskStore) {
		super(diskStore, Currency.USD, BASE_URL + ENDPOINT + "?format=json");
	}

	/**
	 * checks if the response from the web service API was a success
	 *
	 * @throws EndpointException
	 * @throws JSONException
	 */
	public boolean checkResponse() throws EndpointException, JSONException {
		response.getJSONObject("list").getJSONArray("resources").getJSONObject(1).getJSONObject("resource").getJSONObject("fields").getString("name");

		return true;
	}

	public BigDecimal getRate(Currency currency) throws JSONException, CurrencyNotSupportedException {
		if (!rate.isEmpty() && rate.containsKey(currency)) {
			return (BigDecimal) rate.get(currency);
		}
		JSONArray resources = exchangeRates.getJSONObject("list").getJSONArray("resources");
		// JSONArray is not iterable, hence the code
		for (int i = 0; i < resources.length(); ++i) {
			JSONObject field = resources.getJSONObject(i).getJSONObject("resource").getJSONObject("fields");
			if (field.getString("name").equalsIgnoreCase("USD/" + currency.toString())) {
				rate.put(currency, new BigDecimal(field.getDouble("price")));
				return new BigDecimal(field.getDouble("price"));
			}
		}
		throw new CurrencyNotSupportedException("currency: " + currency + " is not supported by Yahoo endpoint");
	}

	/**
	 * Helper to retrieves the timestamp from the stored exchange rate
	 *
	 * @param currency
	 * @return long timestamp in linux format
	 * @throws JSONException
	 * @throws CurrencyNotSupportedException
	 */
	public long getTimestamp(Currency currency) throws JSONException, CurrencyNotSupportedException {
		if (currency.toString().equalsIgnoreCase(Currency.USD.toString())) {
			return new DateTime().getMillis();
		}
		return retrieveTimeForCurrency(currency);
	}

	/**
	 * retrieves the timestamp associated with a currency.
	 * This method also stores the rates for the currencies
	 * it retrieves timestamp for. This makes it fast for
	 * currency lookup during conversion
	 *
	 * @param currency
	 * @return long timestamp in milliseconds
	 * @throws JSONException
	 * @throws CurrencyNotSupportedException
	 */
	private long retrieveTimeForCurrency(Currency currency) throws JSONException, CurrencyNotSupportedException {
		JSONArray resources = exchangeRates.getJSONObject("list").getJSONArray("resources");
		// JSONArray is not iterable, hence the code
		for (int i = 0; i < resources.length(); ++i) {
			JSONObject field = resources.getJSONObject(i).getJSONObject("resource").getJSONObject("fields");
			if(field.length() > 0){
			if (field.getString("name").equalsIgnoreCase("USD/" + currency.toString())) {
				rate.put(currency, new BigDecimal(field.getString("price")));
				return Long.parseLong(field.getString("ts"), 10) * 1000;
			}
			}
		}
		throw new CurrencyNotSupportedException("currency: " + currency + " is not supported by Yahoo endpoint");
	}
}
