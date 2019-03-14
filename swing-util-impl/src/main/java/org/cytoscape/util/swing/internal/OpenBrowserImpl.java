package org.cytoscape.util.swing.internal;

import java.awt.Desktop;
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

	private final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	private static String[] BROWSERS =
        { "xdg-open", "htmlview", "firefox", "mozilla", "konqueror", "chrome", "chromium" };
	final CyServiceRegistrar serviceRegistrar;
	AvailableCommands availableCommands = null;
	CommandExecutorTaskFactory taskFactory = null;
	SynchronousTaskManager taskManager = null;
	Properties props;

	public OpenBrowserImpl(final CyServiceRegistrar registrar) {
		this.serviceRegistrar = registrar;
		/*
		CyProperty<Properties> cyProps =
          serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
    props = cyProps.getProperties();
		*/
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

				if (openURLWithCyBrowser(url, new HashMap<>()))
					return true;

        if (openURLWithDesktop(uri)) {
            return true;
        } else {
						// See if the override browser works
						String defBrowser = props.getProperty(DEF_WEB_BROWSER_PROP_NAME);
						if (defBrowser != null && openURLWithBrowser(url, defBrowser))
							return true;

            for (final String browser : BROWSERS) {
                if (openURLWithBrowser(url, browser)) {
                    return true;
                }
            }
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

		private boolean openURLWithCyBrowser(final String url, Map<String, Object> extraArgs) {
			if (availableCommands == null) {
				availableCommands = serviceRegistrar.getService(AvailableCommands.class);
				taskFactory = serviceRegistrar.getService(CommandExecutorTaskFactory.class);
				taskManager = serviceRegistrar.getService(SynchronousTaskManager.class);
				CyProperty<Properties> cyProps =
       		   serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
    		props = cyProps.getProperties();
			}

			String useCyBrowser = props.getProperty(USE_CYBROWSER);
			if (useCyBrowser != null && !Boolean.parseBoolean(useCyBrowser)) {
				return false;
			}

			List<String> args = availableCommands.getArguments("cybrowser", "dialog");
			if (args == null || args.size() == 0)
				return false;

			if (extraArgs == null)
				extraArgs = new HashMap<>();

			extraArgs.put("url", url);

			TaskIterator ti = taskFactory.createTaskIterator("cybrowser", "dialog", extraArgs, null);
			try {
				taskManager.execute(ti);
			} catch (Exception e) {
				return false;
			}
			return true;
		}
}
