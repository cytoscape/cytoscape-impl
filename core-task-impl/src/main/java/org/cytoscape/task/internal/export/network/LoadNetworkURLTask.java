package org.cytoscape.task.internal.export.network;

import java.io.IOException;
import java.net.URL;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

/**
 * Specific instance of AbstractLoadNetworkTask that loads a URL.
 */
public class LoadNetworkURLTask extends AbstractLoadNetworkTask {
	
	@Tunable(description="The URL to load:", params = "fileCategory=network;input=true")
	public URL url;
	
	static String BAD_INTERNET_SETTINGS_MSG = "<html><p>Cytoscape has failed to connect to the URL. Please ensure that:</p><p><ol><li>the URL is correct,</li><li>your computer is able to connect to the Internet, and</li><li>your proxy settings are correct.</li></ol></p><p>The reason for the failure is: %s</html>";

	private CyNetworkReader reader;
	
	public LoadNetworkURLTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (url == null)
			throw new NullPointerException("url is null");

		this.taskMonitor = taskMonitor;
		
		final String urlString = url.getFile();
		final String[] parts = urlString.split("/");
		name = parts[parts.length-1];

		taskMonitor.setTitle(String.format("Loading Network from \'%s\'", name));
		taskMonitor.setStatusMessage("Checking URL...");
		
		try {
			serviceRegistrar.getService(StreamUtil.class).getURLConnection(url).connect();
		} catch (IOException e) {
			throw new Exception(String.format(BAD_INTERNET_SETTINGS_MSG, e.getMessage()), e);
		}

		if (cancelled)
			return;

		taskMonitor.setStatusMessage("Finding compatible network reader for this file...");
		reader = serviceRegistrar.getService(CyNetworkReaderManager.class).getReader(url.toURI(),url.toString());

		if (cancelled)
			return;

		if (reader == null)
			throw new NullPointerException("Failed to find reader for specified URL: " + name);

		taskMonitor.setStatusMessage("Loading network...");
		loadNetwork(reader);
	}
	
	protected CyNetwork[] getCyNetworks() {
		return reader.getNetworks();
	}
}
