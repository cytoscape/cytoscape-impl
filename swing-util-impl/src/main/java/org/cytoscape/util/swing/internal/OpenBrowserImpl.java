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
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL has an incorrect format: " + url);
        }

        if (openURLWithDesktop(uri)) {
            return true;
        } else {
            for (final String browser : BROWSERS) {
                if (openURLWithBrowser(url, browser)) {
                    return true;
                }
            }
        }

        JOptionPane.showInputDialog(null, "Cytoscape was unable to open your web browser.. "
                + "\nPlease copy the following URL and paste it into your browser:", url);
        return false;
	}

    private boolean openURLWithDesktop(final URI uri) {
        if (!Desktop.isDesktopSupported())
            return false;
        try {
            Desktop.getDesktop().browse(uri);
            return true;
        } catch (IOException e) {
            logger.warn("Failed to launch browser through java.awt.Desktop.browse(): " + e.getMessage());
            return false;
        }
    }

    private boolean openURLWithBrowser(final String url, final String browser) {
        final ProcessBuilder builder = new ProcessBuilder(browser, url);
        try {
            builder.start();
            return true;
        } catch (IOException e) {
            logger.info(String.format("Failed to launch browser process %s: %s", browser, e.getMessage()));
            return false;
        }
    }
}
