package com.ritaja.xchangerate.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ritaja.xchangerate.util.Currency;

/**
 * Created by rsengupta on 22/08/15.
 */
public abstract class AbstractXchangeRate implements CurrencyConverter {
	protected String RATES_FILEPATH = System.getProperty("java.io.tmpdir");
	protected String RATES_FILENAME;
	// default refresh rate of 1 day
	protected int REFRESHRATE_SECONDS = 86400;
	private Currency baseCurr;
	protected JSONObject exchangeRates = null;

	private final Logger LOGGER = LoggerFactory.getLogger(AbstractXchangeRate.class);

	public AbstractXchangeRate(Currency baseCurrency, String filenameAppender) {
		this.baseCurr = baseCurrency;
		this.RATES_FILENAME = "/" + filenameAppender + "XchangeRates.json";
	}

	/**
	 * saves the exchange rates in a stored resource file
	 *
	 * @throws XchangeRateException
	 */
	protected void saveRates() throws XchangeRateException {
		if (exchangeRates == null) {
			throw new XchangeRateException("Cannot save null exchangeRates!");
		}
		try {
			FileWriter file = new FileWriter(RATES_FILEPATH + RATES_FILENAME);
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
			br = new BufferedReader(new FileReader(RATES_FILEPATH + RATES_FILENAME));
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
			exchangeRates = new JSONObject(jsonData);
		} catch (JSONException e) {
			throw new XchangeRateException(e);
		}
	}

	protected JSONObject getRates() {
		return this.exchangeRates;
	}

	/**
	 * check if this resource file exists
	 *
	 * @return boolean truth value
	 */
	private boolean resourceExists() {
		File f = new File(RATES_FILEPATH + RATES_FILENAME);
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
		if (resourceExists() && exchangeRates == null) {
			retrieveRates();
		} else if (!resourceExists()) {
			return false;
		}
		// calculate the difference in timestamp and return false if not expired
		long old = getTimestamp();
		long now = new DateTime().getMillis();
		if ((old - now) / 1000 < (REFRESHRATE_SECONDS)) {
			LOGGER.debug("difference from days: " + (old - now) / 1000);
			return false;
		}
		// return true if the timestamp has expired
		return true;
	}

	protected Currency getBaseCurrency() {
		return this.baseCurr;
	}

	public abstract long getTimestamp() throws XchangeRateException;
}
