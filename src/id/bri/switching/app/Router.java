/**
 * Router
 *
 * Class yang berfungsi untuk memvalidasi tipe message dan mengarahkan kepada
 * class sub proses transaksi, seperti inquiry atau payment
 *
 * @package		id.bri.switching.app
 * @author		PSD Team
 * @copyright           Copyright (c) 2013, PT. Bank Rakyat Indonesia (Persero) Tbk,
 */

// ---------------------------------------------------------------------------------

/*
 * ------------------------------------------------------
 *  Memuat package dan library
 * ------------------------------------------------------
 */

package id.bri.switching.app;

import id.bri.switching.helper.ISO8583PSWPackager;
import id.bri.switching.helper.LogLoader;
import id.bri.switching.helper.ResponseCode;
import id.bri.switching.helper.TextUtil;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

//  Class Router
public class Router {
    
    /* 
     * Property
     * ---------------------------------------------------------------------
     */
    protected static ResponseCode rc = new ResponseCode();
    
    /**
     * startRouter
     * ------------------------------------------------------------------------
     * 
     * Function to start the router.
     * 
     * @access      public
     * @param       String
     * @return      String
     */
    
    public static synchronized String startRouter(String requestString) {
        //  Menampilkan pesan masuk
        LogLoader.setInfo(Router.class.getSimpleName(), "Msg to be verified : " + requestString);
        String response = "";
        //  try catch ISOMsg
        try {
            //  ISO 8583 Proswitching Packager & ISOMsg
            ISO8583PSWPackager packager = new ISO8583PSWPackager();
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.unpack(requestString.getBytes());
            
            //  Hanya melayani MTI 200 & 800
            //  MTI 200 request, 800 
            if(isoMsg.getMTI().equals("0200")){
            	LogLoader.setInfo(Router.class.getSimpleName(), "Verifying the message...");
            	// Business logic; bit 3 defines inquiry VS transaction
            	// Verify the dictionary to Mas Deni
            	// ----------------------------------------------------
        		// bit 3: Proc. code; bit 48: Card Number
            	// bit x: Status Code, bit y: Flag Card
        		// bit 63: TrxAmtTotal & PointValue (point * 100)
            	String cardNum = isoMsg.getString(2).trim();
            	String pointAmt = isoMsg.getString(4).trim();
            	
            	//if( isoMsg.getString(3).trim().equals("101010") && 
            	//    isoMsg.getString(63).trim().equals("POINT")  )
            	if(isoMsg.getString(3).trim().equals("101010")) {
            		// TRANSACTION
            		String tblName = "lbcrdext";
            		// Relay to PSW 
            		// Redeem & Update point balance within a purchase
            		PointRedeem pointRedeem = new PointRedeem();
            		Map<String, String> resDeb = pointRedeem.debetPoint(cardNum, tblName, pointAmt);
            		
            		if(!resDeb.isEmpty()){
            			//Set ISOMsg back with Card's Info - Obtain them via Map resDeb Object
            			isoMsg.set(2, (String) resDeb.get("cardNum"));
            			isoMsg.set(4, (String) resDeb.get("cardPoint"));
                        
                        resDeb.clear();
            		}else{
            			//Update is failed.
            		}
            	}
            	else if(isoMsg.getString(3).trim().equals("303030")) {
            		// INQUIRY
            		String tblName = "lbccpcrd";
            		// Read: DB
            		// Find: (1) Status/Eligibility of Card, (2) Point balance
            		Inquiry inq = new Inquiry();
            		Map<String, String> resInquiry = inq.inquiryPointCard(cardNum, tblName);
            		
            		if(!resInquiry.isEmpty()){
            			//Get the current point when cardStatus == OK
            			PointRedeem pointRedeem = new PointRedeem();
            			int pointOfCard = pointRedeem.inquiryPoint(cardNum, "lbcrdext");
            			
            			//Set ISOMsg back with Card's Status - Obtain them via Map resInquiry Object & pointOfCard
            			isoMsg.set(38, String.valueOf(pointOfCard));
            			isoMsg.set(2, (String) resInquiry.get("cardNum"));
            			isoMsg.set(4, (String) resInquiry.get("cardStatus"));
                        resInquiry.clear();
            		}else{
            			//Do something if there's no point available/Card is inactive/Card is not found
            		}
                	
            	}
            	
            	isoMsg.setResponseMTI();
                response = new String(isoMsg.pack());

            }
            else if (isoMsg.getMTI().equals("0810")){
            	// Response dari MQ == 0810
            	LogLoader.setInfo(Router.class.getSimpleName(), "Test connection response received. MTI: " + isoMsg.getMTI());            	
            }
            
            LogLoader.setInfo(Router.class.getSimpleName(), "Response msg that's sent: " + response);
        } catch (SQLException e) {
        	e.printStackTrace();
        } catch (ISOException e) {
        	LogLoader.setError(Router.class.getSimpleName(), e);
        }
        
        return response;
    }
    
    
    /**
     * listenerRouter
     * ------------------------------------------------------------------------
     * 
     * Fungsi memulai listener Router. Routing response transaksi
     * 
     * @access      public
     * @param       String
     * @return      String
     */
    
    public synchronized String listenerRouter(String requestString){
        //  Menampilkan pesan masuk
    	// LogLoader.setInfo(Router.class.getSimpleName(), "Msg received: " + requestString);
        String response = "";
        //  try catch ISOMsg
        try {
            //  ISO 8583 Proswitching Packager & ISOMsg
            ISO8583PSWPackager packager = new ISO8583PSWPackager();
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.unpack(requestString.getBytes());
            
            //  Hanya melayani MTI 210 & 800
            if(isoMsg.getMTI().equals("0200")) {
                
            }
            else if(isoMsg.getMTI().equals("0210")){ 
            	return TextUtil.formattedResultWithSaldo(isoMsg.getString(39), isoMsg.getString(37), isoMsg.getString(54), new String(isoMsg.pack()));         	
            }
            else if(isoMsg.getMTI().equals("0810")){
            	// Response dari MQ
            	LogLoader.setInfo(Router.class.getSimpleName(), "Test connection response received. MTI: " + isoMsg.getMTI());
            }
            else if(isoMsg.getMTI().equals("0800")){
                isoMsg.set(39, "00");
            }
            else {
                isoMsg.set(39, "79");
                isoMsg.set(48, rc.getResponseDescription("79"));
                System.out.println("Transaction rejected. MTI : " + isoMsg.getMTI());
            }
            
            //isoMsg.setResponseMTI();
            response = new String(isoMsg.pack());
            
        } catch (ISOException e) {
        	response = TextUtil.formattedResult("ER", "-1", e.getMessage());
        	LogLoader.setError(Router.class.getSimpleName(), e);
        }
        
        return response;
    }
    
}
