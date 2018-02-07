package com.mcaldas.automation.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcaldas.automation.exceptions.CommandInvalidException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/automation")
public class AutomationRest {

	private AutomationProperties automationProperties;

	@Autowired
	public AutomationRest(AutomationProperties automationProperties) {
		this.automationProperties = automationProperties;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/temperature")
	public String getCurrentTemperature() throws ClientProtocolException, IOException {
		String url = this.automationProperties.getNodePath() + "/temperature";
		return makeGetRequest(url);
	}

	// is possible to control 3 lights
	@RequestMapping(method = RequestMethod.POST, value = "/light")
	public String changeLight(@RequestParam(value = "id") String id, @RequestParam(value = "value") String value)
			throws Exception {

		String valueToNode = null;
		if (!id.equals("1") && !id.equals("2") && !id.equals("3")) {
			System.out.println("Id inválido");
			throw new CommandInvalidException(
					"Received light id=" + id + " but the only availables values for light are 1,2 and 3");
		}

		if (value.equals("on")) {
			valueToNode = "1";
		} else if (value.equals("off")) {
			valueToNode = "0";
		} else {
			throw new CommandInvalidException(
					"Received value for light=" + value + " but the only availables values for light are on/off");
		}

		System.out.println("Translating lightNumber: " + id + " with value: '" + value + "' to pin: "
				+ this.automationProperties.getLightPin(id) + " with value: " + valueToNode);
		System.out.println(id + ":" + value);

		String url = this.automationProperties.getNodePath() + "/light/" + this.automationProperties.getLightPin(id)
				+ "/" + valueToNode;
		return this.makePostRequest(url);
	}

	public String makeGetRequest(String url) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);

		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		return result.toString();
	}

	private String makePostRequest(String url) throws Exception {

		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		con.setRequestMethod("POST");

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());

		return response.toString();

	}
}