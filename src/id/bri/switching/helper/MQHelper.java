/**
 * MQHelper
 *
 * Helper untuk mengirimkan message balasan ke activemq
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
import id.bri.switching.app.SwitchingApp;

import java.io.IOException;
//import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
//import javax.jms.Queue;
//import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

//import org.jpos.iso.ISOException;


//import java.util.Random;





import org.apache.activemq.ActiveMQConnectionFactory;
//import org.jpos.iso.ISOMsg;

//  Class MQHelper
public class MQHelper {
    
    /* 
     * Parameter
     * ---------------------------------------------------------------------
     */
    
    Connection connection;
	Session session;    
	MessageProducer producer;
    MessageConsumer consumer;
    Destination sendQueue;
    Destination replyQueue;
    String corrId;
    
    /**
     * openConnection
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk membuka koneksi ke activemq
     * 
     * @access      public
     * @throws      JMSException
     * @return      void
     */
    
    public void openConnection(String mqUrl) throws JMSException {
        try {        	
	    	//  Koneksi ke activemq
	    	ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(mqUrl);
	    	// set transport listener so that active MQ start is notified.
	    	//connectionFactory.setTransportListener(transportListenerObject); 
	        setConnection(connectionFactory.createConnection());
	        getConnection().start();
        	
        } catch (JMSException e) {
        	if (e.getLinkedException() instanceof IOException) {
                // ActiveMQ is not running. Do some logic here.
                // use the TransportListener to restart the activeMQ connection
                // when activeMQ comes back up.
        		LogLoader.setError(MQHelper.class.getSimpleName(), "Cannot connect to MQ. Check the MQ to make sure it is running");  
        	} else if (e.getMessage().contains("Connection refused")) {
        		LogLoader.setError(MQHelper.class.getSimpleName(), "Cannot connect to MQ, connection refused");      		
        	} else {
        		LogLoader.setError(MQHelper.class.getSimpleName(), "Cannot connect to MQ, error unknown");
        	}
        }
    }
    
    /**
     * sendMessage
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk mengirim message ke activemq melalui MessageProducer
     * 
     * @access      public
     * @throws      JMSException
     * @return      Message
     * @throws 		JMSException 
     */
    
    public Message sendMessage(String message, String corrid) throws JMSException {
    	try {
	    	Message reply;
	    	if (this.connection == null) {
	    		openConnection(PropertiesLoader.getProperty("MQ_URL"));
	    	}
	    	createSyncProducer(PropertiesLoader.getProperty("SWITCHQUEUEREQUEST"));
	    	createSyncConsumer(PropertiesLoader.getProperty("SWITCHQUEUERESPONSE"));
	    	// create message to be send
	    	TextMessage textMessage = session.createTextMessage(message);
	        textMessage.setJMSReplyTo(replyQueue);
	        textMessage.setJMSCorrelationID(corrid);
	        
	        //LogLoader.setInfo(MQHelper.class.getSimpleName(), "Sending msg, corrId: " + corrid);
	        producer.send(textMessage);        
	        //LogLoader.setInfo(MQHelper.class.getSimpleName(), "Waiting for reply..."); 
	        
	        /*ISO8583PSWPackager packager = new ISO8583PSWPackager();
	        ISOMsg isoMsg = new ISOMsg();
	        isoMsg.setPackager(packager);
	        
	        //MessageConsumer messageConsumer = session.createConsumer(replyQueue, "JMSMessageID='"+corrId+"'");
	        //Message msgConsume = messageConsumer.receiveNoWait();
	        
	        reply = null;
	        Queue replyqueuebrows = session.createQueue(PropertiesLoader.getProperty("SWITCHQUEUERESPONSE"));
	        QueueBrowser queueBrowser = session.createBrowser(replyqueuebrows);
	        Enumeration msgs = queueBrowser.getEnumeration();
	        boolean isMsgFound = false;
	        if ( !msgs.hasMoreElements() ) { 
	            LogLoader.setInfo(MQHelper.class.getSimpleName(), "No messages in queue");
	        } else { 
	            while (msgs.hasMoreElements() && !isMsgFound) { 
	                TextMessage tempMsg = (TextMessage)msgs.nextElement(); 
	                LogLoader.setInfo(MQHelper.class.getSimpleName(), "Message: " + tempMsg.getText()); 
	                // message is unpack to get bit37 value for correlation id	    	        
	    	        isoMsg.unpack(tempMsg.getText().getBytes());
	    	        if (isoMsg.getString(37).equals(String.format("%012d", Long.valueOf(corrid)))) {
	    	        	//reply = tempMsg;
	    	        	LogLoader.setInfo("TEST", isoMsg.getString(37) + " : " + tempMsg.getJMSCorrelationID() + " : " + tempMsg.getJMSCorrelationIDAsBytes().toString());
	    	        	isMsgFound = true;
	    	        	reply = tempMsg;
	    	        }
	            }
	        }*/
	        
	        reply = consumer.receive(30000);
	        while (reply != null) {
	        	if (!reply.getJMSCorrelationID().equals(corrid)) {	// if reply msg not match, then search for another msg
	        		reply = null;
	        		reply = consumer.receive(30000);
	        	} else {	// msg with matching correlation id is found
	        		break;
	        	}
	        }
	        //do {
	        //	reply = consumer.receive(30000);
	        //} while ((reply == null) || (reply.getJMSCorrelationID() != corrId));
	        consumer.close();
	        producer.close();
	        session.close();
	        //closeConnection();
	        return reply;
    	} catch (JMSException e) {
    		LogLoader.setError(MQHelper.class.getSimpleName(), e);
    		consumer.close();
	        producer.close();
	        session.close();
    		return null;
    	} catch (NullPointerException e) {
        	LogLoader.setError(SwitchingApp.class.getSimpleName(), "No message from ProSW");
        	consumer.close();
	        producer.close();
	        session.close();
        	return null;        
    	}/* catch (ISOException e) {
     		LogLoader.setError(MQHelper.class.getSimpleName(), e);
     		return null;
     	}*/
    }
    
    /**
     * sendMessageCorp
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk mengirim message autodebet corp ke activemq melalui MessageProducer
     * 
     * @access      public
     * @throws      JMSException
     * @return      Message
     * @throws 		JMSException 
     */
    
    public Message sendMessageCorp(String message, String corrid) throws JMSException {
    	try {
	    	Message reply;
	    	if (this.connection == null) {
	    		openConnection(PropertiesLoader.getProperty("MQ_URL"));
	    	}
	    	createSyncProducer(PropertiesLoader.getProperty("SWITCHCORPQUEUEREQUEST"));
	    	createSyncConsumer(PropertiesLoader.getProperty("SWITCHCORPQUEUERESPONSE"));
	    	// create message to be send
	    	TextMessage textMessage = session.createTextMessage(message);
	        textMessage.setJMSReplyTo(replyQueue);
	        textMessage.setJMSCorrelationID(corrid);
	        
	        //LogLoader.setInfo(MQHelper.class.getSimpleName(), "Sending msg, corrId: " + corrid);
	        producer.send(textMessage);        
	        //LogLoader.setInfo(MQHelper.class.getSimpleName(), "Waiting for reply..."); 
	        
	        reply = consumer.receive(30000);
	        while (reply != null) {
	        	if (!reply.getJMSCorrelationID().equals(corrid)) {	// if reply msg not match, then search for another msg
	        		reply = null;
	        		reply = consumer.receive(30000);
	        	} else {	// msg with matching correlation id is found
	        		break;
	        	}
	        }
	        //do {
	        //	reply = consumer.receive(30000);
	        //} while ((reply == null) || (reply.getJMSCorrelationID() != corrId));
	        consumer.close();
	        producer.close();
	        session.close();
	        //closeConnection();
	        return reply;
    	} catch (JMSException e) {
    		LogLoader.setError(MQHelper.class.getSimpleName(), e);
    		consumer.close();
	        producer.close();
	        session.close();
    		return null;
    	} catch (NullPointerException e) {
        	LogLoader.setError(SwitchingApp.class.getSimpleName(), "No message from ProSW");
        	consumer.close();
	        producer.close();
	        session.close();
        	return null;        
    	}
    }
    
    /*public String replyMessage() throws JMSException {
    	String response;
    	createSyncConsumer(PropertiesLoader.getProperty("SWITCHQUEUERESPONSE"));
    	// proses reply message
        Message reply = consumer.receive();
        
        if(reply != null){
        	if (reply instanceof TextMessage) {
	            TextMessage textMsg = (TextMessage) reply;
	            response = Router.listenerRouter(textMsg.getText());  
        	} else {
        		response = "response is not a text message";
        	}
        }
        else {
            response = "No response from queue";
        }        
        consumer.close();
        closeConnection();
        return response;
    }*/
    
    // ---------------------------------------------------------------------------------
    
    /**
     * createSyncProducer
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk membentuk MessageProducer dgn metode synchronous
     * 
     * @access      public
     * @throws      JMSException
     * @return      void
     * @throws		JMSException
     */
    public void createSyncProducer(String switchQueueRequest) throws JMSException {
    	try {
	        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);     
	        sendQueue = session.createQueue(switchQueueRequest);        
	        
	        //  Producer
	        producer = session.createProducer(sendQueue);
	        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	        producer.setTimeToLive(0);
    	} catch (JMSException e) {
    		throw new JMSException("Exception from producer message");
    	}
    }
    
    // ---------------------------------------------------------------------------------
    
    /**
     * createSyncConsumer
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk membentuk MessageConsumer dgn metode synchronous
     * 
     * @access      public
     * @throws      JMSException
     * @return      void
     */
    public void createSyncConsumer(String switchQueueResponse) throws JMSException {
    	try {
	    	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);   
	        replyQueue = session.createQueue(switchQueueResponse);
	      	//this.corrId = String.format("%012d", TraceNumberGenerator.getRRN());
	        //consumer = session.createConsumer(replyQueue, "JMSCorrelationID = "+this.corrId);
	        consumer = session.createConsumer(replyQueue);
    	} catch (JMSException e) {
    		throw new JMSException("Exception from consumer message");
    	}
    }
    
    // ---------------------------------------------------------------------------------
    
    
    /**
     * closeConnection
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk menutup koneksi yang ke activemq
     * 
     * @access      public
     * @throws      JMSException
     * @return      void
     */
    
    public void closeConnection() throws JMSException {
        session.close();
        connection.close();
    }
    
    /**
     * closeSession
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk menutup session dan consumer yang ke activemq
     * 
     * @access      public
     * @throws      JMSException
     * @return      void
     */
    
    public void closeSession() throws JMSException {
        consumer.close();
        session.close();
    }
    
    // ---------------------------------------------------------------------------------
    
    public Connection getConnection() {
		return connection;
	}
    
    public void setConnection(Connection conn) {
    	connection = conn;
    }

    public Session getSession() {
		return session;
	}
    
    public MessageConsumer getConsumer() {
    	return consumer;
    }
    
    public MessageProducer getProducer() {
    	return producer;
    }
}
