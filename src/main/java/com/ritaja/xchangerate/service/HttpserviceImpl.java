package com.ritaja.xchangerate.service;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rsengupta on 03/09/15.
 */
public class HttpserviceImpl implements HttpService {
	// used for executing requests to the (REST) API
	private CloseableHttpClient httpClient;
	private String uri;
	private HttpRequestBase httpMethod;

	public HttpserviceImpl(String uri) {
		this.uri = uri;
	}

	private void setMethod(HttpMethods method) throws ServiceException {
		if (method.equals(HttpMethods.GET)) {
			httpMethod = new HttpGet(uri);
		} else if (method.equals(HttpMethods.POST)) {
			httpMethod = new HttpPost(uri);
		} else {
			throw new ServiceException("Unsupported HTTP Method: " + method.toString() + "for HttpserviceImpl class");
		}
	}

	public JSONObject getResponse(HttpMethods method) throws JSONException, ServiceException {
		try {
			setMethod(method);
			httpClient = HttpClients.createDefault();
			CloseableHttpResponse response = httpClient.execute(httpMethod);
			HttpEntity entity = response.getEntity();
			JSONObject responseRates = new JSONObject(EntityUtils.toString(entity));
			//checkResponse();
			//saveRates();
			httpClient.close();
			return responseRates;
		} catch (IOException e) {
			throw new ServiceException(e.getMessage());
		}
	}
}
