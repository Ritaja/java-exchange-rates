package com.ritaja.xchangerate.service;

/**
 * Created by rsengupta on 03/09/15.
 */
public enum HttpMethods {
	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE");

	private String method;

	private HttpMethods(String method) {
		this.method = method;
	}
	public String toString(){return method;}
	/**
	 * Constants only
	 */
}
