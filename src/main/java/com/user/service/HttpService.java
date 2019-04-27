package com.user.service;

import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service("httpService")
public class HttpService {

	private CloseableHttpClient giphyConnect() {
		try {			
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
	        credsProvider.setCredentials(
	        		new AuthScope(new HttpHost("api.giphy.com")),
	                new UsernamePasswordCredentials("", ""));
	        
	        RequestConfig defaultRequestConfig = RequestConfig.custom()
		    	    .setSocketTimeout(5000)
		    	    .setConnectTimeout(5000)
		    	    .setConnectionRequestTimeout(5000)
		    	    .build();
	        
	        CloseableHttpClient httpclient = HttpClients.custom()
	                .setDefaultCredentialsProvider(credsProvider)
	                .setDefaultRequestConfig(defaultRequestConfig)
	                .build();
	        return httpclient;
	        
		} catch (Exception ex) {
			
		}
		return null;
	}
	
	public Map<String,Object> getSearchData(String searchString) { 
		return this.getData(searchString);
	}
	
	public Map<String,Object> getSearchStaticData() { 
		return this.getData(null);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String,Object> getData(String searchString) { 
		try { 
			CloseableHttpClient httpclient = this.giphyConnect();
 			String url = "";
 			if (StringUtils.isEmpty(searchString)) {
 				url = "http://api.giphy.com/v1/gifs/search?q=ryan+gosling&api_key=hqxZtQVtC2c24Obu5maK6o3mZwIrgdFD&limit=20";
 			} else {
 				url = String.format("http://api.giphy.com/v1/gifs/search?q=%s&api_key=hqxZtQVtC2c24Obu5maK6o3mZwIrgdFD&limit=20", searchString.replace(" ", "+"));
 			}
 			HttpGet httpGet = new HttpGet(url);
 	 		httpGet.setHeader("Accept", "application/json");
 	 		httpGet.setHeader("Content-Type", "application/json");
 	        CloseableHttpResponse response = httpclient.execute(httpGet); 
 	        
 	       try {
	 	        if (response !=null && response.getStatusLine().toString().contains("OK")) {
	 	        	String responseBody = EntityUtils.toString(response.getEntity());
	 	            ObjectMapper resultMap = new ObjectMapper();
	 				Map<String,Object> resultMapObj = resultMap.readValue(responseBody, Map.class); 
	 				return resultMapObj;
 					
	 	        }
 	       } catch (Exception ex) {
 	    	   
 	       }
		} catch (Exception ex) {
			
		}
		return null;
	}
	
}