import java.io.*;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import id.bri.switching.app.*;
import id.bri.switching.helper.*;
import id.bri.switching.mq.*;

public class ExperimentalPSWsnippet {

	public static void main(String[] args) {
	    String Str = new String("Tutorials" );
	    System.out.print("String Length :" );
	    System.out.println(Str.length());
	    System.out.print("String Length 0,4 :" );
	    System.out.println(Str.substring(0, Str.length()-2));
	}

}
