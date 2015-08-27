package com.ritaja.xchangerate.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Seconds;
import org.json.JSONException;
import org.json.JSONObject;

import com.ritaja.xchangerate.util.Currency;

/**
 * Created by rsengupta on 22/08/15.
 */
public abstract class AbstractXchangeRate implements CurrencyConverter {
	private static final String RATES_FILEPATH = "./src/main/resources/xchangeRates.json";

	// default refresh rate of 1 day
	protected static int REFRESHRATE_SECONDS = 86400;

	private Currency baseCurr;
	private JSONObject ratesDocument = null;

	public AbstractXchangeRate(Currency baseCurrency) {
		this.baseCurr = baseCurrency;
	}

	/**
	 * saves the exchange rates in a stored resource file
	 *
	 * @param exchangeRates JSONObject of exchange rates from the reply
	 * @throws XchangeRateException
	 */
	protected void saveRates(JSONObject exchangeRates) throws XchangeRateException {
		try {
			FileWriter file = new FileWriter(RATES_FILEPATH);
			file.write(exchangeRates.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			throw new XchangeRateException(e);
		}
	}

	/**
	 * Parses the exchange rates from the stored resource file
	 * and stores them as a JSONObject
	 *
	 * @throws XchangeRateException
	 */
	private void retrieveRates() throws XchangeRateException {
		// parse the JSON string from the resource file
		String jsonData = "";
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader(RATES_FILEPATH));
			while ((line = br.readLine()) != null) {
				jsonData += line + "\n";
			}
		} catch (IOException e) {
			throw new XchangeRateException(e);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		// covert the parsed string to a JSON object
		try {
			ratesDocument = new JSONObject(jsonData);
		} catch (JSONException e) {
			throw new XchangeRateException(e);
		}
	}

	protected JSONObject getRates() {
		return this.ratesDocument;
	}

	/**
	 * check if this resource file exists
	 *
	 * @return boolean truth value
	 */
	private boolean resourceExists() {
		File f = new File(RATES_FILEPATH);
		if (f.exists() && !f.isDirectory()) {
			return true;
		}
		return false;
	}

	/**
	 * checks if the rates have expired judging from the timestamp of
	 * the stored exchange rate resource file
	 *
	 * @return boolean truth value
	 * @throws XchangeRateException
	 */
	protected boolean checkRatesUsable() throws XchangeRateException {
		if (resourceExists() && ratesDocument == null) {
			retrieveRates();
		} else if (!resourceExists()) {
			return false;
		}
		try {
			DateTime timestamp = new DateTime(Long.parseLong(ratesDocument.get("timestamp").toString(), 10) * 1000);
			DateTime now = new DateTime();
			Days days = Days.daysBetween(timestamp, now);
			if (days.toStandardSeconds().isLessThan(Seconds.seconds(REFRESHRATE_SECONDS))) {
				return false;
			}
		} catch (JSONException e) {
			throw new XchangeRateException(e);
		}

		return true;
	}

	protected Currency getBaseCurrency() {
		return this.baseCurr;
	}
}
