package com.ritaja.xchangerate.api;

/**
 * Created by rsengupta on 22/08/15.
 */
public class CurrencyNotSupportedException extends Exception{
	public CurrencyNotSupportedException() {
		super();
	}

	public CurrencyNotSupportedException(String message) {
		super(message);
	}

	public CurrencyNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public CurrencyNotSupportedException(Throwable cause) {
		super(cause);
	}
}
