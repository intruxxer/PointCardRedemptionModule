package id.bri.switching.app;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import id.bri.switching.helper.LogLoader;

@WebServlet("/ListenerServlet")
public class ListenerServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static SwitchingApp switchingapp = new SwitchingApp();
	
	public void init() throws ServletException
    {
		// call listener only once
		try {			
			switchingapp.startListener();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public void destroy()
    {
		// call stop the mq connection 
		try {
        	if (SwitchingApp.mqserver.getConnection() != null) {
        		SwitchingApp.mqserver.getConnection().close();
        	}
		} catch (Exception e) {
			LogLoader.setError("ListenerServlet", e);
		}
    }
}
