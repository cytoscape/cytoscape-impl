/*
 File: GenerateNetworkViewsTask.java

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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

class GenerateNetworkViewsTask extends AbstractTask {
	private final String name;
	private final CyNetworkReader viewReader;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkNaming namingUtil;
	private final int viewThreshold;
	private final VisualMappingManager vmm;

	public GenerateNetworkViewsTask(final String name, final CyNetworkReader viewReader,
				final CyNetworkManager networkManager, final CyNetworkViewManager networkViewManager,
				final CyNetworkNaming namingUtil, final int viewThreshold, final VisualMappingManager vmm) {
		this.name = name;
		this.viewReader = viewReader;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.namingUtil = namingUtil;
		this.viewThreshold = viewThreshold;
		this.vmm = vmm;
	}

	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);

		final VisualStyle style = vmm.getCurrentVisualStyle(); // get the current style before registering the views!
		final CyNetwork[] networks = viewReader.getNetworks();
		double numNets = (double)(networks.length);
		int i = 0;

		for (CyNetwork network : networks) {
			// Use original name if exists
			String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
			if (networkName == null || networkName.trim().length() == 0) {
				networkName = name;
				if (networkName == null)
					networkName = "? (Name is missing)";
				
				network.getRow(network).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(networkName));
			}
			networkManager.addNetwork(network);

			final int numGraphObjects = network.getNodeCount() + network.getEdgeCount();
			if (numGraphObjects < viewThreshold) {
				final CyNetworkView view = viewReader.buildCyNetworkView(network);
				networkViewManager.addNetworkView(view);
				vmm.setVisualStyle(style, view);
				style.apply(view);
				
				if (!view.isSet(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)
						&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)
						&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION))
					view.fitContent();
			}

			taskMonitor.setProgress((double)(++i)/numNets);

			informUserOfGraphStats(network, numGraphObjects, taskMonitor);
		}

		// If this is a subnetwork, and there is only one subnetwork in the root, check the name of the root network
		// If there is no name yet for the root network, set it the same as its base subnetwork
		if (networks.length == 1){
			if (networks[0] instanceof CySubNetwork){
				CySubNetwork subnet = (CySubNetwork) networks[0];
				final CyRootNetwork rootNet = subnet.getRootNetwork();
				String rootNetName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
				if (rootNetName == null || rootNetName.trim().length() == 0){
					// The root network does not have a name yet, set it the same as the base subnetwork
					rootNet.getRow(rootNet).set(CyNetwork.NAME, networks[0].getRow(networks[0]).get(CyNetwork.NAME, String.class));
				}
			}
		}
		
		
		// Make sure rootNetwork has a name
		for (CyNetwork network : networks) {

			if (network instanceof CySubNetwork){
				CySubNetwork subNet = (CySubNetwork) network;
				CyRootNetwork rootNet = subNet.getRootNetwork();

				String networkName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
				if(networkName == null || networkName.trim().length() == 0) {
					networkName = name;
					if(networkName == null)
						networkName = "? (Name is missing)";
					
					rootNet.getRow(rootNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(networkName));
				}
			}			
		}
	}

	/**
	 * Inform User of Network Stats.
	 */
	private void informUserOfGraphStats(final CyNetwork newNetwork, final int objectCount,
			final TaskMonitor taskMonitor) {
		NumberFormat formatter = new DecimalFormat("#,###,###");
		StringBuffer sb = new StringBuffer();

		// Give the user some confirmation
		sb.append("Successfully loaded network from:  ");
		sb.append(name);
		sb.append("\n\nNetwork contains " + formatter.format(newNetwork.getNodeCount()));
		sb.append(" nodes and " + formatter.format(newNetwork.getEdgeCount()));
		sb.append(" edges.\n\n");

		if (objectCount < viewThreshold) {
			sb.append("Network is under " + viewThreshold
					+ " graph objects.  A view will be automatically created.");
		} else {
			sb.append("Network is over " + viewThreshold + " graph objects.  A view has not been created."
					+ "  If you wish to view this network, use " + "\"Create View\" from the \"Edit\" menu.");
		}

		taskMonitor.setStatusMessage(sb.toString());
	}
}
