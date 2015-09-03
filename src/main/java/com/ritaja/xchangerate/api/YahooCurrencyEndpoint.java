package com.ritaja.xchangerate.api;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ritaja.xchangerate.util.Currency;

/**
 * Created by rsengupta on 30/08/15.
 */
public class YahooCurrencyEndpoint extends AbstractXchangeRate {
	public String BASE_URL = "http://finance.yahoo.com/webservice/v1/symbols/allcurrencies/";
	public String ENDPOINT = "quote";
	private Map rate = new HashMap();
	private Currency fromCurrency;
	private Currency toCurrency;
	private final Logger LOGGER = LoggerFactory.getLogger(YahooCurrencyEndpoint.class);
	// used for executing requests to the (REST) API
	private CloseableHttpClient httpClient;

	public YahooCurrencyEndpoint() throws XchangeRateException {
		super(Currency.USD, "yahoo");
	}

	/**
	 * checks if the data is Stale/Usable and refreshes it if required
	 *
	 * @throws XchangeRateException
	 */
	private void checkStaleData() throws XchangeRateException {
		if (!checkRatesUsable()) {
			LOGGER.info("Stored data expired/not existing, sending live request");
			sendLiveRequest();
		} else {
			LOGGER.info("Stored data has not expired, skipping request ");
		}
	}

	/**
	 * Notes:<br><br>
	 *
	 * A JSON response of the form {"key":"value"} is considered a simple Java JSONObject.<br>
	 * To get a simple value from the JSONObject, use: <JSONObject identifier>.get<Type>("key");<br>
	 *
	 * A JSON response of the form {"key":{"key":"value"}} is considered a complex Java JSONObject.<br>
	 * To get a complex value like another JSONObject, use: <JSONObject identifier>.getJSONObject("key")
	 *
	 * Values can also be JSONArray Objects. JSONArray objects are simple, consisting of multiple JSONObject Objects.
	 */

	/**
	 * sends the live request to currency layer API and saves the exchange rates from the response
	 *
	 * @throws XchangeRateException
	 */
	private void sendLiveRequest() throws XchangeRateException {
		HttpGet get = new HttpGet(BASE_URL + ENDPOINT + "?format=json");

		try {
			httpClient = HttpClients.createDefault();
			CloseableHttpResponse response = httpClient.execute(get);
			HttpEntity entity = response.getEntity();
			exchangeRates = new JSONObject(EntityUtils.toString(entity));
			saveRates();
			httpClient.close();
			// LOGGER.debug("Response from  yahoo endpoint: " + exchangeRates.toString());
		} catch (Exception e) {
			throw new XchangeRateException(e);
		}
	}

	/**
	 * sets the filepath for the exchange rates containing file
	 *
	 * @param filePath a valid file path to hold the cached exchange rates
	 */
	public void setRatesFilePath(String filePath) {
		ratesFilepath = filePath;
	}

	/**
	 * returns the date when the exchange rate date was last cached/stored in a nice formated string
	 *
	 * @return String date
	 * @throws XchangeRateException
	 */
	public String getExchangeRateTimestamp() throws XchangeRateException {
		Date timeStampDate = new Date(getTimestamp());
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
		return dateFormat.format(timeStampDate);
	}

	/**
	 * converts the moey amount from provided currency to desired currency
	 * using stored exchange rates
	 *
	 * @param moneyAmount the money amount to be converted
	 * @param fromCurrency the currency to convert from (provided currency)
	 * @param toCurrency the currency to convert into (required currency)
	 * @throws XchangeRateException
	 * @returnouble converted amount
	 */
	public BigDecimal convertCurrency(BigDecimal moneyAmount, Currency fromCurrency, Currency toCurrency) throws CurrencyNotSupportedException, XchangeRateException {
		if (fromCurrency.equals(toCurrency)) {
			return moneyAmount;
		}
		this.fromCurrency = fromCurrency;
		this.toCurrency = toCurrency;
		checkStaleData();
		try {
			// Scenario 0: {convert USD --> other currency} (lame scenario taken care before)
			// Scenario 1: {convert USD --> other currency}
			// Scenario 2: {other currency --> covert USD}
			// Scenario 3: {covert between different currencies}
			if (fromCurrency.equals(Currency.USD)) {
				return convertFromUSD(moneyAmount, toCurrency);
			} else if (toCurrency.equals(Currency.USD)) {
				return convertToUSD(moneyAmount, fromCurrency);
			} else {
				BigDecimal intermediateAmount = convertToUSD(moneyAmount, fromCurrency);
				return convertFromUSD(intermediateAmount, toCurrency);
			}
		} catch (JSONException e) {
			throw new XchangeRateException(e);
		}
	}

