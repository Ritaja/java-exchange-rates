package com.ritaja.xchangerate.service;

/**
 * Created by rsengupta on 03/09/15.
 */
public class ServiceException extends Exception {
	public ServiceException() {
		super();
	}
	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}
}
