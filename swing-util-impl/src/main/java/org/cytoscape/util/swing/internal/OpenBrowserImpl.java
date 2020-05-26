package org.cytoscape.util.swing.internal;

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Utility Impl (swing-util-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class OpenBrowserImpl implements OpenBrowser {

	private static String[] BROWSERS =
        { "xdg-open", "htmlview", "firefox", "mozilla", "konqueror", "chrome", "chromium" };
	
	private final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final CyServiceRegistrar serviceRegistrar;

	public OpenBrowserImpl(CyServiceRegistrar registrar) {
		this.serviceRegistrar = registrar;
	}

	/**
	 * Opens the specified URL in CyBrowser or the default desktop browser
	 * depending on the useCyBrowser argument.
	 *
	 * @param url the URL to open
	 * @param useCyBrowser if false, do not use CyBrowser
	 * @return true if the URL opens successfully.
	 */
	@Override
	public boolean openURL(final String url, boolean useCyBrowser) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("URL has an incorrect format: " + url);
		}
		if (useCyBrowser) {
			if (openURLWithCyBrowser(url, new HashMap<>()))
				return true;
		} else {
			if (openURLWithDesktop(uri)) {
				return true;
			} else if (openURLWithDefault(url)) {
				return true;
			}
		}
		logger.warn("Cytoscape was unable to open your web browser.. "
				+ "\nPlease copy the following URL and paste it into your browser: " + url);
		return false;
	}

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
		
		if (isDialogBlocking()) {
			// Try to open with the default OS browser first,
			// because a modal dialog would block the CyBrowser window...
			if (openURLWithDefault(url))
				return true;
			
			if (openURLWithDesktop(uri))
				return true;
			
			if (openURLWithCyBrowser(url, new HashMap<>()))
				return true;
		} else {
			// No blocking dialog open? Then this is the preferred sequence...
			if (openURLWithCyBrowser(url, new HashMap<>()))
				return true;

			if (openURLWithDesktop(uri))
				return true;
		
			if (openURLWithDefault(url))
				return true;
		}

		logger.warn("Cytoscape was unable to open your web browser.. "
				+ "\nPlease copy the following URL and paste it into your browser: " + url);
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

	private boolean openURLWithDefault(final String url) {
		// See if the override browser works
		var defBrowser = getProperty(DEF_WEB_BROWSER_PROP_NAME);

		try {
			if (defBrowser != null && openURLWithBrowser(url, defBrowser))
				return true;
		} catch (Exception e) {
			// Ignore...
		}

		for (var browser : BROWSERS) {
			try {
				if (openURLWithBrowser(url, browser))
					return true;
			} catch (Exception e) {
				// Ignore...
			}
		}

		return false;
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

	private boolean openURLWithCyBrowser(String url, Map<String, Object> extraArgs) {
		var useCyBrowser = getProperty(USE_CYBROWSER);
		
		if (useCyBrowser != null && !Boolean.parseBoolean(useCyBrowser))
			return false;

		var availableCommands = serviceRegistrar.getService(AvailableCommands.class);
		var args = availableCommands.getArguments("cybrowser", "dialog");
		
		if (args == null || args.size() == 0)
			return false;

		if (extraArgs == null)
			extraArgs = new HashMap<>();

		extraArgs.put("url", url);

		var taskFactory = serviceRegistrar.getService(CommandExecutorTaskFactory.class);
		var ti = taskFactory.createTaskIterator("cybrowser", "dialog", extraArgs, null);
		
		try {
			var taskManager = serviceRegistrar.getService(SynchronousTaskManager.class);
			taskManager.execute(ti);
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	private boolean isDialogBlocking() {
		boolean blocking = false;
		var windows = Window.getWindows();
		
		for (var w : windows) {
			if (!w.isShowing())
				continue;
			
			if (w instanceof Dialog) {
				var modalityType = ((Dialog) w).getModalityType();
				
				if (modalityType == ModalityType.APPLICATION_MODAL || modalityType == ModalityType.TOOLKIT_MODAL) {
					blocking = true;
					break;
				}
			}
		}
		
		return blocking;
	}
	
	private String getProperty(String key) {
		var cyProps = serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		var props = (Properties) cyProps.getProperties();

		return props.getProperty(key);
	}
}
