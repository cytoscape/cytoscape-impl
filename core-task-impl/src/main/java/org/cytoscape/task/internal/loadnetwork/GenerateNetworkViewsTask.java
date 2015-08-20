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
import java.util.List;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

class GenerateNetworkViewsTask extends AbstractTask implements ObservableTask {
	
	private final String name;
	private final CyNetworkReader viewReader;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkNaming namingUtil;
	private final int viewThreshold;
	private final VisualMappingManager vmm;
	private final CyNetworkViewFactory nullNetworkViewFactory;
	private	Collection<CyNetworkView> results;

	public GenerateNetworkViewsTask(
			final String name,
			final CyNetworkReader viewReader,
			final CyNetworkManager networkManager,
			final CyNetworkViewManager networkViewManager,
			final CyNetworkNaming namingUtil,
			final int viewThreshold,
			final VisualMappingManager vmm,
			final CyNetworkViewFactory nullNetworkViewFactory
	) {
		this.name = name;
		this.viewReader = viewReader;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.namingUtil = namingUtil;
		this.viewThreshold = viewThreshold;
		this.vmm = vmm;
		this.nullNetworkViewFactory = nullNetworkViewFactory;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);

		final CyNetwork[] networks = viewReader.getNetworks();
		double numNets = (double)(networks.length);
		int i = 0;
		results = new ArrayList<>();
		
		final List<CyNetwork> largeNetworks = new ArrayList<>();
		
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
			
			if (numGraphObjects < viewThreshold)
				createNetworkView(network);
			else
				largeNetworks.add(network);
			
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
					rootNet.getRow(rootNet).set(
							CyNetwork.NAME, networks[0].getRow(networks[0]).get(CyNetwork.NAME, String.class));
				}
			}
		}
		
		// Make sure rootNetwork has a name
		for (CyNetwork net : networks) {
			if (net instanceof CySubNetwork){
				CySubNetwork subNet = (CySubNetwork) net;
				CyRootNetwork rootNet = subNet.getRootNetwork();
				String networkName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
				
				if (networkName == null || networkName.trim().length() == 0) {
					networkName = name;
					
					if (networkName == null)
						networkName = "? (Name is missing)";
					
					rootNet.getRow(rootNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(networkName));
				}
			}			
		}
		
		if (!largeNetworks.isEmpty())
			insertTasksAfterCurrentTask(new ConfirmCreateNetworkViewsTask(largeNetworks));
	}

	@Override
	public Object getResults(Class expectedType) {
		if (expectedType.equals(String.class))
			return getStringResults();
	
		return results;
	}

	private Object getStringResults() {
		String strRes = "";
		
		for (CyNetworkView view: results)
			strRes += (view.toString() + "\n");
		
		return strRes.isEmpty() ? null : strRes.substring(0, strRes.length()-1);
	}
	
	private void createNetworkView(final CyNetwork network) {
		final CyNetworkView view = viewReader.buildCyNetworkView(network);
		networkViewManager.addNetworkView(view);
		
		final VisualStyle style = vmm.getCurrentVisualStyle(); // get the current style before registering the views!
		vmm.setVisualStyle(style, view);
		style.apply(view);
		
		if (!view.isSet(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)
				&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)
				&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION))
			view.fitContent();
		
		results.add(view);
	}
	
	public class ConfirmCreateNetworkViewsTask extends AbstractTask implements ObservableTask {

		@Tunable(
				description = "Do you want to create a view for your large networks now?\nThis could take a long time.",
				params="ForceSetDirectly=true;ForceSetTitle=Create Network Views?"
		)
		public boolean createNetworkViews;
		
		private List<CyNetwork> networks;
		
		public ConfirmCreateNetworkViewsTask(final List<CyNetwork> networks) {
			this.networks = networks;
		}
		
		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setProgress(0.0);
			final double numNets = (double)(networks.size());
			int i = 0;
		
			for (CyNetwork net : networks) {
				if (createNetworkViews)
					createNetworkView(net);
				else
					results.add(nullNetworkViewFactory.createNetworkView(net));
				
				taskMonitor.setProgress((double)(++i)/numNets);
			}
		}
		
		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Object getResults(Class expectedType) {
			if (expectedType.equals(String.class))
				return getStringResults();
			
			return results;
		}
	}
}
