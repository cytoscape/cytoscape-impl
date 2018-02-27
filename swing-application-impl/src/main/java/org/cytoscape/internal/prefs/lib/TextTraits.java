package org.cytoscape.internal.prefs.lib;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * TextTrait objects are used to store text formatting information. They can be stored into XML using getAsElement().
 */
public final class TextTraits
{
    public interface Defaults
    {
        final Color  BACK_COLOR           = Color.WHITE;
        final Color  FORE_COLOR           = Color.BLACK;
        final int    FONT_SIZE            = 12;
        final int    FONT_STYLE           = Font.PLAIN;
        final String FONT_NAME            = "SansSerif";
        final String FONT_STYLE_STRING    = "plain";
        final String JUSTIFICATION_STRING = "left";
    }

    static public String[] AVAILABLE_FONT_NAMES;
    {
        AVAILABLE_FONT_NAMES = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(AVAILABLE_FONT_NAMES);
    }
    private Color       backColor = Defaults.BACK_COLOR, foreColor = Defaults.FORE_COLOR;
    private int       	justification;
    private String      name = "";
    private String 		fontName = Defaults.FONT_NAME;
    private int 		fontSize = Defaults.FONT_SIZE;
    private int 		fontStyle = Defaults.FONT_STYLE;

    public String getTTName()    		{        return name;    }
    public void setTTName(String s)    	{        if (s != null) name = s;    }
    
    public TextTraits(String context)
    {
        this();
        if (context != null) setTTName(context);
    }

    public TextTraits()    {    }
    public static TextTraits makeDefault()	{ return new TextTraits(Defaults.FONT_NAME,Defaults.FONT_SIZE,Defaults.FONT_STYLE,Defaults.BACK_COLOR, Defaults.FORE_COLOR, 0);	}
    public TextTraits(String inName, int inSize, int inStyle, Color bg, Color fg, int just)
    {
    	this();
        backColor =  (bg == null) ? Defaults.BACK_COLOR : bg;
        foreColor = (fg == null) ?  Defaults.FORE_COLOR : fg;
        fontName = inName;
        fontSize = inSize;
        fontStyle = inStyle;
        name = inName;
    }

	public Font getFont()    				{        return new Font(fontName,fontStyle, fontSize);       }
    public Color getColor()  				{        return foreColor;       }
    @Override public Object clone()    		{        return new TextTraits(fontName,fontSize,fontStyle, backColor, foreColor, justification);    }

    @Override public String toString()		{	return	getFontName() + "(" + getSize() + ", " + getStyleString() + ")"	;		}

    public String getFontName()			{		return fontName;	}
    public void setFontName(String fn)	{		fontName = fn;		}
    public int getSize()    			{        return fontSize;    }
    public int getLineHeight()    		{        return (int) (1.4 * fontSize);    }
    public void setSize(int inSize)    	{        fontSize = inSize;    }
    public void setStyle(int inStyle)	{		fontStyle = inStyle;	}
    public String getStyleString()		{		return ParseUtil.styleToString(fontStyle);		}
//    public String getJustificationString()	{	return ParseUtil.justificationToString(justification);	}
//    public void setJustification(String j)	{	justification = ParseUtil.stringToJustification(j);	}
    public void setColor(Color c)		{		foreColor = c;	}
    public void setBackColor(Color c)	{		backColor = c;	}

}
