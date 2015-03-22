/**
 * Autodebet
 *
 * Class untuk memproses Autodebet
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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import id.bri.switching.helper.LogLoader;
import id.bri.switching.helper.TextUtil;
//import id.bri.switching.helper.TraceNumberGenerator;


public class Autodebet {
    
    /* 
     * Property
     * ---------------------------------------------------------------------
     */
    
    protected String onlineStatus = "00";
    protected String responseCode;
    protected String responseData;
    protected String responseDescription;
    //protected ResponseCode rc = new ResponseCode();
    protected Timestamp trxTime;
    
    //private final String BIT41 = "00011251";
    protected String accountNumber;
	protected String paymentType;
	protected String paymentNumber = "";
	protected int paymentAmount;
	protected String paymentCurrency; 
	protected String paymentObject;
	protected String cardNumber;
    
	protected ISOMsg isoMsg;
	
    /**
     * Payment
     * ------------------------------------------------------------------------
     * 
     * Fungsi constructor
     * 
     * @access      public
     * @param       String, String, String, String
     * @return      String
     */
    
    public Autodebet(String paymenttype) {
        
        //  Inisialisasi
    	this.paymentType = paymenttype.trim();
    	this.paymentCurrency = "IDR";
    	//this.cardNumber = "5188280130227805";
        this.cardNumber = "";    	
        trxTime = new Timestamp(System.currentTimeMillis());
    }
    
    // ---------------------------------------------------------------------------------
    
    /**
     * getResponseCode
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk mengambil nilai kode response
     * 
     * @access      public
     * @return      String
     */
    
    public String getResposeCode(){
        return responseCode;
    }
    
    // ---------------------------------------------------------------------------------
    
    /**
     * getResponseDescription
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk mengambil deskripsi dari kode respon
     * 
     * @access      public
     * @return      String
     */
    
    /*public String getResposeDescription(){
        if(responseCode.equals("00")){
            return responseData;
        }
        return rc.getResponseDescription(responseCode);
    }*/
    
    /**
     * getIsoMsg
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk mengambil Iso Message
     * 
     * @access      public
     * @return      String
     */
    
    public ISOMsg getIsoMsg(){
        return isoMsg;
    }
    
    /**
     * setIsoMsg
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk mengeset Iso Message
     * 
     * @access      public
     * @return      String
     */
    
    public void setIsoMsg(ISOMsg isomsg){
    	isoMsg = isomsg;
    }
    
    // ---------------------------------------------------------------------------------
    /**
     * PaymentTrx
     * dicopy dari ibBillPaymentTrx(), modul pembayaran pada IB
     * Membentuk Iso message yang akan dikirim ke proswitching
     * ------------------------------------------------------------------------
     * 
     * Fungsi 
     * 
     * @access      public
     * @return      String
     */
    
    public String billPaymentTrx(String acctNum, String payNum, int payAmt, String cardNum) {
		
    	String result = "";
    	String b37 = "";	// bit 37 diisi payment number; generate dari CSAP
		try{
			if(acctNum.trim().length() == 0 || payNum.trim().length() == 0 || cardNum.trim().length() == 0){
				LogLoader.setError(Autodebet.class.getSimpleName(), "Parameters autodebet not complete");
				return TextUtil.formattedResult("NC", payNum, "PARAMETERS NOT COMPLETE");
			}
			//Long.parseLong(acctNum);
			//Long.parseLong(cardNum);
			//Long.valueOf(payNum);
			b37 = String.format("%012d", Long.valueOf(payNum));
		}catch(NumberFormatException e){
			LogLoader.setError(Autodebet.class.getSimpleName(), "Parameters in autodebet not in correct format");
			return TextUtil.formattedResult("ER", payNum, e.getMessage());
		}catch (Exception e) {
			LogLoader.setError(Autodebet.class.getSimpleName(), "Parameters autodebet error");
			return TextUtil.formattedResult("ER", payNum, e.getMessage());
		}
		
		/*if(Long.valueOf(payAmt) <=0 ){
			return TextUtil.formattedResult("IA", payNum, "ILLEGAL AMOUNT FOUND");
		}*/
		
		String b2 = "";
		String b3 = "";
		String b4 = "";
		String b22 = "111";
		String b32 = "001";
		String b41 = "00011251";
		String b48 = "";
		String b49 = "";
		String b57 = "";
		String b60 = "ON2       002000";
		String b63 = "";
		//String b1 = "0000000004010000";
		
		
		if(paymentType.startsWith("CC")){
			b3 = "171000";
			//b4 = Integer.toString(paymentAmount);
			b4 = Integer.toString(payAmt);
			//b48 = cardNumber;
			b48 = cardNum;
			b63 = "204IBNKBRICC        ";
			b2 = "AUTODEBET";
		} else if (paymentType.startsWith("CORP")) {
			b3 = "171000";
			b4 = Integer.toString(payAmt);
			b48 = cardNum+"1";	// 9 Sept 2014: 1 untuk autodebet corp
			b63 = "204IBNKBRICORP      ";
			b2 = String.format("%015d", Long.parseLong(acctNum));
			b41 = "00011237";
		} else if (paymentType.startsWith("SALDO")) {
			b3 = "330000";
			b4 = Integer.toString(0);
			//b48 = cardNumber;
			//b48 = cardNum;
			b63 = "204IBNKSLD          ";
			b2 = "AUTODEBETINQ";
			b22 = "021";
			b32 = "001";
			b57 = "@KIOSBRI#";			
		} else { //UNKNOWN FORMAT
			return TextUtil.formattedResult("UF",payNum, "UNKNOWN FORMAT MESSAGE");
		}
			
		if (paymentCurrency.equalsIgnoreCase("IDR"))
			b49 = "360";
		else
			b49 = "360";
		try {
			Date dateNow = new Date();
			//int traceNumber = TraceNumberGenerator.getSystemTraceNumber();
			SimpleDateFormat date10 = new SimpleDateFormat("MMddHHmmss");
			SimpleDateFormat date6 = new SimpleDateFormat("HHmmss");
			SimpleDateFormat date4 = new SimpleDateFormat("MMdd");
			
			//ISO87PSWPackager packager = new ISO87PSWPackager();
			ISO8583PSWPackager packager = new ISO8583PSWPackager();
			//ISOMsg isoMsg = new ISOMsg();
			isoMsg = new ISOMsg();
			isoMsg.setPackager(packager);
			isoMsg.setMTI("0200");
			isoMsg.set(2, b2);
			isoMsg.set(3, b3);
			isoMsg.set(4, String.valueOf(Long.parseLong(b4)));
			isoMsg.set(7, date10.format(dateNow));
			//isoMsg.set(11, String.valueOf(traceNumber));
			isoMsg.set(11, payNum);
			isoMsg.set(12, date6.format(dateNow));
			isoMsg.set(13, date4.format(dateNow));
			isoMsg.set(15, date4.format(dateNow));
			isoMsg.set(22, b22);
			isoMsg.set(32, b32);
			if (paymentType.startsWith("SALDO")) {
				isoMsg.set(35, "CEKSALDO");
			}
			isoMsg.set(37, b37);
			isoMsg.set(41, b41);
			isoMsg.set(43, "");
			isoMsg.set(48, b48);
			if (!paymentType.startsWith("SALDO")) {
				isoMsg.set(49, b49);
			}
			isoMsg.set(60, b60);
			isoMsg.set(63, b63);
			//isoMsg.set(102, String.format("%015d", Long.parseLong(accountNumber)));
			isoMsg.set(102, String.format("%015d", Long.parseLong(acctNum)));
			//isoMsg.set(112, encPassword);

			//String correlationId = String.format("%06d", traceNumber);
			
			/*
			if(aSync){
				MQConnectionFactory mqcf = new MQConnectionFactory();
				result = mqcf.sendISOTrxASync(new String(isoMsg.pack()), correlationId);
				if(result.startsWith("[OK]")){
					return TextUtil.formattedResult("OK", sB37, "");
				}else if(result.startsWith("[FAILED]")){
					return TextUtil.formattedResult("ER", sB37, result);
				}else{
					return TextUtil.formattedResult("UN", sB37, "UNDEFINED ERROR");
				}
			}
			
			MQConnectionFactory mqcf = new MQConnectionFactory();
			result = mqcf.sendISOTrx(new String(isoMsg.pack()), correlationId);
			if (result.startsWith("TIMEOUT") || result == null) {
				result = TextUtil.formattedResult("Q4", sB37, "TIMEOUT");
			} else if(result.startsWith("[FAILED]")){
				result = TextUtil.formattedResult("ER", sB37, result.substring("[FAILED]".length()));
			} else {
				isoMsg.unpack(result.getBytes());
				result = TextUtil.formattedResult(isoMsg.getString(39), isoMsg.getString(37), isoMsg.getString(48));
			}*/
		} catch (ISOException e) {
			result = TextUtil.formattedResult("ER", payNum, e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			result = TextUtil.formattedResult("ER", payNum, e.getMessage());
			e.printStackTrace();
		}

		return result;
	}
    
 // ---------------------------------------------------------------------------------
    /**
     * InquirySaldo
     * dicopy dari ibBillPaymentTrx(), modul pembayaran pada IB
     * Membentuk Iso message yang akan dikirim ke proswitching
     * ------------------------------------------------------------------------
     * 
     * Fungsi 
     * 
     * @access      public
     * @return      String
     */
    
    public String inquirySaldo(String acctNum, String payNum) {
		
    	String result = "";
    	String b37 = "";	// bit 37 diisi payment number; generate dari CSAP
		try{
			if(acctNum.trim().length() == 0 || payNum.trim().length() == 0){
				LogLoader.setError(Autodebet.class.getSimpleName(), "Parameters autodebet not complete");
				return TextUtil.formattedResult("NC", payNum, "PARAMETERS NOT COMPLETE");
			}
			//Long.parseLong(acctNum);
			//Long.parseLong(cardNum);
			//Long.valueOf(payNum);
			b37 = String.format("%012d", Long.valueOf(payNum));
		}catch(NumberFormatException e){
			LogLoader.setError(Autodebet.class.getSimpleName(), "Parameters in autodebet not in correct format");
			return TextUtil.formattedResult("ER", payNum, e.getMessage());
		}catch (Exception e) {
			LogLoader.setError(Autodebet.class.getSimpleName(), "Parameters autodebet error");
			return TextUtil.formattedResult("ER", payNum, e.getMessage());
		}
		
		String b2 = "";
		String b3 = "";
		String b4 = "";
		String b41 = "00011251";
		String b48 = "";
		String b49 = "";
		String b57 = "@KIOSBRI#";
		String b60 = "ON2       002000";
		String b63 = "";
		//String b1 = "0000000004010000";
		
		
		if (paymentType.startsWith("SALDO")) {
			b3 = "330000";
			b4 = Integer.toString(0);
			//b48 = cardNumber;
			//b48 = cardNum;
			b63 = "204IBNKSLD          ";
			b2 = "AUTODEBETINQ";
		}		
		else{ //UNKNOWN FORMAT
			return TextUtil.formattedResult("UF",payNum, "UNKNOWN FORMAT MESSAGE");
		}
			
		/*if (paymentCurrency.equalsIgnoreCase("IDR"))
			b49 = "360";
		else
			b49 = "360";*/
		try {
			Date dateNow = new Date();
			SimpleDateFormat date10 = new SimpleDateFormat("MMddHHmmss");
			SimpleDateFormat date6 = new SimpleDateFormat("HHmmss");
			SimpleDateFormat date4 = new SimpleDateFormat("MMdd");
			
			ISO8583PSWPackager packager = new ISO8583PSWPackager();
			isoMsg = new ISOMsg();
			isoMsg.setPackager(packager);
			isoMsg.setMTI("0200");
			isoMsg.set(2, b2);
			isoMsg.set(3, b3);
			isoMsg.set(4, String.valueOf(Long.parseLong(b4)));
			isoMsg.set(7, date10.format(dateNow));
			//isoMsg.set(11, String.valueOf(traceNumber));
			isoMsg.set(11, payNum);
			isoMsg.set(12, date6.format(dateNow));
			isoMsg.set(13, date4.format(dateNow));
			isoMsg.set(15, date4.format(dateNow));
			//isoMsg.set(22, "111");
			isoMsg.set(22, "021");
			isoMsg.set(32, "001");
			//isoMsg.set(35, username);
			isoMsg.set(37, b37);
			isoMsg.set(41, b41);
			isoMsg.set(43, "");
			isoMsg.set(48, b48);
			//isoMsg.set(49, b49);
			isoMsg.set(60, b60);
			isoMsg.set(63, b63);
			//isoMsg.set(102, String.format("%015d", Long.parseLong(accountNumber)));
			isoMsg.set(102, String.format("%015d", Long.parseLong(acctNum)));
		} catch (ISOException e) {
			result = TextUtil.formattedResult("ER", payNum, e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			result = TextUtil.formattedResult("ER", payNum, e.getMessage());
			e.printStackTrace();
		}

		return result;
	}
}

