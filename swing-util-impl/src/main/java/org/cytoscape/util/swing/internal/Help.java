package org.cytoscape.util.swing;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

public class Help 
{
	private static String HELP_PREFIX = "http://manual.cytoscape.org/en/3.4.0/";
	private static String HELP_SUFFIX = ".html";

	public static void linkout(String pageId)
	{
  		openWebpage(pageId);
  	}
	   
    //============================
    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
            }
        }
    }

    public static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {  }
    }

    public static void openWebpage(String url) {
 		try{
  			openWebpage(new URL(url));
  	}
  	catch(Exception ex){}

    }
}