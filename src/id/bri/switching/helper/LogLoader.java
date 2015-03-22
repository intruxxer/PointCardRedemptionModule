/**
 * LogLoader
 *
 * Class untuk menyimpan log aplikasi
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

//import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.PropertyConfigurator;

public class LogLoader {
	
	public static Logger LOG;
		
	/*public static PropertiesLoader getInstance() {
		return new PropertiesLoader();
	}*/
	
	public static Logger getLog() {
		if (LOG == null) {
		    PropertyConfigurator.configure(LogLoader.class.getClassLoader().getResourceAsStream("log4j-switching.properties"));
			LOG = LoggerFactory.getLogger(LogLoader.class);
		
			try {		 
				/*input = PropertiesLoader.class.getClassLoader().getResourceAsStream("config.properties");
				//input = new FileInputStream("config.properties");
				
				// load a properties file
				LOG.load(input);*/
		
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return LOG;
	}
	
	public static void setInfo(String clsName, String msg) {
        //String temp = new Timestamp(System.currentTimeMillis()).toString() + "::" + clsName;
		getLog().info("{}: {}", clsName, msg);
	}
	
	public static void setError(String clsName, Object msg) {
        //String temp = new Timestamp(System.currentTimeMillis()).toString() + "::" + clsName;
		getLog().error("{}: {}", clsName, msg);
	}
}