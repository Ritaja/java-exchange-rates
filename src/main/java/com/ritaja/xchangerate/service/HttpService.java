package com.ritaja.xchangerate.service;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rsengupta on 03/09/15.
 */
public interface HttpService {
	/**
	 * Makes a HTTP REQUEST with the desiered HTTP METHOD and
	 * returns the RESPONSE from the web service as a JSONObject
	 *
	 * @param method the HTTP method
	 * @return response JSONobject of the response from the service
	 * @throws JSONException
	 * @throws ServiceException
	 */
	public JSONObject getResponse(HttpMethods method) throws JSONException, ServiceException;

}
