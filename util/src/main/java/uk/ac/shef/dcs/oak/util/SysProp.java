package uk.ac.shef.dcs.oak.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author annalisa
 *
 */
public class SysProp implements Serializable{

	private static Logger l4j = Logger.getLogger(SysProp.class.getName());

	private Properties properties;
	private String propertiesUrl;

//	private static String propertiesUrl = "/share/raid1/users/annalisa/prop/wIn.properties";

	public String getPropertiesUrl() {
		return propertiesUrl;
	}

	public void setPropertiesUrl(String propertiesUrl) {
		this.propertiesUrl = propertiesUrl;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public SysProp() {
		super();
		this.properties = new Properties();
		
		try {
			l4j.info("Loading properties file");
		   properties.load(new FileInputStream(propertiesUrl));

		} catch (IOException e) {
			   l4j.error("Failed to Load properties, using defaults");

		}

		
	}
	
	public SysProp(String propUrl) {
		super();
		this.propertiesUrl = propUrl;
	
		this.properties = new Properties();
		
		try {
		   l4j.info("Loading properties file");
		   properties.load(new FileInputStream(this.propertiesUrl));

		} catch (IOException e) {
			l4j.error("Failed to Load properties, using defaults");

		}

		
	}
	
	
}
