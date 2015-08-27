package com.ritaja.xchangerate.api;

import com.ritaja.xchangerate.util.Currency;

public interface CurrencyConverter {

	public Double convertCurrency(double moneyAmount, Currency fromCurrency, Currency toCurrency) throws XchangeRateException, CurrencyNotSupportedException;
}
