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

import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractObservableTask;
import org.cytoscape.work.TaskMonitor;

class GenerateNetworkViewsTask extends AbstractObservableTask<Collection<CyNetworkView>> {
	private final String name;
	private final CyNetworkReader viewReader;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkNaming namingUtil;
	private final int viewThreshold;
	private final VisualMappingManager vmm;
	private final CyNetworkViewFactory nullNetworkViewFactory;

	public GenerateNetworkViewsTask(final String name, final CyNetworkReader viewReader,
				final CyNetworkManager networkManager, final CyNetworkViewManager networkViewManager,
				final CyNetworkNaming namingUtil, final int viewThreshold, final VisualMappingManager vmm,
				final CyNetworkViewFactory nullNetworkViewFactory) {
		this.name = name;
		this.viewReader = viewReader;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.namingUtil = namingUtil;
		this.viewThreshold = viewThreshold;
		this.vmm = vmm;
		this.nullNetworkViewFactory = nullNetworkViewFactory;
	}

	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);

		final VisualStyle style = vmm.getCurrentVisualStyle(); // get the current style before registering the views!
		final CyNetwork[] networks = viewReader.getNetworks();
		double numNets = (double)(networks.length);
		int i = 0;

		Collection<CyNetworkView> result = new ArrayList<CyNetworkView>();
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
				result.add(view);
			} else {
				result.add(nullNetworkViewFactory.createNetworkView(network));
			}
			taskMonitor.setProgress((double)(++i)/numNets);
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
		finish(result);
	}
}