	/**
	 * helper method to convert to USD
	 * Scenario : {other currency --> covert USD}
	 *
	 * @param moneyAmount money amount to convert
	 * @param fromCurrency currency to convert from
	 * @return double converted amount
	 * @throws JSONException
	 */
	private BigDecimal convertToUSD(BigDecimal moneyAmount, Currency fromCurrency) throws JSONException, XchangeRateException, CurrencyNotSupportedException {
		if (rate.isEmpty()) {
			return (moneyAmount.divide(new BigDecimal(getPrice(fromCurrency)), 2, RoundingMode.HALF_UP));
		}
		return (moneyAmount.divide(new BigDecimal(rate.get(fromCurrency).toString()), 2, RoundingMode.HALF_UP));
	}

	/**
	 * helper method to convert to USD
	 * Scenario : {covert USD --> other currency}
	 *
	 * @param moneyAmount money amount to convert
	 * @param toCurrency currency to USD into
	 * @return double converted amount
	 * @throws JSONException
	 */
	private BigDecimal convertFromUSD(BigDecimal moneyAmount, Currency toCurrency) throws JSONException, XchangeRateException, CurrencyNotSupportedException {
		int digitsBeforeDecimal = moneyAmount.toPlainString().split("\\.")[0].length();
		if (rate.isEmpty()) {
			return (moneyAmount.divide(new BigDecimal(getPrice(toCurrency)), 2, RoundingMode.HALF_UP));
		}
		return (new BigDecimal(rate.get(toCurrency).toString()).multiply(moneyAmount, new MathContext(digitsBeforeDecimal + 2, RoundingMode.HALF_UP)));
	}

	/**
	 * Helper to retrieves the timestamp from the stored exchange rate
	 *
	 * @return long timestamp in linux format
	 * @throws XchangeRateException
	 */
	public long getTimestamp() throws XchangeRateException {
		try {
			if (fromCurrency.toString().equalsIgnoreCase(Currency.USD.toString())) {
				return retrieveTimeForCurrency(toCurrency);
			} else if (toCurrency.toString().equalsIgnoreCase(Currency.USD.toString())) {
				return retrieveTimeForCurrency(fromCurrency);
			}
			// return the lowest timestamp
			long fromCurrencyTimestamp = retrieveTimeForCurrency(fromCurrency);
			long toCurrencyTimestamp = retrieveTimeForCurrency(toCurrency);
			return fromCurrencyTimestamp < toCurrencyTimestamp ? fromCurrencyTimestamp : toCurrencyTimestamp;
			// Java 7 does not allow combined catch statements
		} catch (JSONException e) {
			throw new XchangeRateException(e);
		} catch (CurrencyNotSupportedException e) {
			throw new XchangeRateException(e);
		}
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
			if (field.getString("name").equalsIgnoreCase("USD/" + currency.toString())) {
				rate.put(currency, field.getString("price"));
				return Long.parseLong(field.getString("ts"), 10) * 1000;
			}
		}
		throw new CurrencyNotSupportedException("currency: " + currency + " is not supported by Yahoo endpoint");
	}

	/**
	 * retrieves the "price" value from the JSON data for given currency
	 * this helper is run only when we are creating the xchangeRates file for the first time.
	 * once created we use the java map rate for efficiency
	 *
	 * @param currency
	 * @return String "price" value
	 * @throws JSONException
	 * @throws CurrencyNotSupportedException
	 */
	private String getPrice(Currency currency) throws JSONException, CurrencyNotSupportedException {
		JSONArray resources = exchangeRates.getJSONObject("list").getJSONArray("resources");
		// JSONArray is not iterable, hence the code
		for (int i = 0; i < resources.length(); ++i) {
			JSONObject field = resources.getJSONObject(i).getJSONObject("resource").getJSONObject("fields");
			if (field.getString("name").equalsIgnoreCase("USD/" + currency.toString())) {
				return field.getString("price");
			}
		}
		throw new CurrencyNotSupportedException("currency: " + currency + " is not supported by Yahoo endpoint");
	}
}
