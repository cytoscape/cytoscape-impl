package org.cytoscape.util.swing.internal;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Help {

	public static void linkout(String pageId) {
		openWebpage(pageId);
	}

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
		} catch (URISyntaxException e) {
		}
	}

	public static void openWebpage(String url) {
		try {
			openWebpage(new URL(url));
		} catch (Exception ex) {
		}
	}
}