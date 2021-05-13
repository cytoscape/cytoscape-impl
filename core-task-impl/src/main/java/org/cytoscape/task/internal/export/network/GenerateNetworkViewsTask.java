package org.cytoscape.task.internal.export.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class GenerateNetworkViewsTask extends AbstractTask implements ObservableTask {
	
	private final String name;
	private final CyNetworkReader viewReader;
	private final int viewThreshold;
	private final CyServiceRegistrar serviceRegistrar;
	private	List<CyNetworkView> results;
	private	List<CyNetwork> largeNetworks;
	public static final String JSON_EXAMPLE = "{ \"networks\":[101,102,103],\"views\":[200,201,204] }";

	public GenerateNetworkViewsTask(
			final String name,
			final CyNetworkReader viewReader,
			final int viewThreshold,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.name = name;
		this.viewReader = viewReader;
		this.viewThreshold = viewThreshold;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		final CyNetwork[] networks = viewReader.getNetworks();
		
		if (networks == null || networks.length == 0)
			return;
		
		taskMonitor.setProgress(0.0);
		final CyNetworkNaming networkNaming = serviceRegistrar.getService(CyNetworkNaming.class);
		double numNets = (double) networks.length;
		int i = 0;
		results = new ArrayList<>();
		largeNetworks = new ArrayList<>();
		
		for (CyNetwork net : networks) {
			// Use original name if exists
			String networkName = net.getRow(net).get(CyNetwork.NAME, String.class);
			
			if (networkName == null || networkName.trim().length() == 0) 
				networkName = (name != null) ? name : "? (Name is missing)";
			
			net.getRow(net).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle(networkName));
			
			serviceRegistrar.getService(CyNetworkManager.class).addNetwork(net, false);
			final int numGraphObjects = net.getNodeCount() + net.getEdgeCount();
			
			if (numGraphObjects < viewThreshold)
				createNetworkView(net);
			else
				largeNetworks.add(net);
			
			taskMonitor.setProgress((double)(++i)/numNets);
		}

		// If this is a subnetwork, and there is only one subnetwork in the root, check the name of the root network
		// If there is no name yet for the root network, set it the same as its base subnetwork
		if (networks.length == 1) {
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
					
					rootNet.getRow(rootNet).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle(networkName));
				}
			}			
		}
		
		setCurrentNetworkAndViewTask(networks[0]);
		
		if (!largeNetworks.isEmpty())
			insertTasksAfterCurrentTask(new ConfirmCreateNetworkViewsTask(largeNetworks));
	}

	private Object getStringResults() {
		String strRes = "";
		
		for (CyNetworkView view: results)
			strRes += (view.toString() + "\n");
		
		return strRes.isEmpty() ? null : strRes.substring(0, strRes.length()-1);
	}
	
	private void createNetworkView(final CyNetwork network) {
		var view = viewReader.buildCyNetworkView(network);
		
		var vmManager = serviceRegistrar.getService(VisualMappingManager.class);
		var viewStyle = vmManager.getVisualStyle(view);
		
		var viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		viewManager.addNetworkView(view, false);
		
		// Only set a style explicitly when no style (or usually the default one) is already set for this view.
		// This allows the CyNetworkReader implementation to set the desired style itself.
		if (viewStyle == null || viewStyle.equals(vmManager.getDefaultVisualStyle())) {
			var style = vmManager.getDefaultVisualStyle();
			
			// If the base network of this collection has a view, use the same base network’s style
			var rootNet = serviceRegistrar.getService(CyRootNetworkManager.class).getRootNetwork(network);
			var baseViewSet = viewManager.getNetworkViews(rootNet.getBaseNetwork());
			
			if (!baseViewSet.isEmpty())
				style = vmManager.getVisualStyle(baseViewSet.iterator().next());
			
			vmManager.setVisualStyle(style, view);
			style.apply(view);
		}
		
		if (!view.isSet(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)
				&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)
				&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION))
			view.fitContent();
		
		results.add(view);
	}
	
	public void setCurrentNetworkAndViewTask(final CyNetwork network) {
		final CyNetworkViewManager netViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		final List<CyNetworkView> views = new ArrayList<>(netViewManager.getNetworkViews(network));
		
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		boolean currentViewSet = false;
		
		for (CyNetworkView v : views) {
			if (v.getModel().equals(network)) {
				applicationManager.setCurrentNetworkView(v);
				currentViewSet = true;
				break;
			}
		}
		
		if (!currentViewSet)
			applicationManager.setCurrentNetwork(network);
	}
		
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class expectedType) {
		if (expectedType.equals(String.class))
			return getStringResults();
		else if (expectedType.equals(JSONResult.class)) {
			JSONResult res = () -> {if (results == null && largeNetworks.isEmpty()) 
				return "{}";
			else {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				List<CyNetwork> networks = new ArrayList<>();
				for (CyNetworkView view: results)
					networks.add(view.getModel());
				networks.addAll(largeNetworks);
				String jsonRes = "{ \"networks\":";
				jsonRes += cyJSONUtil.cyIdentifiablesToJson(networks);
				jsonRes += ", \"views\":";
				jsonRes += cyJSONUtil.cyIdentifiablesToJson(results) + "}";
				return jsonRes;
			}};
			return res;
		}
		return results;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(List.class, String.class, JSONResult.class);
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
			final CyNetworkViewFactory nullNetViewFactory =
					serviceRegistrar.getService(CyNetworkViewFactory.class, "(id=NullCyNetworkViewFactory)");
			final double numNets = (double) networks.size();
			int i = 0;
		
			for (CyNetwork net : networks) {
				if (createNetworkViews)
					createNetworkView(net);
				else
					results.add(nullNetViewFactory.createNetworkView(net));
				
				taskMonitor.setProgress((double)(++i)/numNets);
			}
			
			if (!networks.isEmpty())
				setCurrentNetworkAndViewTask(networks.get(0));
		}
		
		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Object getResults(Class expectedType) {
			if (expectedType.equals(String.class))
				return getStringResults();
			else if (expectedType.equals(JSONResult.class)) {
				JSONResult res = () -> {if (results == null && largeNetworks.isEmpty()) 
					return "{}";
				else {
					CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
					List<CyNetwork> networks = new ArrayList<>();
					for (CyNetworkView view: results)
						networks.add(view.getModel());
					for (CyNetwork net: largeNetworks) {
						if (!networks.contains(net))
							networks.add(net);
					}
					String jsonRes = "{ \"networks\":";
					jsonRes += cyJSONUtil.cyIdentifiablesToJson(networks);
					jsonRes += ", \"views\":";
					jsonRes += cyJSONUtil.cyIdentifiablesToJson(results);
					return jsonRes;
				}};
				return res;
			}
			return results;
		}
		
		@Override
		public List<Class<?>> getResultClasses() {
			return Arrays.asList(List.class, String.class, JSONResult.class);
		}
	}
}
