package id.bri.switching.app;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

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
	Map<String, String> cardInfo;
	
	public Inquiry(){
		pointBalance = "0";
		response = "";
		cardInfo = new HashMap<String, String>();
	}
	
	//Table: lbccpcrd
    public Map<String, String> inquiryPointCard(String cardNum, String tblName) throws SQLException {
    	
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
    		cardInfo.put("cardNum", cardNum);
        	cardInfo.put("cardStatus", "OK");
            return cardInfo;
         } else{
        	 return cardInfo;
         }
         
    	
	}
}
