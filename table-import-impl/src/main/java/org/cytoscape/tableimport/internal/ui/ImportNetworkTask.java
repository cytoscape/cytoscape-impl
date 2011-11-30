
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.tableimport.internal.ui;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.tableimport.internal.reader.GraphReader;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;


/**
 *
 */
public class ImportNetworkTask extends AbstractTask { //implements CyNetworkViewReader {

	protected CyNetworkView[] cyNetworkViews;
	protected VisualStyle[] visualstyles;
	private final GraphReader reader;
	
	final CyNetwork network = CytoscapeServices.cyNetworkFactory.createNetwork();

	/**
	 * Creates a new ImportNetworkTask object.
	 *
	 * @param reader  DOCUMENT ME!
	 * @param source  DOCUMENT ME!
	 */
	public ImportNetworkTask(final GraphReader reader) {
		this.reader = reader;
	}


	@Override
	public void run(TaskMonitor tm) throws IOException {
		tm.setProgress(0.10);
		this.reader.setNetwork(network);

		if (this.cancelled){
			return;
		}

		this.reader.read();

		tm.setProgress(0.80);

		if (this.cancelled){
			return;
		}

		final CyNetworkView view = CytoscapeServices.cyNetworkViewFactory.createNetworkView(network);

		CytoscapeServices.cyNetworkManager.addNetwork(network);
		CytoscapeServices.cyNetworkViewManager.addNetworkView(view);

		//view.fitContent();

		tm.setProgress(1.0);

		//informUserOfGraphStats(network, tm);
	}

	/**
	 * Inform User of Network Stats.
	 */
	private void informUserOfGraphStats(final CyNetwork newNetwork, final TaskMonitor taskMonitor) {
		NumberFormat formatter = new DecimalFormat("#,###,###");
		StringBuffer sb = new StringBuffer();

		// Give the user some confirmation
		sb.append("Successfully loaded network from:  ");
		sb.append(newNetwork.getCyRow().get("title", String.class));
		sb.append("\n\nNetwork contains "
				+ formatter.format(newNetwork.getNodeCount()));
		sb.append(" nodes and " + formatter.format(newNetwork.getEdgeCount()));
		sb.append(" edges.\n\n");

		String thresh = "0"; //CytoscapeServices.cyProperties.getProperties().getProperty("viewThreshold");

		if (newNetwork.getNodeCount() < Integer.parseInt(thresh)) {
			sb.append("Network is under " + thresh
					+ " nodes.  A view will be automatically created.");
		} else {
			sb.append("Network is over " + thresh
					+ " nodes.  A view has not been created."
					+ "  If you wish to view this network, use "
					+ "\"Create View\" from the \"Edit\" menu.");
		}

		taskMonitor.setStatusMessage(sb.toString());
	}	

}
