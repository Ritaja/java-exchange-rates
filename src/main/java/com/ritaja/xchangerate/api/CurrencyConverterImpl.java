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
 * Created by rsengupta on 04/09/15.
 */
public class CurrencyConverterImpl implements Converter {
	private DiskStore diskStore = new FileStore("random");
	private ServiceFactory serviceFactory = new YahooEndpoint(diskStore);
	private JSONObject exchangeRates;

	public BigDecimal convertCurrency(BigDecimal moneyAmount, Currency fromCurrency, Currency toCurrency) throws XchangeRateException, CurrencyNotSupportedException, JSONException, StorageException, EndpointException, ServiceException {
		BigDecimal amount;
		if (!serviceFactory.checkRatesUsable(fromCurrency) || !serviceFactory.checkRatesUsable(toCurrency)) {
			JSONObject response = serviceFactory.sendLiveRequest();
			diskStore.saveRates(response);
		}
		exchangeRates = serviceFactory.getExchangeRates();
		if (fromCurrency.equals(serviceFactory.baseCurrency)) {
			amount = serviceFactory.convertFromBaseCurrency(moneyAmount, toCurrency);
		} else if (toCurrency.equals(serviceFactory.baseCurrency)) {
			amount = serviceFactory.convertToBaseCurrency(moneyAmount, fromCurrency);
		} else {
			BigDecimal intermediateAmount = serviceFactory.convertToBaseCurrency(moneyAmount, fromCurrency);
			amount = serviceFactory.convertFromBaseCurrency(intermediateAmount, toCurrency);
		}
		return amount;
	}

	public static void main(String args[]) throws EndpointException, XchangeRateException, JSONException, StorageException, CurrencyNotSupportedException, ServiceException {
		CurrencyConverterImpl c = new CurrencyConverterImpl();
		System.out.println(c.convertCurrency(new BigDecimal("100"), Currency.USD, Currency.EUR));
	}
}
