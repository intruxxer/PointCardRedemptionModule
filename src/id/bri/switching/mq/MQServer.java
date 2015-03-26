/**
 * MQServer
 *
 * Class yang berfungsi seolah seperti server yg mendengarkan request dari prosw melalui MQ,
 * kemudian memproses request tsb dan mengembalikan hasil proses ke prosw melalui MQ
 *
 * @package		id.bri.switching.mq
 * @author		PSD Team
 * @copyright           Copyright (c) 2013, PT. Bank Rakyat Indonesia (Persero) Tbk,
 */

// ---------------------------------------------------------------------------------

/*
 * ------------------------------------------------------
 *  Memuat package dan library
 * ------------------------------------------------------
 */

package id.bri.switching.mq;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import id.bri.switching.app.Router;
import id.bri.switching.helper.LogLoader;
//import id.bri.switching.helper.MQHelper;
import id.bri.switching.helper.PropertiesLoader;

public class MQServer implements MessageListener {

	Connection connection;
	Session session;
	MessageProducer replyProducer;
	String messageQueueProducer;
	
	public Connection getConnection() {
		return connection;
	}
    
    public void setConnection(Connection conn) {
    	connection = conn;
    }
	
	public synchronized void openConnection(String mqUrl) {
        try {        	
	    	//Connection to activeMQ
	    	ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(mqUrl);
	    	this.connection = connectionFactory.createConnection();
	        this.connection.start();
	        
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
    }
	
	public void setupMessageConsumer(String messageQueueRequest, String messageQueueResponse) {
		try {
			if (this.connection == null) {	// check connection
	    		openConnection(PropertiesLoader.getProperty("MQ_URL"));
	    	}
			this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);  
			Destination requestQueue = this.session.createQueue(messageQueueRequest);
			MessageConsumer consumer = this.session.createConsumer(requestQueue);
	        consumer.setMessageListener(this); // Trigger this.onMessage(Message message)
	        
			//This messageQueueProducer() is producer's instance being used later on  [when void onMessage() triggered],
			//because void onMessage() is triggered automatically due to its nature as a must-be-override interface;
			//This approach is carried out as a way of global variable for void onMessage() to determine
			//MQ Topic to which an MQ Server will put a response to.
			this.messageQueueProducer = messageQueueResponse;
	        LogLoader.setInfo(MQServer.class.getSimpleName(), "Listener: ON");
						
		} catch (JMSException e) {
			LogLoader.setError(MQServer.class.getSimpleName(), e);
		} catch (Exception e) {
			LogLoader.setError(MQServer.class.getSimpleName(), e);
		}	
	}
	
	public void onMessage(Message message) {
        try {
            
            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String messageText = txtMsg.getText();
                //System.out.println(messageText);
                
                // Calling Router() object to process the request from PWS.
                // Here, ISOMessage is unpacked, extracted, and processed according to our business logic.
                // Upon unpacking from ISO to data, it is then we can proceed for executing our business logics,
                // Hence, dig out & play around with business logic in your DB apps;
                String result = Router.startRouter(messageText);
                //String result = "The Peak Message isThe Recently Published One"; -->For Testing only
                
                //System.out.println(result);
                
                // CHECK HERE; DO WE NEED TO SEND BACK?
                // If result !=  empty, there is message to send back as response upon receiving message
                // String result is the ISOMessage that needs to be sent as The Body of message
                // The ISOMessage produced from Router() will be enveloped by within TextMessage response.
                if (!result.equals("")) {	
	                TextMessage response = this.session.createTextMessage(result);
	                //ENVELOPING Process:
	                //1. Connection
	                if (this.session == null) {
	                	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);   
	                }
	                //2. Correlation ID
	                //Set the correlation ID from the received message to be the correlation id of the response message
	                //this lets the client identify which message this is a response to if it has more than
	                //one outstanding message to the server
	                response.setJMSCorrelationID(message.getJMSCorrelationID());
	                
	                //3. Setup The Producer/Publisher
	                //Setup a message producer to respond to messages from clients, we will get the destination
	                //to send to from the JMSReplyTo header field from a Message
	                Destination responseQueue = this.session.createQueue(this.messageQueueProducer);
	    			this.replyProducer = this.session.createProducer(responseQueue);
	                this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	                
	                //Send the response to the Destination              
	    	        this.replyProducer.send(response);
	    	        LogLoader.setInfo(MQServer.class.getSimpleName(), "Sending verification message: success. ");
                } else {
                	LogLoader.setInfo(MQServer.class.getSimpleName(), "There is incoming message, but no response needed");
                }
            }            
        } catch (JMSException e) {
            //Handle the exception appropriately
        	LogLoader.setError(MQServer.class.getSimpleName(), e);
        } /*finally {
        	try {
	        	if (this.replyProducer != null)
	        		this.replyProducer.close();
	        	if (this.session != null)
	        		this.session.close();
        	} catch (JMSException jmse) {
        		LogLoader.setError(MQServer.class.getSimpleName(), jmse);
        	}        	
        }*/
    }
}
