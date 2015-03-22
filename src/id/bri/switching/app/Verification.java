package id.bri.switching.app;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import id.bri.switching.helper.LogLoader;
import id.bri.switching.helper.MsSqlConnect;
import id.bri.switching.helper.PropertiesLoader;
import id.bri.switching.helper.TextUtil;

public class Verification {
	
	/**
	 * Variables
	 */
	String paymentCurrency;
	String cardNumber;
	String suppName;
	String bornDateStr;
	Timestamp trxTime;
	
	/**
     * Payment
     * ------------------------------------------------------------------------
     * 
     * Fungsi constructor
     * 
     * @access      public
     */
    
    public Verification (String cardNum, String borndate) throws ParseException {
        
        //  Inisialisasi
    	this.paymentCurrency = "IDR";
    	if (cardNum.length() == 16 && TextUtil.isAllDigit(cardNum, false)) {
    		this.cardNumber = cardNum;
    	} else {
    		throw new ParseException("Card number is not valid format", 0);
    	}    	
        this.bornDateStr = borndate.substring(0, 8);
    	trxTime = new Timestamp(System.currentTimeMillis());
    }
    
    public String doVerification() {
	        
        String sql;
        String sql2;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        String borndatedb;
        String res = "99";
        //  try catch database
        try {
            MsSqlConnect db = new MsSqlConnect(PropertiesLoader.getProperty("DB_NAME"));
            
            //  Cek payment type
        	sql = "SELECT SUP_NAME, SUP_BORNDATE, CORE_ID "
        			+ "FROM APPSUPPINFO WHERE CORE_ID = ? ;";
            ps = db.getConnection().prepareStatement(sql);
            ps.setString(1, cardNumber);
            rs = ps.executeQuery();
                
            //  Data kartu ditemukan
            if(rs.next()){
            	suppName = rs.getString(1);
            	borndatedb = new SimpleDateFormat("ddMMyyyy").format(rs.getDate(2));
            	cardNumber = rs.getString(3);
            }
            else {
            	suppName = "";
            	borndatedb = "";
            	cardNumber = "";
                //responseCode = "14";
            	// nomor kartu tidak ditemukan di database supplemen
            	// kemungkinan nomor kartu utama
            	// cari ke database Cardlink
            	MsSqlConnect db2 = new MsSqlConnect(PropertiesLoader.getProperty("CL_NAME"));
            	sql2 = "SELECT CM_CARD_NMBR, CR_DTE_BIRTH "
            			+ "FROM VW_SDG_CUSCARD WHERE CM_CARD_NMBR = ? ;";
            	ps2 = db2.getConnection().prepareStatement(sql2);
                ps2.setString(1, cardNumber);
                rs2 = ps2.executeQuery();
                    
                //  Data kartu ditemukan
                if(rs2.next()){
                	suppName = rs2.getString(1);
                	borndatedb = new SimpleDateFormat("ddMMyyyy").format(rs2.getDate(2));
                	cardNumber = rs2.getString(3);
                }
                else {
                	
                }
            }
            
            // compare the born date            
            if (!borndatedb.equals("")) {
	            if (borndatedb.equals(bornDateStr)) {
	            	res = "00"; // born date is match
	            }
	            else {
	            	res = "25"; // born date not match
                	LogLoader.setInfo(Verification.class.getSimpleName(), "ERROR respon25: BORNDATEDB = "+ borndatedb + ", BORNDATE = " + bornDateStr);
	            }
            } else { // born date is null
            	res = "53";
            	LogLoader.setInfo(Verification.class.getSimpleName(), "ERROR respon53: BORNDATEDB = "+ borndatedb + ", BORNDATE = " + bornDateStr);

            }
        }
        catch (SQLException e) {
            //responseCode = "92";
        	LogLoader.setError(Verification.class.getSimpleName(), "db except 1: "+ e.getLocalizedMessage());
        } catch (ClassNotFoundException e) {
        	e.printStackTrace();
        } catch (NullPointerException e) {
        	LogLoader.setError(Verification.class.getSimpleName(), "null pointer exception when parsing born date");
        	res = "53";
        } finally {
            try {
                if(rs != null){
                    rs.close();
                }
                if(ps != null){
                    ps.close();
                }
                if(rs2 != null){
                    rs2.close();
                }
                if(ps2 != null){
                    ps2.close();
                }
            } catch (SQLException e) {
                //responseCode = "92";
                LogLoader.setError(Verification.class.getSimpleName(), "db except 2: "+ e.getLocalizedMessage());
            }
        }
        return res;
	}   

}
