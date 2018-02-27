package org.cytoscape.internal.prefs.lib;

import java.awt.Font;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.swing.text.StyleConstants;

public class ParseUtil
{
//----------------------------------------------------------------------------------------------

    
    public static boolean getBool(String text) {
    	return getBool(text, false);
    }
    
    /**
     * getBool with null protected default value
     * @param text
     * @param deflt
     * @return
     */
    public static boolean getBool(String text, Boolean deflt)
    {
        if (text == null) {
        	if (deflt == null)
        		return false;
        	return deflt;
        }
        if ("1".equals(text))	return true;
        if (text.charAt(0) == 'T' || text.charAt(0) == 't')	return true;
        return false;     
    }

    public static boolean isInteger(String text)
    {
        if (text == null)            return false;
        try
        {
            Integer.parseInt(text);
            return true;
        }
        catch (NumberFormatException e)        {            return false;        }
    }
    
//    public static int getInteger(String inString)
//    {
//        int result = 0;
//       if (inString != null)
//        {
//           if (inString.equals("4294967296"))        	return Integer.MAX_VALUE;
//           inString = inString.trim();
//            try
//            {
//                if (inString.length() > 0)
//                    result = Integer.parseInt(inString);
//            }
//            catch (NumberFormatException ex)           
//            {
//            	return (int) getFloat(inString);
//            }
//        }
//        return result;
//    }
//    
    public static int getInteger(String inString, int deflt)
    {
        int result = deflt;
        if (inString != null)
        {
            inString = inString.trim();
            try
            {
                if (inString.length() > 0)
                    result = Integer.parseInt(inString);
            }
            catch (NumberFormatException ex)            {}
        }
        return result;
    }

    public static boolean isNumber(String s)
    {
    	if ("0".equals(s)) 									return true;
    	if (s == null || "null".equals(s) || s.isEmpty())	return false;
    	try
    	{
    		s = s.trim();
    		if (s.endsWith("%"))
    			s = s.substring(0, s.length() - 1);

    		if (s.length() != 0)
    			Double.parseDouble(s);
    	}
    	catch (Exception e){	
    		try{
    			return !Double.isNaN(getNumber(s).doubleValue());
    		}
    		catch(Exception pe){
    			return false;
    		} 
    	}
    	return true;
    }

// 
    //I18N parser
    private static Number getNumber(String inString) throws ParseException{
    	return Double.parseDouble(inString);			
    }
    
    //Doubles
    public static double getDouble(String inString)   { return getDouble(inString, 0.0);	}  
    public static double getDouble(String inString, double deflt)     
    {
    	if (inString == null || inString.length() == 0 || "null".equals(inString))	return deflt;
    	inString = inString.trim();
    	if (inString.endsWith("%"))		return getDouble(inString.substring(0, inString.length() - 1)) / 100.0;
    	double result = deflt;
    	try
    	{    		result =   Double.parseDouble(inString);        }
    	catch (NumberFormatException e)
    	{
    		try{
    			result = getNumber(inString).doubleValue();
    		}
    		catch(ParseException pe){
    			return Double.NaN;  //pe.printStackTrace();
    		}
    	}
    	return result;
    }
//    
//    //Floats
//    public static float getFloat(String inString)	{ return getFloat(inString, 0.0f);	}
//    public static float getFloat(String inString, float deflt)
//    {
//    	float result = deflt;
//    	if (inString != null && !("null".equals(inString))){
//    		try
//	    	{
//	    			inString = inString.trim();
//	    			if (inString.length() != 0)
//	    				result = Float.parseFloat(inString); 
//	    	}
//	    	catch (NumberFormatException e)
//	    	{
//	    		try{
//	    			result = getNumber(inString).floatValue();
//	    		}
//	    		catch(ParseException pe){
//	    			return Float.NaN;  //pe.printStackTrace();		//ALERT AM 12/5/12 change to deflt?  NaN seems like wrong behavior.
//	    		}
//	    	}
//    	}
//    	return result;
//    }
//	public static float[] getFloatArray(String separator, String target) 
//	{
//		if (target == null || target.length() == 0) return new float[] { 1f };
//		//split out the first and last characters if they are [ or ].
//		if(target.charAt(0)=='[') target=target.substring(1);
//		if(target.charAt(target.length()-1)==']') target=target.substring(0, target.length()-1);
////		System.out.println("target string="+target);
//		String[] parts = target.split(separator);
//		float[] floats = new float[parts.length];
//		for (int i = 0; i < parts.length; i++) 
//			floats[i] = ParseUtil.getFloat(parts[i]);
//		return floats;
//	}
//  //  public static boolean getBooleanAttribute(Element elem, String attribute)    {         return toBool(elem.getAttributeValue(attribute));    }
//     public static boolean toBool(String s)    {        return "1".equals(s) || "true".equals(s);    }
//    static public String boolToString(boolean truth)    {        return (truth) ? "1" : "0";    }
//
//    private static final int DEFAULT_SIZE = 12;
//
////  
//    public static TextTraits fontToElement(Font inFont)
//    {
//    	TextTraits elem = new TextTraits();
//        elem.setFontName(inFont.getFamily());
//        elem.setSize(inFont.getSize());
//        elem.setStyle(inFont.getStyle());
//        return elem;
//    }

    public static String styleToString(int style)
    {
      switch (style)
      {
            case Font.BOLD:                    return "bold";
            case Font.ITALIC:                  return "italic";
            case Font.BOLD + Font.ITALIC:      return "bold-italic";
       }
      return "plain";
    }
    
    

//    public static final int ALIGN_LEFT = 0, ALIGN_RIGHT = 1, ALIGN_CENTER = 2;

//    public static String justificationToString(int inJust)
//    {
//        switch (inJust)
//        {
//            case StyleConstants.ALIGN_LEFT:              return "left";
//            case StyleConstants.ALIGN_CENTER:            return "center";
//            case StyleConstants.ALIGN_RIGHT:             return "right";
//        }
//        return "left";
//    }
//
//    public static String justificationToString(float inJust)
//    {
//        // if (inJust == Component.LEFT_ALIGNMENT)
//        // return "left";
//        if (inJust == StyleConstants.ALIGN_CENTER)          return "center";
//        if (inJust == StyleConstants.ALIGN_RIGHT)           return "right";
//        return "left";
//    }
//
//    public static int stringToAlignment(String inJust)
//    {
//        if ("right".equals(inJust))           return StyleConstants.ALIGN_RIGHT;
//        if ("center".equals(inJust))          return StyleConstants.ALIGN_CENTER;
//        return StyleConstants.ALIGN_LEFT;
//    }
//
//    public static int stringToStyle(String inStyle, boolean forceItal)
//    {
//        int iStyle = forceItal ? Font.ITALIC: Font.PLAIN;
//        if (inStyle == null || inStyle.length() == 0)    return iStyle;
//        if (inStyle.indexOf("bold") >= 0)
//            iStyle += Font.BOLD;
//        if (inStyle.indexOf("italic") >= 0)			// || forceItal
//            iStyle += Font.ITALIC;
//        return iStyle;
//    }
//    //--------------------------------------------------------------------------------



	public static String base64Encode(String s) {		return base64Encode(s.getBytes());	}
	public static String base64Encode(byte[] bytes) {		return Base64.getEncoder().encodeToString(bytes);	}
	public static byte[] base64Decode(String s) throws IOException {		return Base64.getDecoder().decode(s);	}


}
