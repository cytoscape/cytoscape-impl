package de.mpg.mpi_inf.bioinf.netanalyzer;

import java.awt.Desktop;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenBrowser {
	private static final Logger logger = LoggerFactory.getLogger(OpenBrowser.class);
	private OpenBrowser() {}
	public static void openURL(String urlString) {
		try {
			URL url = new URL(urlString);
			Desktop.getDesktop().browse(url.toURI());
		} catch (Exception e) {
			logger.warn("failed to open url: " + urlString, e);
		}
	}
}
