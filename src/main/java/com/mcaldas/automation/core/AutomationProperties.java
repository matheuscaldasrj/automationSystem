/**
 * 
 */
package com.mcaldas.automation.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.mcaldas.automation.exceptions.CommandInvalidException;

/**
 * Automation properties definition
 *  
 * @author mcaldas
 */
@Component
@ConfigurationProperties(prefix="automation")
public class AutomationProperties {

	private String configLightMapping;
	private String nodePath;
	private String light1;
	private String light2;
	private String light3;
	private String door1;
	private String serverKey;
	
	
	public String getConfigLightMapping() {
		return configLightMapping;
	}
	public void setConfigLightMapping(String configLightMapping) {
		this.configLightMapping = configLightMapping;
	}
	public String getNodePath() {
		return nodePath;
	}
	public void setNodePath(String nodePath) {
		this.nodePath = nodePath;
	}

	public void setLight1(String light1) {
		this.light1 = light1;
	}

	public void setLight2(String light2) {
		this.light2 = light2;
	}

	public void setLight3(String light3) {
		this.light3 = light3;
	}
	
	public String getDoor1() {
		return door1;
	}
	public void setDoor1(String door1) {
		this.door1 = door1;
	}
	public String getLightPin(String lightNumber) throws CommandInvalidException {
		switch (lightNumber) {
		case "1":
			return this.light1;
		case "2":
			return this.light2;
		case "3":
			return this.light3;
		default:
			break;
		}
		throw new CommandInvalidException(
				"Received light number '" + lightNumber + "' but the availables values for light are 1,2 and 3");
	}
	
	public String getDoorPin(String doorNumber) throws CommandInvalidException {
		switch (doorNumber) {
		case "1":
			return this.door1;
		default:
			break;
		}
		throw new CommandInvalidException(
				"Received doorNumber '" + doorNumber + "' but the availables values for light are 1");
	}
	
	public String getServerKey() {
		return serverKey;
	}
	public void setServerKey(String serverKey) {
		this.serverKey = serverKey;
	}

	
	
	


}
