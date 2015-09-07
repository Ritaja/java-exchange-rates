package com.ritaja.xchangerate.endpoint;

/**
 * Created by rsengupta on 03/09/15.
 */
public class EndpointException extends Exception {
	public EndpointException() {
		super();
	}
	public EndpointException(String message) {
		super(message);
	}

	public EndpointException(String message, Throwable cause) {
		super(message, cause);
	}

	public EndpointException(Throwable cause) {
		super(cause);
	}
}
