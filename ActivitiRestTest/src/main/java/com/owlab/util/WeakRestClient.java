package com.owlab.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;





/**
 * A simple RESTful client for Activiti REST API
 * Use with your own risks!
 *  
 * @author Nemo Hunjae Lee
 * 
 * Usuage example:
 * WeakRestClient.RestRespone respone = WeakRestClient.get(<URL>) // also delete for DELETE method
 * 											.basicAuth("auth user id", "auth password")
 * 											.queryString("paramName", "paramValue")
 * 											.execute();
  * WeakRestClient.RestRespone respone = WeakRestClient.post(<URL>) // also put for PUT method
 * 											.header("content-type", "application/json")
 * 											.basicAuth("auth user id", "auth password")
 * 											.bodyAsJsonNode(< a JsonNode object>) // or .body(String)
 * 											.execute();
 * response.statueCode => HTTP STATUS CODE
 * response.responseBody => String
 * response.asJsonNode() => return JsonNode object of the body string
 *
 */
public class WeakRestClient {
	private enum RequestType {ENTITY_ENCLOSING, NON_ENTITY_ENCLOSING}
	private RequestType requestType;
	private List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
	
	private DefaultHttpClient httpClient;
	private URI uri;
	private HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase;
	private HttpRequestBase httpRequestBase;
	private WeakRestClient(String uri) throws URISyntaxException {
		this.httpClient = new DefaultHttpClient();
		this.uri = new URI(uri);
	}
	
	public static class RestResponse {
		public final int statusCode;
		public final String responseBody;
		public RestResponse(int status, String responseString) {
			this.statusCode = status;
			this.responseBody = responseString;
		}
		/*
		 * Following helper methods should be called when the status is 200, mostly.
		 */
		public JsonNode asJsonNode() throws JsonProcessingException, IOException {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readTree(this.responseBody);
		}
	}
	
	
	public static WeakRestClient get(String uri) throws URISyntaxException {
		WeakRestClient restClient = new WeakRestClient(uri);
		restClient.requestType = RequestType.NON_ENTITY_ENCLOSING;
		restClient.httpRequestBase = new HttpGet();
		return restClient;
	}
	
	public static WeakRestClient put(String uri) throws URISyntaxException {
		WeakRestClient restClient = new WeakRestClient(uri);
		restClient.requestType = RequestType.ENTITY_ENCLOSING;
		restClient.httpEntityEnclosingRequestBase = new HttpPut();
		return restClient;
	}
	
	public static WeakRestClient post(String uri) throws URISyntaxException {
		WeakRestClient restClient = new WeakRestClient(uri);
		restClient.requestType = RequestType.ENTITY_ENCLOSING;
		restClient.httpEntityEnclosingRequestBase = new HttpPost();
		return restClient;
	}
	
	public static WeakRestClient delete(String uri) throws URISyntaxException {
		WeakRestClient restClient = new WeakRestClient(uri);
		restClient.requestType = RequestType.NON_ENTITY_ENCLOSING;
		restClient.httpRequestBase = new HttpDelete();
		return restClient;
	}
	
	public WeakRestClient header(String name, String value) {
		if(this.requestType == RequestType.NON_ENTITY_ENCLOSING)
			this.httpRequestBase.addHeader(name, value);
		if(this.requestType == RequestType.ENTITY_ENCLOSING)
			this.httpEntityEnclosingRequestBase.addHeader(name, value);
		
		return this;
	}
	
	public WeakRestClient basicAuth(String userId, String password) {
		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(this.uri.getHost(), this.uri.getPort()),
				new UsernamePasswordCredentials(userId, password));
		return this;
	}
	
	public WeakRestClient queryString(String parameterName, String parameterValue) {
		this.queryParameters.add(new BasicNameValuePair(parameterName, parameterValue));
		return this;
	}
	
	public WeakRestClient queryString(Map<String, String> queryParameters) {
		Set<String> keySet = queryParameters.keySet();
		for(String key: keySet) {
			this.queryParameters.add(new BasicNameValuePair(key, queryParameters.get(key)));
		}
		
		return this;
	}
	
	public WeakRestClient body(String contents) throws UnsupportedEncodingException {
		if(this.requestType == RequestType.ENTITY_ENCLOSING)
			this.httpEntityEnclosingRequestBase.setEntity(new StringEntity(contents));
		if(this.requestType == RequestType.NON_ENTITY_ENCLOSING)
			System.out.println("Entity enclosing not supported in this type of http method: " + this.httpRequestBase.getMethod());
		return this;
	}
	
	
	
	
	public RestResponse execute() throws URISyntaxException, ClientProtocolException, IOException {
		if(this.requestType == RequestType.NON_ENTITY_ENCLOSING) {
			if(this.queryParameters.size() > 0) {
				String query = URLEncodedUtils.format(this.queryParameters, "UTF-8");
				this.httpRequestBase.setURI(new URI(this.uri.toString() + "?" + query));
			}  else {
				this.httpRequestBase.setURI(this.uri);
			}
		} else if(this.requestType == RequestType.ENTITY_ENCLOSING) {
			this.httpEntityEnclosingRequestBase.setURI(this.uri);
		} else  {
		}
		RestResponse restResponse = null;
		try {
			HttpResponse httpResponse = null;
			if(this.requestType == RequestType.NON_ENTITY_ENCLOSING)
				httpResponse = this.httpClient.execute(this.httpRequestBase);
			if(this.requestType == RequestType.ENTITY_ENCLOSING)
				httpResponse = this.httpClient.execute(this.httpEntityEnclosingRequestBase);
			restResponse = new RestResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(
					httpResponse.getEntity(), "UTF-8"));
			
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		return restResponse;
	}
	
	public WeakRestClient bodyAsJsonNode(JsonNode jsonNode) throws UnsupportedEncodingException, JsonProcessingException {
		if(this.requestType == RequestType.ENTITY_ENCLOSING) {
			final ObjectMapper mapper = new ObjectMapper();
			final Object o = mapper.treeToValue(jsonNode, Object.class);
			final String contents = mapper.writeValueAsString(o);
			System.out.println(contents);
			this.httpEntityEnclosingRequestBase.setEntity(new StringEntity(contents));
		}
		if(this.requestType == RequestType.NON_ENTITY_ENCLOSING)
			System.out.println("Entity enclosing does not support in this type of http method: " + this.httpRequestBase.getMethod());
		return this;
	}
	
	
	
}

