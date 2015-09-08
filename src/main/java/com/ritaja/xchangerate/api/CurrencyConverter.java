package com.ritaja.xchangerate.api;

import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;

import com.ritaja.xchangerate.endpoint.EndpointException;
import com.ritaja.xchangerate.endpoint.EndpointFactory;
import com.ritaja.xchangerate.service.ServiceException;
import com.ritaja.xchangerate.storage.DiskStore;
import com.ritaja.xchangerate.storage.StorageException;
import com.ritaja.xchangerate.util.Currency;
import com.ritaja.xchangerate.util.Strategy;

/**
 * Created by rsengupta on 04/09/15.x
 */
public class CurrencyConverter implements Converter {
	public DiskStore diskStore;
	public EndpointFactory endpointFactory;

	public CurrencyConverter(DiskStore diskStore, EndpointFactory endpointFactory) {
		this.diskStore = diskStore;
		this.endpointFactory = endpointFactory;
	}

	/**
	 * set the refresh rate of the cached resource.
	 * This rate decides the time for which the conversion
	 * rate is considered to be usable for the currency.
	 * When this time in seconds exceeds for the currency a
	 * fresh request is sent and exchange rates are cached.
	 *
	 * @param seconds int
	 */
	public void setRefreshRateSeconds(int seconds) {
		endpointFactory.refreshRateSeconds = seconds;
	}

	/**
	 * sets the filepath where resource should be cached/stored
	 *
	 * @param resourceFilepath String
	 */
	public void setResourceFilepath(String resourceFilepath) {
		diskStore.resourceFilepath = resourceFilepath;
	}

	/**
	 * converts the money amount from one currency type to the other
	 *
	 * @param moneyAmount the money amount to convert
	 * @param fromCurrency conversion from currency
	 * @param toCurrency conversion to currency
	 * @return BigDecimal converted amount
	 * @throws CurrencyNotSupportedException
	 * @throws JSONException
	 * @throws StorageException
	 * @throws EndpointException
	 * @throws ServiceException
	 */
	public BigDecimal convertCurrency(BigDecimal moneyAmount, Currency fromCurrency, Currency toCurrency) throws CurrencyNotSupportedException, JSONException, StorageException, EndpointException, ServiceException {
		BigDecimal amount;
		updateResource(fromCurrency, toCurrency);
		if (fromCurrency == null || toCurrency == null) {
			throw new IllegalArgumentException("Convert currency takes 2 arguments!");
		} else if (fromCurrency.equals(toCurrency)) {
			amount = moneyAmount;
		} else if (fromCurrency.equals(endpointFactory.baseCurrency)) {
			amount = endpointFactory.convertFromBaseCurrency(moneyAmount, toCurrency);
		} else if (toCurrency.equals(endpointFactory.baseCurrency)) {
			amount = endpointFactory.convertToBaseCurrency(moneyAmount, fromCurrency);
		} else {
			BigDecimal intermediateAmount = endpointFactory.convertToBaseCurrency(moneyAmount, fromCurrency);
			amount = endpointFactory.convertFromBaseCurrency(intermediateAmount, toCurrency);
		}
		return amount;
	}

	/**
	 * Helper method to check and update the cache for exchange rates.
	 *
	 * @param fromCurrency conversion from currency
	 * @param toCurrency conversion to currency
	 * @throws CurrencyNotSupportedException
	 * @throws StorageException
	 * @throws JSONException
	 * @throws EndpointException
	 * @throws ServiceException
	 */
	public void updateResource(Currency fromCurrency, Currency toCurrency) throws CurrencyNotSupportedException, StorageException, JSONException, EndpointException, ServiceException {
		if (!endpointFactory.checkRatesUsable(fromCurrency) || !endpointFactory.checkRatesUsable(toCurrency)) {
			JSONObject response = endpointFactory.sendLiveRequest();
			diskStore.saveRates(response);
			endpointFactory.setExchangeRates(response);
		} else {
			endpointFactory.setExchangeRates(diskStore.loadRates());
		}
	}
}
