import java.io.IOException;

import javax.jms.JMSException;

import id.bri.switching.helper.ISO8583PSWPackager;
import id.bri.switching.helper.LogLoader;
import id.bri.switching.helper.PropertiesLoader;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOException;
import org.jpos.iso.packager.GenericPackager;

import id.bri.switching.app.*;
import id.bri.switching.helper.*;
import id.bri.switching.mq.*;


public class ExperimentalPSWactiveMQ {
	// Require:  iptables -A INPUT -i eth0 -p tcp --destination-port [PortNo] -j ACCEPT 
	// e.g. 3306 for MySQL [PortNo], 61616 for ActiveMQ
	public static void main(String[] args) {
		try{
			String isoMessage = "";
			ISO8583PSWPackager packager = new ISO8583PSWPackager();
			//GenericPackager packager = new GenericPackager("packager/iso8583.xml");
			ISOMsg isoMsg = new ISOMsg();
	        isoMsg.setPackager(packager);
	        
	        //******	PART 1 ******//
	        //AS IF WE ARE PSW
	        
	        //1 (a)
	        //Packing an ISO Message
	        /* START COMMENT
	        isoMsg.setMTI("0200");
	        isoMsg.set(2, "0004259678945688902");
			isoMsg.set(3, "101010");
			byte[] result = isoMsg.pack();
			
			// Produce output ISO 8583 Message String
	        for (int i = 0; i < result.length; i++) {
	            isoMessage += (char) result[i];
	        }
            System.out.println("1"+String.valueOf(result)); 
			System.out.println("2"+isoMessage);
			System.out.println("3"+isoMsg);
			//1 (b)
			//Publisher to ActiveMQ
			try { 
			MQClient mqclient = new MQClient();
			mqclient.openConnection("tcp://128.199.102.160:61616");
		    mqclient.setupMessageProducer("PswPoint.Response", isoMessage); // We set PSWPoint.Request because we dont have message yet from PSW
			} catch (JMSException e) {
	        	if (e.getLinkedException() instanceof IOException) {
	                // ActiveMQ is not running. Do some logic here.
	                // use the TransportListener to restart the activeMQ connection
	                // when activeMQ comes back up.
	        		
	        	} else if (e.getMessage().contains("Connection refused")) {
	        		LogLoader.setError(MQServer.class.getSimpleName(), "Cannot connect to MQ, connection refused");
	        	} else {
	        		LogLoader.setError(MQServer.class.getSimpleName(), "Cannot connect to MQ, error unknown");
	        	}
	        }
			// END COMMENT
			*/
			
			//******	PART 2 ******//
	        //CORE JOBS - To listen to activeMQ and response to it accordingly
	        
			//2 (a)
			//Subscriber to ActiveMQ; 
	        //also acting as Response Publisher if necessary (depending on what we receive from PSW)
			
			MQServer mqserver = new MQServer();
		    mqserver.openConnection("tcp://128.199.102.160:61616");
		    //Listening/Subscribe to "PswPoint.Request", Response/Publish to "PswPoint.Response"
		    mqserver.setupMessageConsumer("PswPoint.Request", "PswPoint.Response");
			
		    //2 (b)
	        //Unpacking an ISO Message obtained from ActiveMQ
			
			/*
	        String requestString = "0800822000000000000004000000000000000224200936083503002";
	        isoMsg.unpack(requestString.getBytes());
			// Print the unpacked ISO8583
	        System.out.println("MTI='"+isoMsg.getMTI()+"'");
	        for(int i=1; i<=isoMsg.getMaxField(); i++){
	            if(isoMsg.hasField(i))
	                System.out.println(i+"='"+isoMsg.getString(i)+"'");
	        	}
        	*/
			
			}catch (Exception e) {//ISOException e) {
	            e.printStackTrace();
	        }
	        
			

	}

}
