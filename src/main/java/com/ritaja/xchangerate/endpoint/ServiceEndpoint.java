package com.ritaja.xchangerate.endpoint;

import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;

import com.ritaja.xchangerate.api.CurrencyNotSupportedException;
import com.ritaja.xchangerate.service.ServiceException;
import com.ritaja.xchangerate.util.Currency;

/**
 * Created by rsengupta on 06/09/15.
 */
public interface ServiceEndpoint {

	/**
	 * sends the live request to currency layer API and saves the exchange rates from the response
	 *
	 * @throws JSONException
	 * @throws ServiceException
	 * @throws EndpointException
	 */
	public JSONObject sendLiveRequest() throws JSONException, ServiceException, EndpointException;

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

	/**
	 * helper method to convert to base currency e.g:USD
	 * Scenario : {other currency --> covert USD}
	 *
	 * @param moneyAmount money amount to convert
	 * @param fromCurrency currency to convert from
	 * @return double converted amount
	 * @throws JSONException
	 * @throws CurrencyNotSupportedException
	 */
	public BigDecimal convertToBaseCurrency(BigDecimal moneyAmount, Currency fromCurrency) throws JSONException, CurrencyNotSupportedException;

	/**
	 * helper method to convert to base currency e.g:USD
	 * Scenario : {covert USD --> other currency}
	 *
	 * @param moneyAmount money amount to convert
	 * @param toCurrency currency to USD into
	 * @return double converted amount
	 * @throws JSONException
	 * @throws CurrencyNotSupportedException
	 */
	public BigDecimal convertFromBaseCurrency(BigDecimal moneyAmount, Currency toCurrency) throws JSONException, CurrencyNotSupportedException;

	/**
	 * retrieves rate of exchange price for the desiered currency
	 *
	 * @param currency
	 * @return BigDecimal exchange rate
	 * @throws JSONException
	 * @throws CurrencyNotSupportedException
	 */
	public abstract BigDecimal getRate(Currency currency) throws JSONException, CurrencyNotSupportedException;
}
