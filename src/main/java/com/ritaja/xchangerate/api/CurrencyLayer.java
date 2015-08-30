package com.ritaja.xchangerate.api;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ritaja.xchangerate.util.Currency;

public class CurrencyLayer extends AbstractXchangeRate {

	public static String BASE_URL = "http://apilayer.net/api/";
	public static String ENDPOINT = "live";
	public static String accessKey;
	public static JSONObject exchangeRates;
	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyLayer.class);
	// used for executing requests to the (REST) API
	static CloseableHttpClient httpClient;

	public CurrencyLayer(String accessKey) throws XchangeRateException {
		super(Currency.USD);
		this.accessKey = accessKey;
		checkStaleData();
		exchangeRates = getRates();
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
		HttpGet get = new HttpGet(BASE_URL + ENDPOINT + "?access_key=" + accessKey);

		try {
			httpClient = HttpClients.createDefault();
			CloseableHttpResponse response = httpClient.execute(get);
			HttpEntity entity = response.getEntity();
			exchangeRates = new JSONObject(EntityUtils.toString(entity));
			checkResponse();
			saveRates(exchangeRates);
			httpClient.close();
			LOGGER.debug("Response from currency layer: " + exchangeRates.toString());
		} catch (Exception e) {
			throw new XchangeRateException(e);
		}
	}

	/**
	 * checks if the response from the web service API was a success
	 *
	 * @throws XchangeRateException
	 * @throws JSONException
	 */
	private void checkResponse() throws XchangeRateException, JSONException {
		if (exchangeRates.get("success").toString().equalsIgnoreCase("false")) {
			throw new XchangeRateException("Currency Layer request did not succeed, info: " + exchangeRates.getJSONObject("error").get("info"));
		}
	}

	/**
	 * set the refresh rate for checking when stored exchange rate is outdated
	 *
	 * @param refreshrateSeconds
	 */
	public static void setRefreshrateSeconds(int refreshrateSeconds) {
		REFRESHRATE_SECONDS = refreshrateSeconds;
	}

	/**
	 * sets the filepath for the exchange rates containing file
	 *
	 * @param filePath a valid file path to hold the cached exchange rates
	 */
	public static void setRatesFilePath(String filePath) {
		RATES_FILEPATH = filePath;
	}

	/**
	 * returns the date when the exchange rate date was last cached/stored
	 *
	 * @return String date
	 * @throws XchangeRateException
	 */
	public String getExchangeRateTimestamp() throws XchangeRateException {
		try {
			Date timeStampDate = new Date(Long.parseLong(exchangeRates.get("timestamp").toString(), 10) * 1000);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
			return dateFormat.format(timeStampDate);
		} catch (JSONException e) {
			throw new XchangeRateException(e);
		}
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
		checkStaleData();
		try {
			// Scenario 1: {covert USD --> other currency}
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
			if (e.getMessage().contains("\"USD")) {
				throw new CurrencyNotSupportedException("currency: " + e.getMessage().substring(15, 18) + " not supported");
			} else {
				throw new XchangeRateException(e);
			}
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
	private BigDecimal convertToUSD(BigDecimal moneyAmount, Currency fromCurrency) throws JSONException, XchangeRateException {
		return (moneyAmount.divide(new BigDecimal(exchangeRates.getJSONObject("quotes").getDouble("USD" + fromCurrency)), 2, RoundingMode.HALF_UP));
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
	private BigDecimal convertFromUSD(BigDecimal moneyAmount, Currency toCurrency) throws JSONException, XchangeRateException {
		int digitsBeforeDecimal = moneyAmount.toPlainString().split("\\.")[0].length();
		return (new BigDecimal(exchangeRates.getJSONObject("quotes").getDouble("USD" + toCurrency)).multiply(moneyAmount, new MathContext(digitsBeforeDecimal + 2, RoundingMode.HALF_UP)));
	}
}