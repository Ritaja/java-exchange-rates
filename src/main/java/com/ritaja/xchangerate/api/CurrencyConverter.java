package com.ritaja.xchangerate.api;

import java.math.BigDecimal;

import com.ritaja.xchangerate.util.Currency;

public interface CurrencyConverter {

	public BigDecimal convertCurrency(BigDecimal moneyAmount, Currency fromCurrency, Currency toCurrency) throws XchangeRateException, CurrencyNotSupportedException;
}
