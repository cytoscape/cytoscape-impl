package org.cytoscape.cpath2.internal.task;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JOptionPane;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/**
 * Task to load a new network from a URL.
 *
 * Modified version of the original LoadNetworkTask from cytoscape code.
 * 
 */
public class LoadNetworkFromUrlTask implements Task {
	private final URL url;
	private final CPath2Factory factory;

	public LoadNetworkFromUrlTask(URL url, CPath2Factory factory) {
        this.url = url;
        this.factory = factory;
	}

	/**
	 * Executes Task.
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Loading Network");
        taskMonitor.setProgress(0);
        taskMonitor.setStatusMessage("Reading in Network Data...");

        try {
        	CyNetworkReaderManager readerManager = factory.getCyNetworkViewReaderManager();
			CyNetworkReader reader = readerManager.getReader(url.toURI(), url.getFile());

    		if (reader == null) {
    			JOptionPane.showMessageDialog(factory.getCySwingApplication().getJFrame(),
                      "Unable to connect to URL "+ url ,
                      "URL Connect Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
			taskMonitor.setStatusMessage("Creating Cytoscape Network...");
			reader.run(taskMonitor);
			CyNetwork[] networks = reader.getNetworks();
			CyNetwork cyNetwork = networks[0];

			// TODO: Does CPath2 listen for this?
//			Object[] ret_val = new Object[2];
//			ret_val[0] = cyNetwork;
//			ret_val[1] = url.toString();
//
//			Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, ret_val);

			if (cyNetwork != null) {
				informUserOfGraphStats(cyNetwork, taskMonitor);
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append("Could not read network from: ");
				sb.append(url);
				sb.append("\nThis file may not be a valid file format.");
				throw new IOException(sb.toString());
			}
			taskMonitor.setProgress(1.0);
		} catch (Exception e) {
			throw new Exception("Unable to load network.", e);
		}
	}

	/**
	 * Inform User of Network Stats.
	 * @param taskMonitor 
	 */
	private void informUserOfGraphStats(CyNetwork newNetwork, TaskMonitor taskMonitor) {
		NumberFormat formatter = new DecimalFormat("#,###,###");
		StringBuffer sb = new StringBuffer();

		// Give the user some confirmation
		sb.append("Successfully loaded network from:  ");
		sb.append(url);
		sb.append("\n\nNetwork contains " + formatter.format(newNetwork.getNodeCount()));
		sb.append(" nodes and " + formatter.format(newNetwork.getEdgeCount()));
		sb.append(" edges.\n\n");

		// TODO: Port this
//		if (newNetwork.getNodeCount() < Integer.parseInt(CytoscapeInit.getProperties()
//		                                                              .getProperty("viewThreshold"))) {
//			sb.append("Network is under "
//			          + CytoscapeInit.getProperties().getProperty("viewThreshold")
//			          + " nodes.  A view will be automatically created.");
//		} else {
//			sb.append("Network is over "
//			          + CytoscapeInit.getProperties().getProperty("viewThreshold")
//			          + " nodes.  A view has not been created."
//			          + "  If you wish to view this network, use "
//			          + "\"Create View\" from the \"Edit\" menu.");
//		}
		taskMonitor.setStatusMessage(sb.toString());
	}

	@Override
	public void cancel() {
	}
}
