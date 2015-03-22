/**
 * MsSqlConnect
 *
 * Class untuk koneksi ke database switching. Menggunakan Microsoft Sql
 *
 * @package		id.bri.switching.helper
 * @author		PSD Team
 * @copyright           Copyright (c) 2013, PT. Bank Rakyat Indonesia (Persero) Tbk,
 */

// ---------------------------------------------------------------------------------

/*
 * ------------------------------------------------------
 *  Memuat package dan library
 * ------------------------------------------------------
 */

package id.bri.switching.helper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//import com.microsoft.sqlserver.jdbc.*;

//  Class MsSqlConnect
public class MsSqlConnect {
    
    /* 
     * Property
     * ---------------------------------------------------------------------
     */
    
    protected Connection connection;
    
    /**
     * MsSqlConnect
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk konek ke database
     * 
     * @access      public
     * @return      String
     */
    
    public MsSqlConnect(String dbName) throws SQLException, ClassNotFoundException {
    	try {	    	
	    	createConnection(dbName);
	        //LogLoader.setInfo(MsSqlConnect.class.getSimpleName(), "Connection to MSSQL established");

    	} catch (ClassNotFoundException e) {
    		e.printStackTrace();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public void createConnection(String dbName) throws SQLException, ClassNotFoundException {
    	if (dbName.equals("LOSBRICC")) {
	    	String db_user = PropertiesLoader.getProperty("DB_USERNAME");
	        String db_pass = PropertiesLoader.getProperty("DB_PASSWORD");
	    	//String url = "jdbc:sqlserver://"+PropertiesLoader.getProperty("DB_URL")+":"+PropertiesLoader.getProperty("DB_PORT")+";databaseName="+dbName;
	    	//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	    	//connection = DriverManager.getConnection(url, db_user, db_pass);
	        
	        Class.forName("net.sourceforge.jtds.jdbc.Driver"); //specify the jtds driver
	        String url = "jdbc:jtds:sqlserver://"+PropertiesLoader.getProperty("DB_URL")+":"+PropertiesLoader.getProperty("DB_PORT")+";databaseName="+dbName;
	        connection = DriverManager.getConnection(url, db_user, db_pass);
    	} else if (dbName.equals("1700_CC")) {
    		String db_user = PropertiesLoader.getProperty("CL_USERNAME");
	        String db_pass = PropertiesLoader.getProperty("CL_PASSWORD");
	    	//String url = "jdbc:sqlserver://"+PropertiesLoader.getProperty("DB_URL")+":"+PropertiesLoader.getProperty("DB_PORT")+";databaseName="+dbName;
	    	//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	    	//connection = DriverManager.getConnection(url, db_user, db_pass);
	        
	        Class.forName("net.sourceforge.jtds.jdbc.Driver"); //specify the jtds driver
	        String url = "jdbc:jtds:sqlserver://"+PropertiesLoader.getProperty("CL_URL")+":"+PropertiesLoader.getProperty("CL_PORT")+";databaseName="+dbName;
	        connection = DriverManager.getConnection(url, db_user, db_pass);
    	}
    }
    
    // ---------------------------------------------------------------------------------
    
    /**
     * getConnection
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk mengambil object connection yang aktif
     * 
     * @access      public
     * @return      Connection
     */
    
    public Connection getConnection(){
        return connection;
    }
    
    // ---------------------------------------------------------------------------------
    
    /**
     * closeConnection
     * ------------------------------------------------------------------------
     * 
     * Fungsi untuk menutup koneksi ke database yang masih aktif
     * 
     * @access      public
     * @return      void
     */
    
    public void closeConnection() throws SQLException{
        getConnection().close();
    }
    
    // ---------------------------------------------------------------------------------
                
}
