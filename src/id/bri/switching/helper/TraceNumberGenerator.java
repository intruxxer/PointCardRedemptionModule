package id.bri.switching.helper;

import java.util.Random;

public class TraceNumberGenerator {
	private static int TraceNumber = 0;
	private static long rrn = 0;
    
    public static synchronized int getSystemTraceNumber() {
       TraceNumber++;        
       if (TraceNumber > 999998)
           TraceNumber = 1;      
       
       return TraceNumber;
    }
    
    public static synchronized long getRRN() {
    	if (rrn == 0) {
    		rrn = retrieveRRNFromDB();
	    	if(rrn <= 0){
	    		rrn = retrieveRRNFromDB();
	    	}
    	}
    	return rrn;
    }
    
    private static long retrieveRRNFromDB(){
    	/*Connection connection1 = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try{
			Context initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:/comp/env");
			DataSource ds = (DataSource)envContext.lookup("jdbc/IbankDB");
			connection1 = ds.getConnection();
            String sql = "SELECT * FROM ibank.tbl_rrn WHERE id = 1;";
            ps = connection1.prepareStatement(sql);
            rs = ps.executeQuery();
            long rrn = -5;
            if(rs.next()){
                rrn = rs.getLong("rrn");
            }
            rs.close();
            rs = null;
            if(rrn >= Long.parseLong("999999999999"))
                rrn = 0;                
            sql = "UPDATE ibank.tbl_rrn SET rrn = "+(rrn + 1)+" WHERE id = 1;";
            ps = connection1.prepareStatement(sql);
            ps.executeUpdate();
            ps.close();
            ps = null;
            connection1.close();
            connection1 = null;
            return rrn + 1;
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }finally{
        	if(rs != null){
        		try { rs.close(); } catch (SQLException e) { ; }
        		rs = null;
        	}
        	if(ps != null){
        		try { ps.close(); } catch (SQLException e) { ; }
        		ps = null;
        	}
        	if(connection1 != null){
        		try { connection1.close(); } catch (SQLException e) { ; }
        		connection1 = null;
        	}
        }*/
    	//long res = (long)Math.random() * 1000 + 1;
    	//long res = 37000436;
    	Random randomGenerator = new Random();
    	long randomInt = (long)randomGenerator.nextInt(10000000);
    	return randomInt;
    }
    
}
