package com.mcaldas.automation.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
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
	private String userToken = "dbk-9Ziem54:APA91bF5jn6LLAahm_F-L8wJ1dLOt80ViF7WzO3uicbYG7ntNgXyo1s716J_f2ALqedJM7SHGttf3Ro40B2Pzwhv9l4nb5AePaaWwsAuruz6QjC5bayOla9rk5PsRVT8sGQCNW3WQLYt";
	private String masterPass;
	private Map<String,JSONObject> currentAlerts = new HashMap<String,JSONObject>();
	
	
	@Autowired
	public AutomationRest(AutomationProperties automationProperties) {
		this.automationProperties = automationProperties;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/temperature")
	public String getCurrentTemperature() throws ClientProtocolException, IOException {
		String url = this.automationProperties.getNodePath() + "/temperature";
		return makeGetRequest(url);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/humidity")
	public String getHumidity() throws ClientProtocolException, IOException {
		String url = this.automationProperties.getNodePath() + "/humidity";
		return makeGetRequest(url);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/rain")
	public String getRainValue() throws ClientProtocolException, IOException {
		String url = this.automationProperties.getNodePath() + "/rain";
		return makeGetRequest(url);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/alerts")
	public Object[] getAlerts() throws ClientProtocolException, IOException {
		return currentAlerts.keySet().toArray();
		
	}
	
	
	// is possible to control 3 lights
	@RequestMapping(method = RequestMethod.POST, value = "/light")
	public String changeLight(@RequestParam(value = "id") String id, @RequestParam(value = "value") String value)
			throws Exception {

		String valueToNode = null;

		if (value.equals("on")) {
			valueToNode = "1";
		} else if (value.equals("off")) {
			valueToNode = "0";
		} else {
			throw new CommandInvalidException(
					"Received value for light=" + value + " but the availables values for light are on/off");
		}

		System.out.println("Translating lightNumber: " + id + " with value: '" + value + "' to pin: "
				+ this.automationProperties.getLightPin(id) + " with value: " + valueToNode);
		System.out.println(id + ":" + value);

		String url = this.automationProperties.getNodePath() + "/digital/write/" + this.automationProperties.getLightPin(id)
				+ "/" + valueToNode;
		return this.makePostRequest(url);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/allLights")
	public String changeAllLights(@RequestParam(value = "value") String value)
			throws Exception {

		String valueToNode = null;
		
		if (value.equals("on")) {
			valueToNode = "1";
		} else if (value.equals("off")) {
			valueToNode = "0";
		} else {
			throw new CommandInvalidException(
					"Received value for all lights=" + value + " but the only availables values for light are on/off");
		}
		
		String baseUrl = this.automationProperties.getNodePath() + "/digital/write/";
		String urlLight1 = baseUrl + this.automationProperties.getLightPin("1") + "/" + valueToNode;
		String urlLight2 = baseUrl + this.automationProperties.getLightPin("2") + "/" + valueToNode;
		String urlLight3 = baseUrl + this.automationProperties.getLightPin("3") + "/" + valueToNode;

		makePostRequest(urlLight1);
		makePostRequest(urlLight2);
		makePostRequest(urlLight3);

		return "Comando enviado: " + value + " para todas as luzes";
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/door")
	public String changeDoorState(@RequestParam(value = "id") String id, @RequestParam(value = "password") String password)
			throws Exception {
		
		System.out.println("Teste da porta");
		String url;
		
		if(password.equals("null")) {
			url = this.automationProperties.getNodePath() + "/changeServo/" + this.automationProperties.getDoorPin(id);
			return this.makePostRequest(url);
		}
		
		if(masterPass == null) {
			masterPass = this.automationProperties.getMasterPass();
		}
		if(!password.equals(masterPass)) {
			//bad password;
			return null;
		}

		url = this.automationProperties.getNodePath() + "/changeServo/" + this.automationProperties.getDoorPin(id);

		return this.makePostRequest(url);

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/setToken")
	public void setToken(@RequestParam(value = "token") String token){
		this.userToken = token;	
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/notification")
	public void notifications(@RequestBody Map<String,String> bodyValue) throws JSONException {
		
		System.out.println("Notificação recebida");
		System.out.println("Body message: ");
		System.out.println(bodyValue);
		
		String messageString = bodyValue.get("message");
		
		JSONObject message = new JSONObject();
		message.put("title", "Alerta");
	    message.put("message", messageString);
	    JSONObject protocol = new JSONObject();
	    protocol.put("to", this.userToken);
	    protocol.put("data", message);
	    
	    if(currentAlerts.get(messageString) == null) {	    	
	    	currentAlerts.put(messageString, message);
	    } else {
	    	System.out.println("Alerta não enviado pois já estava na lista de alertas!!");
	    	return ;
	    }
		 try {
		        HttpClient httpClient = HttpClientBuilder.create().build();

		        HttpPost request = new HttpPost("https://fcm.googleapis.com/fcm/send");
		        request.addHeader("content-type", "application/json");
		        request.addHeader("Authorization", "key=" + this.automationProperties.getServerKey());
		        
		        StringEntity paramsEntity = new StringEntity(protocol.toString());
		        request.setEntity(paramsEntity);
		        System.out.println(paramsEntity);

		        HttpResponse response = httpClient.execute(request);
		        System.out.println(response.toString());
		    } catch (Exception e) {
		    }
		 
		

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/deleteAlert")
	public Object[] deleteAlarm(@RequestParam(value = "alertMessage") String alertMessage) throws JSONException { 
		System.out.println("Trying to remove alert: " + alertMessage);
		currentAlerts.remove(alertMessage);
		return currentAlerts.keySet().toArray();
	}

	private String makeGetRequest(String url) throws ClientProtocolException, IOException {
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
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

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