/**
 * SwitchingApp
 *
 * Sebuah aplikasi middleware yang melayani switching iso message 8583
 * transaksi
 *
 * @package		id.bri.switching.app
 * @author		PSD Team
 * @copyright           Copyright (c) 2013, PT. Bank Rakyat Indonesia (Persero) Tbk,
 * @since		Version 1.0
 */

// ---------------------------------------------------------------------------------

/*
 * ------------------------------------------------------
 *  Memuat package dan library
 * ------------------------------------------------------
 */

package id.bri.switching.app;

import id.bri.switching.helper.LogLoader;
import id.bri.switching.helper.MQHelper;
import id.bri.switching.helper.PropertiesLoader;

import id.bri.switching.mq.MQServer;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Message;

//  Class SwitchingApp
public class SwitchingApp {
    
    /* 
     * Parameter
     * ---------------------------------------------------------------------
     */
	public static MQHelper mq;    
	public static MQServer mqserver;
	
    /**
     * main
     * ------------------------------------------------------------------------
     * 
     * Fungsi awal yang dijalankan oleh aplikasi. 
     * Membuat proses listener message, dan membalas message ke activemq
     * 
     * @access      public
     * @param       String[]
     * @return      void
     */
		
	/**
     * startListener
     * ------------------------------------------------------------------------
     * 
     * Function to start listener. It is used for transactions that need to listen
     * to the activeMQ.
     * 
     * @access      public
     * @return		void
     */
	public void startListener() {
		// Start listener for card verification
		
	    mqserver = new MQServer();
	    mqserver.openConnection(PropertiesLoader.getProperty("MQ_URL"));
	    mqserver.setupMessageConsumer(PropertiesLoader.getProperty("POINTCARDQUEUEREQUEST"), 
	    		 PropertiesLoader.getProperty("POINTCARDQUEUERESPONSE"));
		
	}    
    
    // ---------------------------------------------------------------------------------
    
    /**
     * getMq
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk mengembalikan MQHelper
     * 
     * @access      public
     * @return      MQHelper
     */
    
    public MQHelper getMq() {
    	return mq;
    }
}
