import id.bri.switching.helper.ISO8583PSWPackager;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOException;

import id.bri.switching.app.*;
import id.bri.switching.helper.*;
import id.bri.switching.mq.*;


public class ExperimentalPSW2 {

	public static void main(String[] args) {
		try{
			ISO8583PSWPackager packager = new ISO8583PSWPackager();
			ISOMsg isoMsg = new ISOMsg();
	        isoMsg.setPackager(packager);
	        String requestString = "0800822000000000000004000000000000000224200936083503002";
	        isoMsg.unpack(requestString.getBytes());
	        
			// Print the unpacked ISO8583
	        System.out.println("MTI='"+isoMsg.getMTI()+"'");
	        for(int i=1; i<=isoMsg.getMaxField(); i++){
	            if(isoMsg.hasField(i))
	                System.out.println(i+"='"+isoMsg.getString(i)+"'");
	        	}
        
			}catch (ISOException e) {
	            e.printStackTrace();
	        }

	}

}
