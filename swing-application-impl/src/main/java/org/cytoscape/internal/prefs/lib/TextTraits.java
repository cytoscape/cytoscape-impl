package org.cytoscape.internal.prefs.lib;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TextTrait objects are used to store text formatting information..
 */
public final class TextTraits
{
    public interface Defaults
    {
        final Color  BACK_COLOR           = Color.WHITE;
        final Color  FORE_COLOR           = Color.BLACK;
        final int    FONT_SIZE            = 12;
        final int    FONT_STYLE           = Font.PLAIN;
        final String FONT_NAME            = "Serif";
        final String FONT_STYLE_STRING    = "plain";
    }

    static public String[] AVAILABLE_FONT_NAMES;
    {
        AVAILABLE_FONT_NAMES = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(AVAILABLE_FONT_NAMES);
//       List<String> strList = Arrays.asList(AVAILABLE_FONT_NAMES); 
//	 	strList.add(0, "------");
//         String[] predefined = { "Monospaced", "Dialog", "Serif", "SansSerif" };
//         for (String family : predefined)        // sort "Serif", "SansSerif", "Dialog", "Monospaced" up to top
//         {
//        	 	strList.remove(family);
//        	 	strList.add(0, family);
//         }
    }
    private Color       backColor = Defaults.BACK_COLOR, foreColor = Defaults.FORE_COLOR;
    private String      name = "";
    private String 		fontName = Defaults.FONT_NAME;
    private int 			fontSize = Defaults.FONT_SIZE;
    private int 			fontStyle = Defaults.FONT_STYLE;

    public String getTTName()    		{        return name;    }
    public void setTTName(String s)    	{        if (s != null) name = s;    }
    
    public TextTraits(String serialization)
    {
        this();
        fromString(serialization);
    }
    
   public void fromString(String s)
   {
	   int index1 = s.indexOf("(");
	   int index2 = s.indexOf(")");
	   fontName = s.substring(0,  index1).trim();
	   String[] tokens =  s.substring(index1+1,  index2).split(",");
	   
	   if (tokens.length == 2)
	   {
		   fontStyle = stringToStyle(tokens[1]);
		  try
		  {
			  fontSize =   Integer.parseInt(tokens[0]);
		  
		  } catch (NumberFormatException e) { fontSize = 11;	}
	   }
	   fontName = s.substring(0,  index1).trim();

   }
    
    public TextTraits(String name, String serialization)
    {
        this(serialization);
        	setTTName(name);
    }

    public TextTraits()    {    }
//    public static TextTraits makeDefault()	{ return new TextTraits(Defaults.FONT_NAME,Defaults.FONT_SIZE,Defaults.FONT_STYLE,Defaults.BACK_COLOR, Defaults.FORE_COLOR, 0);	}
    public TextTraits(String fontFamily, int inSize, int inStyle, Color bg, Color fg, String inName)
    {
    	this();
        backColor =  (bg == null) ? Defaults.BACK_COLOR : bg;
        foreColor = (fg == null) ?  Defaults.FORE_COLOR : fg;
        fontName = fontFamily;
        fontSize = inSize;
        fontStyle = inStyle;
        name = inName;
    }

	public Font getFont()    				{        return new Font(fontName,fontStyle, fontSize);       }
    public Color getColor()  				{        return foreColor;       }
    @Override public Object clone()    		{        return new TextTraits(fontName,fontSize,fontStyle, backColor, foreColor, name);    }

    @Override public String toString()		{	return	name + ": " + getFontName() + "(" + getSize() + ", " + getStyleString() + ")"	;		}

    public String getFontName()			{		return fontName;	}
    public void setFontName(String fn)	{		fontName = fn;		}
    public int getSize()    				{        return fontSize;    }
    public int getLineHeight()    		{        return (int) (1.4 * fontSize);    }
    public void setSize(int inSize)    	{        fontSize = inSize;    }
    public void setStyle(int inStyle)	{		fontStyle = inStyle;	}
    public int getStyle()				{		return fontStyle;	}
    public String getStyleString()		{		return styleToString(fontStyle);		}
//    public String getJustificationString()	{	return ParseUtil.justificationToString(justification);	}
//    public void setJustification(String j)	{	justification = ParseUtil.stringToJustification(j);	}
    public void setColor(Color c)		{		foreColor = c;	}
    public void setBackColor(Color c)	{		backColor = c;	}


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

    public static int stringToStyle(String s)
    {
    		int style = Font.PLAIN;
    		if (s.contains("bold"))	style += Font.BOLD;
    		if (s.contains("italic"))	style += Font.ITALIC;
      return style;
    }
    
}
