/*
 File: LoadNetworkURLTask.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.task.internal.loadnetwork;


import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


/**
 * Specific instance of AbstractLoadNetworkTask that loads a URL.
 */
public class LoadNetworkURLTask extends AbstractLoadNetworkTask {
	@Tunable(description="The URL to load", params = "fileCategory=network;input=true")
	public URL url;
	
	StreamUtil streamUtil;

	static String BAD_INTERNET_SETTINGS_MSG = "<html><p>Cytoscape has failed to connect to the URL. Please ensure that:</p><p><ol><li>the URL is correct,</li><li>your computer is able to connect to the Internet, and</li><li>your proxy settings are correct.</li></ol></p><p>The reason for the failure is: %s</html>";

	public LoadNetworkURLTask(CyNetworkReaderManager mgr, 
				  CyNetworkManager netmgr, final CyNetworkViewManager networkViewManager,
				  final Properties props,
				  CyNetworkNaming namingUtil, StreamUtil streamUtil)
	{
		super(mgr, netmgr, networkViewManager, props, namingUtil);
		this.streamUtil = streamUtil;
	}

	/**
	 * Executes Task.
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (url == null)
			throw new NullPointerException("url is null");

		this.taskMonitor = taskMonitor;
		
		name = url.toString();

		taskMonitor.setTitle(String.format("Loading Network from \'%s\'", name));

		taskMonitor.setStatusMessage("Checking URL...");
		try {
			streamUtil.getURLConnection(url).connect();
		} catch (IOException e) {
			throw new Exception(String.format(BAD_INTERNET_SETTINGS_MSG, e.getMessage()), e);
		}

		if (cancelled)
			return;

		taskMonitor.setStatusMessage("Finding network reader...");
		reader = mgr.getReader(url.toURI(),url.toString());

		if (cancelled)
			return;

		if (reader == null)
			throw new NullPointerException("Failed to find reader for specified URL: " + name);

		taskMonitor.setStatusMessage("Loading network...");
		loadNetwork(reader);
	}
}
