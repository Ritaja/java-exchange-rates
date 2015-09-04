package com.ritaja.xchangerate.endpoint;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.json.JSONException;
import org.json.JSONObject;

import com.ritaja.xchangerate.api.CurrencyNotSupportedException;
import com.ritaja.xchangerate.api.XchangeRateException;
import com.ritaja.xchangerate.caching.CachingXchangeRate;
import com.ritaja.xchangerate.service.HttpMethods;
import com.ritaja.xchangerate.service.HttpserviceImpl;
import com.ritaja.xchangerate.service.ServiceException;
import com.ritaja.xchangerate.storage.DiskStore;
import com.ritaja.xchangerate.util.Currency;

/**
 * Created by rsengupta on 04/09/15.
 */
public abstract class ServiceFactory extends CachingXchangeRate {
	// the intermediate currency
	public Currency baseCurrency;
	// used for executing requests to the (REST) API
	private HttpserviceImpl httpservice;

	public ServiceFactory(DiskStore diskStore, Currency baseCurrency, String uri) {
		super(diskStore);
		this.baseCurrency = baseCurrency;
		httpservice = new HttpserviceImpl(uri);
	}

	public Currency getBaseCurrency() {
		return this.baseCurrency;
	}

	/**
	 * sends the live request to currency layer API and saves the exchange rates from the response
	 *
	 * @throws XchangeRateException
	 */
	public JSONObject sendLiveRequest() throws JSONException, ServiceException, EndpointException {
		JSONObject response=httpservice.getResponse(HttpMethods.GET);
		if(checkResponse()) {
			return response;
		}
		return null;
	}

	/**
	 * helper method to convert to base currency e.g:USD
	 * Scenario : {other currency --> covert USD}
	 *
	 * @param moneyAmount money amount to convert
	 * @param fromCurrency currency to convert from
	 * @return double converted amount
	 * @throws JSONException
	 */
	public BigDecimal convertToBaseCurrency(BigDecimal moneyAmount, Currency fromCurrency) throws JSONException, CurrencyNotSupportedException {
		return (moneyAmount.divide(getRate(fromCurrency), 2, RoundingMode.HALF_UP));
	}

	/**
	 * helper method to convert to base currency e.g:USD
	 * Scenario : {covert USD --> other currency}
	 *
	 * @param moneyAmount money amount to convert
	 * @param toCurrency currency to USD into
	 * @return double converted amount
	 * @throws JSONException
	 */
	public BigDecimal convertFromBaseCurrency(BigDecimal moneyAmount, Currency toCurrency) throws JSONException, CurrencyNotSupportedException {
		int digitsBeforeDecimal = moneyAmount.toPlainString().split("\\.")[0].length();
		return getRate(toCurrency).multiply(moneyAmount, new MathContext(digitsBeforeDecimal + 2, RoundingMode.HALF_UP));
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
	public abstract boolean checkResponse() throws EndpointException, JSONException;

	public abstract BigDecimal getRate(Currency currency) throws JSONException, CurrencyNotSupportedException;
}
