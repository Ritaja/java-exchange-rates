package com.ritaja.xchangerate.util;

import com.ritaja.xchangerate.endpoint.YahooEndpoint;
import com.ritaja.xchangerate.storage.FileStore;

/**
 * Created by rsengupta on 06/09/15.
 */
public enum Strategy {
	YAHOO_FINANCE_FILESTORE("YAHOO_FINANCE_FILESTORE"),

	CURRENCY_LAYER_FILESTORE("CURRENCY_LAYER_FILESTORE"),

	// yet to be implemented
	YAHOO_FINANCE_MONGOSTORE("YAHOO_FINANCE_MONGOSTORE"),

	CURRENCY_LAYER_MONGOSTORE("CURRENCY_LAYER_MONGOSTORE");

	private final String strategy;

	private Strategy(String strategy) {
		this.strategy = strategy;
	}

	public String toString() {
		return this.strategy;
	}
	/**
	 * Constants only
	 */
}
