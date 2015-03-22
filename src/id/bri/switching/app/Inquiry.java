package id.bri.switching.app;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import id.bri.switching.helper.LogLoader;
import id.bri.switching.helper.MsSqlConnect;
import id.bri.switching.helper.MysqlConnect;
import id.bri.switching.helper.PropertiesLoader;
import id.bri.switching.helper.TextUtil;

public class Inquiry {
	
	String cardNum;
	String limitCard;
	String flagCard;
	String blockCode;
	String statusCard;
	String pointBalance;
	String trxAmount;
	String response;
	Connection con;
	
	public Inquiry(){
		pointBalance = "0";
		response = "";
	}
	
	//Table: lbccpcrd
    public boolean inquiryPointCard(String cardNum, String tblName, String blockCode, String statusCode, String flag) throws SQLException {
    	
    	ResultSet rs = null;
    	Statement stm = null;
    	MysqlConnect db = null;
		try {
			db = new MysqlConnect(PropertiesLoader.getProperty("DB_NAME"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        con = db.getConnection();
        
    	String authQuery = "SELECT * FROM " + tblName 
    			      + " WHERE CP_CARDNMBR = " + cardNum 
    			      + " AND CP_POSTING_FLAG = PP AND CM_STATUS IN (1, 2) AND CP_BLOCK_CODE IN (V, P, Q, ' ')";
    	try {
            stm = con.createStatement();
            rs = stm.executeQuery(authQuery);
	    } catch (SQLException e ) {
	        	response = "SQL exception : " + e.toString();
	    } finally {
	            if (stm != null) { stm.close(); }
	        }
    	
    	if (rs.next()) {
            return true;
         }
         else {
        	 return false; 
         }
    	
	}
}
