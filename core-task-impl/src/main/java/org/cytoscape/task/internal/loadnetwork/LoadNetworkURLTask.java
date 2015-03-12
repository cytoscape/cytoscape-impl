package org.cytoscape.task.internal.loadnetwork;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


/**
 * Specific instance of AbstractLoadNetworkTask that loads a URL.
 */
public class LoadNetworkURLTask extends AbstractLoadNetworkTask {
	
	@Tunable(description="The URL to load:", params = "fileCategory=network;input=true")
	public URL url;
	
	StreamUtil streamUtil;

	static String BAD_INTERNET_SETTINGS_MSG = "<html><p>Cytoscape has failed to connect to the URL. Please ensure that:</p><p><ol><li>the URL is correct,</li><li>your computer is able to connect to the Internet, and</li><li>your proxy settings are correct.</li></ol></p><p>The reason for the failure is: %s</html>";

	public LoadNetworkURLTask(CyNetworkReaderManager mgr, 
				  CyNetworkManager netmgr, final CyNetworkViewManager networkViewManager,
				  final Properties props,
				  CyNetworkNaming namingUtil, StreamUtil streamUtil, final VisualMappingManager vmm, final CyNetworkViewFactory nullNetworkViewFactory)
	{
		super(mgr, netmgr, networkViewManager, props, namingUtil, vmm, nullNetworkViewFactory);
		this.streamUtil = streamUtil;
	}

	/**
	 * Executes Task.
	 */
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
			streamUtil.getURLConnection(url).connect();
		} catch (IOException e) {
			throw new Exception(String.format(BAD_INTERNET_SETTINGS_MSG, e.getMessage()), e);
		}

		if (cancelled)
			return;

		taskMonitor.setStatusMessage("Finding compatible network reader for this file...");
		reader = mgr.getReader(url.toURI(),url.toString());

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
