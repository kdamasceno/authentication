package br.pucrio.tecgraf.demo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Cfg {
	private static Cfg cfg = new Cfg();
	Properties properties = new Properties();

	private Cfg() {
		loadProperties();
	}

	public String getProperty(String key) {

		String value = System.getenv(key);
		if (value != null && !value.trim().equals("")) {
			return value.trim();
		}
		
		value = properties.getProperty(key);
		if (value != null && !value.trim().equals("")) {
			return value.trim();
		}

		value = System.getProperty(key);
		if (value != null && !value.trim().equals("")) {
			return value.trim();
		}
		
		return null;
	}
	
	public void loadProperties() {
		InputStream input = null;
		try {

			try {
				InputStream is = Loader.getResource("internal-config.properties");
				properties.load(is);
				is.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			String filename = getProperty("configPath");									
			if( filename != null && !filename.trim().equals("")) {
				input = new FileInputStream(filename);
				properties.load(input);
			}			

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Cfg instance() {
		return cfg;
	}	

}
