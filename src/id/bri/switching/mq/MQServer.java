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
	    	//Koneksi ke activemq
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
			this.messageQueueProducer = messageQueueResponse;
			MessageConsumer consumer = this.session.createConsumer(requestQueue);
	        consumer.setMessageListener(this);
	        LogLoader.setInfo(MQServer.class.getSimpleName(), "Listener on");
						
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
                
                // Process the request from PWS.
                // Upon unpacking from ISO to plain, primitive data type,
                // digs out & play around with business logic in your DB apps;
                // Which Router.startRouter()s to call?
                String result = Router.startRouter(messageText);
                                
                if (!result.equals("")) {	// result not null, there is message to be send
	                TextMessage response = this.session.createTextMessage(result);
	                //Set the correlation ID from the received message to be the correlation id of the response message
	                //this lets the client identify which message this is a response to if it has more than
	                //one outstanding message to the server
	                response.setJMSCorrelationID(message.getJMSCorrelationID());
	                
	                if (this.session == null) {
	                	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);   
	                }
	                
	                //Setup a message producer to respond to messages from clients, we will get the destination
	                //to send to from the JMSReplyTo header field from a Message
	                Destination responseQueue = this.session.createQueue(this.messageQueueProducer);
	    			this.replyProducer = this.session.createProducer(responseQueue);
	                this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	                
	                //Send the response to the Destination              
	    	        this.replyProducer.send(response);
	    	        LogLoader.setInfo(MQServer.class.getSimpleName(), "Sending verification message success");
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
