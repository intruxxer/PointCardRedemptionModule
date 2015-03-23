import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import id.bri.switching.app.*;
import id.bri.switching.helper.*;
import id.bri.switching.mq.*;

public class ExperimentalPSW {

	public static void main(String[] args) {
	//public ExperimentalPSW(){
		try{
			ISO8583PSWPackager packager = new ISO8583PSWPackager();
	        ISOMsg isoMsg = new ISOMsg();
	        isoMsg.setPackager(packager);
	        
			/* Experimental Test to Pack an ISO Message */
	        System.out.println("===Building ISO Message===");
	        
	        isoMsg.setMTI("0200");
	        isoMsg.set(2, "0004259678945688902");
			isoMsg.set(3, "101010");
			byte[] result = isoMsg.pack();
			
			// Produce output ISO 8583 Message String
	        String isoMessage = "";
	        for (int i = 0; i < result.length; i++) {
	            isoMessage += (char) result[i];
	        }
            System.out.println(String.valueOf(result)); 
			System.out.println(isoMessage);
			System.out.println("===Done with ISO Message===");
						
			/* Experimental Test to Unpack an ISO Message */
			System.out.println("");
			System.out.println("===Unpacking ISO Message===");
			// byte[] version of isoMessage is "[B@70177ecd" via isoMessage.getBytes()
	        
            isoMsg.unpack(isoMessage.getBytes());
            // Print the unpacked ISO8583
	        System.out.println("MTI='"+isoMsg.getMTI()+"'");
	        for(int i=1; i<=isoMsg.getMaxField(); i++){
	            if(isoMsg.hasField(i))
	                System.out.println(i+"='"+isoMsg.getString(i)+"'");
	        	}
            /*
            System.out.print("MTI->");
            for (int i = 0; i < result.length; i++) {
            	if(i<4){
            		System.out.print((char) result[i]);
            	}
            	if(i==4){
            		System.out.println("");
            		System.out.print("BITMAP->");
            		System.out.print((char) result[i]);
            	}
            	if(i>4 && i<22){
            		System.out.print((char) result[i]);
            	}
            	if(i==22){
            		System.out.println("");
            		System.out.print("CARD NUMBER->");
            		System.out.print((char) result[i]);
            	}
            	if(i>21 && i<41){
            		System.out.print((char) result[i]);
            	}
            	if(i==41){
            		System.out.println("");
            		System.out.print("PROC CODE->");
            		System.out.print((char) result[i]);
            	}
            	if(i>41 && i<47){
            		System.out.print((char) result[i]);
            	}
	        }
            System.out.println("");
            System.out.println("===Done Unpacking ISOMsg===");
            //System.out.println(isoMsg);
            */
		}catch (ISOException e) {
            e.printStackTrace();
        }
	}

}
