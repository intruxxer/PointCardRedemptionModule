package id.bri.switching.helper;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TextUtil {
        
    public static boolean isSecureString(String str, int multiCharCount){
        if(multiCharCount <=0)
            return true;
        if(str.length() <= multiCharCount)
            return true;
        int occurence = 0;
        for(int i = 1; i < str.length(); i++){
            if(str.charAt(i) == str.charAt(i - 1)){
                occurence++;
            }else{
                occurence = 0;
            }
            if(occurence > (multiCharCount-1)){
                break;
            }
        }
        return occurence <= (multiCharCount-1);
    }
    
    public static String getFormattedAccountNumber(String unformatted, int[] lenGroup){
        String formatted = unformatted;
        int length = 0;
        for(int i = 0; i < lenGroup.length; i++){
            length += lenGroup[i];
        }
        for(int i = formatted.length(); i < length; i++){
            formatted = "0" + formatted;
        }
        //xxxx-xx-xxxxxx-xx-x
        String tmp = formatted;
        formatted = "";
        for(int i = 0; i < lenGroup.length; i++){
            formatted += tmp.substring(0, lenGroup[i]) + "-";
            tmp = tmp.substring(lenGroup[i]);
        }
        formatted =formatted.substring(0, formatted.length()-1);
        return formatted;
    }
    
    public static String formattedResult(String respondCode, String referenceNumber, String transactionObject){
    	return respondCode + "$$" + referenceNumber + "$$" + transactionObject;
    }
    
    public static String formattedResultWithSaldo(String respondCode, String referenceNumber, String saldo, String transactionObject){
    	return respondCode + "$$" + referenceNumber + "$$" + saldo + "$$" + transactionObject;
    }
    
    public static String usernameMasking(String username){
    	username = username.substring(0, 2) + "XXXXXX" + username.substring(8);
    	return username;
    }
    
    public static String stringPadding(String txt, String filler, int length, boolean isPaddingLeft){
    	if(filler.length() <= 0)
    		return txt;
    	if(txt.length() >= length)
    		return txt.substring(0, length);
    	while(txt.length() < length){
    		if(isPaddingLeft)
    			txt = txt + filler;
    		else
    			txt = filler + txt;
    	}
    	return txt;
    }
    
    public static boolean isAllDigit(String str, boolean needTrim){
    	boolean result = true;
    	if(needTrim){
    		str = str.trim();
    	}
    	if(str.length() > 0){
    		for(int i=0; i < str.length();i++){
        		if(!Character.isDigit(str.charAt(i))){
        			result = false;
        			break;
        		}
        	}
    	}else{
    		result = false;
    	}
    	
    	return result;
    }
    
    public static boolean isThisDateValid(String dateToValidate, String dateFormat) {
    	if (dateToValidate == null) {
    		return false;
    	}
    	SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    	sdf.setLenient(false);
    	try {
    		Date date = sdf.parse(dateToValidate);
    		
    	} catch (ParseException e) {
    		LogLoader.setError(TextUtil.class.getSimpleName(), "Error parsing borndate");
    		return false;
    	}
    	return true;
    }
    
    public static String cutAndPadd(String str, int length, boolean isPaddingLeft){
    	
    	if(str.length() > length){
    		return str.substring(0, length);
    	}
    	else{
    		return TextUtil.stringPadding(str, " ", length, isPaddingLeft);
    	}
    	
    	
    }    

    public static String sikat(String message1, String message2)
    {
      String str = "";
      int num1 = 0;
      for (int index = 0; index < message2.length(); ++index)
        //num1 += Convert.ToInt32(message2[index]);
      	//num1 += Character.getNumericValue(message2.charAt(index));
    	  num1 += (int)message2.charAt(index);
      int num2 = num1 / 10;
      for (int index = 0; index < message1.length(); ++index)
      {
        //int utf32 = Convert.ToInt32(message1[index]) ^ num2;
        //int utf32 = Character.getNumericValue((int)message1.charAt(index)) ^ num2;
    	int utf32 = (int)message1.charAt(index) ^ num2;
        //str = str + char.ConvertFromUtf32(utf32);
        //str = str + String.valueOf(Character.toChars(utf32));
        str = str + Character.toString((char)utf32);
      }
      return str;
    }
}
