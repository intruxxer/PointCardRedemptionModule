/**
 * MQClient
 *
 * Class yang berfungsi untuk mengirimkan pesan ke prosw melalui MQ 
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

import org.apache.activemq.ActiveMQConnectionFactory;

import id.bri.switching.helper.LogLoader;

import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import java.io.IOException;
import java.util.Random;
 
public class MQClient implements MessageListener {
    
    private MessageProducer producer;
    private Connection connection;
    private Session session;
    
    public void openConnection(String mqUrl) throws JMSException {
        try {        	
	    	//  Koneksi ke activemq
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
 
    public void setupMessageProducer(String clientQueueName, String message) {
        try {
        	this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);  
            Destination adminQueue = session.createQueue(clientQueueName);
 
            //Setup a message producer to send message to the queue the server is consuming from
            this.producer = session.createProducer(adminQueue);
            this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 
            //Create a temporary queue that this client will listen for responses on then create a consumer
            //that consumes message from this temporary queue...for a real application a client should reuse
            //the same temp queue for each message to the server...one temp queue per client
            Destination tempDest = session.createTemporaryQueue();
            MessageConsumer responseConsumer = session.createConsumer(tempDest);
 
            //This class will handle the messages to the temp queue as well
            responseConsumer.setMessageListener(this);
 
            //Now create the actual message you want to send
            TextMessage txtMessage = session.createTextMessage();
            txtMessage.setText(message);
 
            //Set the reply to field to the temp queue you created above, this is the queue the server
            //will respond to
            txtMessage.setJMSReplyTo(tempDest);
 
            //Set a correlation ID so when you get a response you know which sent message the response is for
            //If there is never more than one outstanding message to the server then the
            //same correlation ID can be used for all the messages...if there is more than one outstanding
            //message to the server you would presumably want to associate the correlation ID with this
            //message somehow...a Map works good
            String correlationId = this.createRandomString();
            txtMessage.setJMSCorrelationID(correlationId);
            this.producer.send(txtMessage);
        } catch (JMSException e) {
            //Handle the exception appropriately
        	LogLoader.setError(MQClient.class.getSimpleName(), e);
        }
    }
 
    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }
 
    public void onMessage(Message message) {
        String messageText = null;
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                messageText = textMessage.getText();
                LogLoader.setInfo(MQClient.class.getSimpleName(), "messageText = " + messageText);
            }
        } catch (JMSException e) {
            //Handle the exception appropriately
        	LogLoader.setError(MQClient.class.getSimpleName(), e);
        }
    }
}