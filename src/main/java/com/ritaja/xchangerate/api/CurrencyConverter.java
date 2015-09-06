package com.ritaja.xchangerate.api;

import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;

import com.ritaja.xchangerate.endpoint.EndpointException;
import com.ritaja.xchangerate.endpoint.ServiceFactory;
import com.ritaja.xchangerate.endpoint.YahooEndpoint;
import com.ritaja.xchangerate.service.ServiceException;
import com.ritaja.xchangerate.storage.DiskStore;
import com.ritaja.xchangerate.storage.FileStore;
import com.ritaja.xchangerate.storage.StorageException;
import com.ritaja.xchangerate.util.Currency;

/**
 * Created by rsengupta on 04/09/15.x
 */
public class CurrencyConverter implements Converter {
	public DiskStore diskStore = new FileStore("random");
	public ServiceFactory serviceFactory = new YahooEndpoint(diskStore);

	public BigDecimal convertCurrency(BigDecimal moneyAmount, Currency fromCurrency, Currency toCurrency) throws CurrencyNotSupportedException, JSONException, StorageException, EndpointException, ServiceException {
		BigDecimal amount;
		updateResource(fromCurrency, toCurrency);
		if (fromCurrency == null || toCurrency == null) {
			throw new IllegalArgumentException("Convert currency takes 2 arguments!");
		} else if (fromCurrency.equals(toCurrency)) {
			amount = moneyAmount;
		} else if (fromCurrency.equals(serviceFactory.baseCurrency)) {
			amount = serviceFactory.convertFromBaseCurrency(moneyAmount, toCurrency);
		} else if (toCurrency.equals(serviceFactory.baseCurrency)) {
			amount = serviceFactory.convertToBaseCurrency(moneyAmount, fromCurrency);
		} else {
			BigDecimal intermediateAmount = serviceFactory.convertToBaseCurrency(moneyAmount, fromCurrency);
			amount = serviceFactory.convertFromBaseCurrency(intermediateAmount, toCurrency);
		}
		return amount;
	}

	public void updateResource(Currency fromCurrency, Currency toCurrency) throws CurrencyNotSupportedException, StorageException, JSONException, EndpointException, ServiceException {
		if (!serviceFactory.checkRatesUsable(fromCurrency) || !serviceFactory.checkRatesUsable(toCurrency)) {
			JSONObject response = serviceFactory.sendLiveRequest();
			diskStore.saveRates(response);
			serviceFactory.setExchangeRates(response);
		} else {
			serviceFactory.setExchangeRates(diskStore.loadRates());
		}
	}

	public static void main(String args[]) throws EndpointException, JSONException, StorageException, CurrencyNotSupportedException, ServiceException {
		CurrencyConverter c = new CurrencyConverter();
		System.out.println(c.convertCurrency(new BigDecimal("100"), Currency.USD, Currency.EUR));
	}
}
