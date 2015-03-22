/**
 * PropertiesLoader
 *
 * Class untuk membaca file config.properties 
 *
 * @package		id.bri.switching.helper
 * @author		PSD Team
 * @copyright           Copyright (c) 2013, PT. Bank Rakyat Indonesia (Persero) Tbk,
 */

// ---------------------------------------------------------------------------------

/*
 * ------------------------------------------------------
 *  Memuat package dan library
 * ------------------------------------------------------
 */

package id.bri.switching.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
 
public class PropertiesLoader {
	
	private static Properties PROP;
	
	protected static Properties getProp() {
		if (PROP == null) {
			PROP = new Properties();
			InputStream input = null;		 
			try {		 
				input = PropertiesLoader.class.getClassLoader().getResourceAsStream("config.properties");
				//input = new FileInputStream("config.properties");
				//input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
			    if (input == null)
			    {
			      throw new FileNotFoundException("property file not found in the classpath");
			    }
				// load a properties file
				PROP.load(input);
				
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return PROP;
	}
	
	public static String getProperty(String propStr) {
		return getProp().getProperty(propStr);		
	}
}