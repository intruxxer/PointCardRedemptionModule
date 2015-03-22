package id.bri.switching.app;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;

import id.bri.switching.helper.LogLoader;
import id.bri.switching.helper.MysqlConnect;
import id.bri.switching.helper.PropertiesLoader;
import id.bri.switching.helper.TextUtil;

public class PointRedeem {
	Connection con;
	MysqlConnect db;
	String response;
	Map<String, String> cardInfo;
	
	public PointRedeem(){
		db = null;
		con = null;
		response = null;
		cardInfo = new HashMap<String, String>();
	}
	
	//Table: lbcrdext
	public int inquiryPoint(String cardNum, String tblName) {
    	Statement stm = null;
    	ResultSet rs = null;
    	MysqlConnect db = null;
    	int currPointBal = 0;
		try {
			db = new MysqlConnect(PropertiesLoader.getProperty("DB_NAME"));
			con = db.getConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
    	String pointQuery = "SELECT LB_CP_PAS_CURR_BAL FROM " + tblName + " WHERE LB_CARD_NMBR = " + cardNum;
    	try {
            stm = con.createStatement();
            rs = stm.executeQuery(pointQuery);
	    } catch (SQLException e ) {
	        	response = "SQL exception Query Point : " + e.toString();
	    }
    	
    	try {
    		while (rs.next()){
    			currPointBal = rs.getInt("LB_CP_PAS_CURR_BAL");
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	
    	return currPointBal;
    }
	
    public Map debetPoint(String cardNum, String tblName, String pointAmt) throws SQLException {
    	MysqlConnect db = null;
		try {
			db = new MysqlConnect(PropertiesLoader.getProperty("DB_NAME"));
			con = db.getConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
    	int currPointBal = inquiryPoint(cardNum, tblName);

    	if ( (currPointBal > 0) && (currPointBal >= Integer.parseInt(pointAmt)) ){
    		currPointBal = currPointBal - Integer.parseInt(pointAmt);
    	}
    	
        String query = "UPDATE " + tblName  + " SET LB_CP_PAS_CURR_BAL = ? WHERE LB_CARD_NMBR = ?";
        int rows = 0;
        try {
        	PreparedStatement prepStmt = con.prepareStatement(query);
        	prepStmt.setString   (1, String.valueOf(currPointBal));
            prepStmt.setString	 (2, cardNum);
            rows = prepStmt.executeUpdate();
        } catch (SQLException e ) {
        	response = "Error SQL exception Update Point : " + e.toString();
        	e.printStackTrace();
    	}
        
        if(rows > 0){
        	String msgResponse = "UPDATE " + tblName + " Success in" + rows +" rows.";
        	System.out.println("Point Redeem updates? " + msgResponse);
        	cardInfo.put("cardNum", cardNum);
        	cardInfo.put("cardPoint", String.valueOf(currPointBal));
        	return cardInfo;
        }else{
        	String msgResponse = "UPDATE " + tblName + " Failed.";
        	System.out.println("Point Redeem updates? " + msgResponse);
        	return cardInfo;
        }
        
	}
    

}
