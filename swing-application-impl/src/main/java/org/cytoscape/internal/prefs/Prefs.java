package org.cytoscape.internal.prefs;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JComboBox;

import org.cytoscape.internal.prefs.lib.ProxyConfig;
import org.cytoscape.internal.prefs.lib.StringUtil;

abstract public class Prefs extends HashMap<String, String>
{
    public static final String LAST_DATA_DIR = "lastDataDirectory";
    public static final String LAST_DOCUMENT_DIR = "lastDocumentDirectory";
    public static final String PREFS_EMAIL_EXCEPTIONS_TO_CYTOSCAPE = "emailExceptionsToCytoscape";
	// ---------------------------------------------------------------------------
    protected static Prefs instance; 
    public static Prefs getPrefs() 	{ 		return instance;		}

	public void setDialogDontAsk(String dialogKey, int userChoice ){
		String fromPrefs =  get("dontAsk");
	}
	
	public boolean isDialogDontAsk(String dialogKey){
		String fromPrefs =  get(dialogKey + "dontAsk");
		return (fromPrefs != null) && fromPrefs.contains(dialogKey);
	}
	
	public Locale getLocale()		{		return new Locale("en"); 	}  //get(PrefsML.Language.language), get(PrefsML.Language.country)

	// ---------------------------------------------------------------------------
    public void resetDefaults()
    {
    }
	// ---------------------------------------------------------------------------
	public void setString(String packageName, String name, String inValue)
	{
		String value = inValue;
		if (value == null)			value = ""; // avoid setting a null attribute value
		put(name, value);
	}

	// ---------------------------------------------------------------------------	
	public boolean getLoggingShouldSend()		{	return "true".equalsIgnoreCase(get("sendLogsstatic"));		}
	public boolean getLoggingShouldWrite()		{	return "true".equalsIgnoreCase(get("Logging.writeLogs"));	}
	public int getLoggingSaveDuration()			{ 	return stringToInt(get("Logging.saveDuration"));	} //days
	private int stringToInt(String string) {
		try
		{
			return Integer.parseInt(string);
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}

	public int getLoggingSaveCount()			{	return stringToInt(get("Logging.saveCount"));	}
	public String getLoggingPostURL()			{	return get("Logging.postURL");		}
	public void savePrefs()						{ 												}

    abstract public ProxyConfig getProxy();	
    abstract public void setProxyConfig(ProxyConfig config);
    public boolean isProxyConfigured() 
    {
    	ProxyConfig proxy = getProxy();
    	return !StringUtil.isEmpty(proxy.getHost()) && proxy.getPortInt() > 0;
    }
    public void reset()	{}
    public static String getNAString()	{		return "n/a";	}
	
	abstract public File getPrefsFile();
	abstract public boolean canSetPrefs();
	
//	public SElement getDefaultPageSection(boolean isLayout, String defltElementName)	{		return new SElement(defltElementName);	}
	//----------------------------------------------------------------------------------------------------
	public void saveRecentDirectory(File file)
	{
		if (file == null) return;
		File parent = file.getParentFile();
		put("lastDirectory", parent.getAbsolutePath());
	}
	
	public String getRecentDirectory(String fld)	{		return get("lastDirectory");	}

	public void sync(JComboBox<String> b, String value, boolean setting)
	{
		if (setting) 		b.setSelectedItem(get(value));
		else				put(value, "" +b.getSelectedItem());
	}
	
	
}