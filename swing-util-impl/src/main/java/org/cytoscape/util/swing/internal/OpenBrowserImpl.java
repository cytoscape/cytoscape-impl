package org.cytoscape.util.swing.internal;

/*
 * #%L
 * Cytoscape Swing Utility Impl (swing-util-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import org.cytoscape.util.swing.OpenBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenBrowserImpl implements OpenBrowser {

	private final Logger logger = LoggerFactory.getLogger(OpenBrowserImpl.class);
	private static String[] BROWSERS =
        { "xdg-open", "htmlview", "firefox", "mozilla", "konqueror", "chrome", "chromium" };

	/**
	 * Opens the specified URL in the system default web browser.
	 *
	 * @return true if the URL opens successfully.
	 */
	@Override
	public boolean openURL(final String url) {
		try {
			URI uri = new URI(url);
			if(Desktop.isDesktopSupported()) {
				final Desktop desktop = Desktop.getDesktop();
				desktop.browse(uri);
			}
			else { //fallback if desktop API not supported
				for (final String browser : BROWSERS) {
					String cmd = browser + " " + url;
					final Process p = Runtime.getRuntime().exec(cmd);
					if(p.waitFor() == 0)
						break;
				}
			}
		} catch (IOException ioe) {
			JOptionPane.showInputDialog(null, "There was an error while attempting to open the system browser. "
					+ "\nPlease copy and paste the following URL into your browser:", url);
			logger.info("Error opening system browser; displaying copyable link instead");
		} catch (URISyntaxException e) {
			logger.warn("This URI is invalid: " + url, e);
			return false;
		} catch (InterruptedException e) {
			logger.warn("Browser process thread interrupted");
		}
		return true;
	}
}
