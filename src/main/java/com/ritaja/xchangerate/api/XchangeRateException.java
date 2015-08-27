package com.ritaja.xchangerate.api;

/**
 * Created by rsengupta on 22/08/15.
 */
public class XchangeRateException extends Exception {
	public XchangeRateException() {
		super();
	}

	public XchangeRateException(String message) {
		super(message);
	}

	public XchangeRateException(String message, Throwable cause) {
		super(message, cause);
	}

	public XchangeRateException(Throwable cause) {
		super(cause);
	}
}
