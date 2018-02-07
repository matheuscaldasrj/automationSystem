package com.mcaldas.automation;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/automation")
public class AutomationRest {
	
	private String baseNodeUrl = "http://192.168.0.181:8080";
   
	@RequestMapping(
			method=RequestMethod.GET,
			value="/temperature"
	)
	public String getCurrentTemperature() throws ClientProtocolException, IOException {
		String url = baseNodeUrl + "/temperature";	
		return makeGetRequest(url);
	}
	
	
	//is possible to control 3 lights
	@RequestMapping(
			method=RequestMethod.POST,
			value="/light"
	)
	public String changeLight(@RequestParam(value="id") String id, @RequestParam(value="value") String value) {
		System.out.println(id + ":" + value);
		return id + ":" + value;
	}
	
	public String makeGetRequest(String url) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);

		System.out.println("Response Code : "
		                + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		return result.toString();
	}
	
	
}