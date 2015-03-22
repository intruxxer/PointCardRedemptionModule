/**
 * RequestListener
 *
 * Class untuk mengambil request antrian transaksi yang masuk di activemq * 
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
import id.bri.switching.app.Router;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class RequestListener implements MessageListener, ExceptionListener {
	
	protected String responseMsg = "";
	
	public void onMessage(Message message) {
	//  try catch untuk menerima exception message 
	//	bila terjadi exception dari MQ server
        try {
            if(message instanceof TextMessage){
	            TextMessage textMessage = (TextMessage) message;
	            Router router = new Router();
	            setResponseMsg(router.listenerRouter(textMessage.getText()));
	            
            }
            else {
                System.out.println("Tidak ada respon dari router");
            }
            
        } catch (JMSException e) {
            
        } catch (Exception e){
            
        }
    }
    
    // ---------------------------------------------------------------------------------
    
    /**
     * onMessage
     * ------------------------------------------------------------------------
     * 
     * Fungsi override dari class ExceptionListener. Fungsi ini aktif apabila terjadi
     * exception sewaktu mengambil message dari activemq
     * 
     * @access      public
     * @param       JMSException
     * @return      void
     */
    
    @Override
    public void onException(JMSException jsme){
        
    }
    
    // ---------------------------------------------------------------------------------
    
    public String getResponseMsg() {
    	return responseMsg;
    }
    
    public void setResponseMsg(String respMsg) {
    	responseMsg = respMsg;
    }
}
