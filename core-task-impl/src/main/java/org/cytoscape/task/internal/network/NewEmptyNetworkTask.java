package org.cytoscape.task.internal.network;

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


import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
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
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;


/**
 * Create an empty network with view.
 */
public class NewEmptyNetworkTask extends AbstractTask implements ObservableTask {

	private final CyNetworkNaming namingUtil; 
	private final CyNetworkViewManager netViewMgr;
	private final VisualMappingManager vmMgr;
	private final CyApplicationManager appMgr;
	protected final CyNetworkFactory netFactory;
	protected final CyNetworkManager netMgr;
	protected final CyRootNetworkManager rootNetMgr;
	protected final CyServiceRegistrar registrar;

	private boolean cancel;
	private CyNetworkView view;
	private CySubNetwork subNetwork;

	public static final String CRERATE_NEW_COLLECTION_STRING = " -- Create new network collection --";

	//******** tunables ********************

	public ListSingleSelection<String> rootNetworkList;
	@Tunable(description = "Network Collection:", 
	         longDescription = "Choose the network collection the new network should be part of. "+
					                   "If no network collection is selected, a new network collection is created.",
	         exampleStringValue = "network name",
	         gravity=1.0)
	public ListSingleSelection<String> getRootNetworkList() {
		return rootNetworkList;
	}

	public void setRootNetworkList(ListSingleSelection<String> roots) {
		rootNetworkList = roots;
	}

	@Tunable(description = "Network Name:", 
	         longDescription = "Enter the name of the new network.",
	         exampleStringValue = "My New Network",
	         gravity=2.0)
	public String name = "Network";

	@Tunable(description = "Network View Renderer:", 
	         longDescription = "Select the renderer to use for the new network view.  By default, "+
					                   "the standard Cytoscape 2D renderer (Ding) will be used",
	         exampleStringValue = "Cytoscape 2D",
	         gravity=3.0)
	public ListSingleSelection<NetworkViewRenderer> renderers;

	@ProvidesTitle
	public String getTitle() {
		return "Create New Network ";
	}

	protected HashMap<String, CyRootNetwork> name2RootMap;

	@SuppressWarnings("unchecked")
	public NewEmptyNetworkTask(final CyNetworkFactory netFactory,
							   final CyNetworkManager netMgr,
							   final CyNetworkViewManager netViewMgr,
							   final CyNetworkNaming namingUtil,
							   final VisualMappingManager vmMgr,
							   final CyRootNetworkManager rootNetMgr,
							   final CyApplicationManager appMgr,
							   final Set<NetworkViewRenderer> viewRenderers,
								 final CyServiceRegistrar registrar) {
		this.netMgr = netMgr;
		this.netViewMgr = netViewMgr;
		this.netFactory = netFactory;
		this.namingUtil = namingUtil;
		this.vmMgr = vmMgr;
		this.rootNetMgr = rootNetMgr;
		this.appMgr = appMgr;
		this.registrar = registrar;

		// initialize the network Collection
		this.name2RootMap = getRootNetworkMap();

		final List<String> rootNames = new ArrayList<>();
		rootNames.addAll(name2RootMap.keySet());

		if (!rootNames.isEmpty()) {
			sort(rootNames);
			rootNames.add(0, CRERATE_NEW_COLLECTION_STRING);
		}

		rootNetworkList = new ListSingleSelection<>(rootNames);

		if (!rootNames.isEmpty())
			rootNetworkList.setSelectedValue(rootNames.get(0));

		final List<CyNetwork> selectedNetworks = appMgr.getSelectedNetworks();

		if (selectedNetworks != null && selectedNetworks.size() > 0) {
			CyNetwork selectedNetwork = appMgr.getSelectedNetworks().get(0);
			String rootName = "";

			if (selectedNetwork instanceof CySubNetwork) {
				final CySubNetwork subnet = (CySubNetwork) selectedNetwork;
				final CyRootNetwork rootNet = subnet.getRootNetwork();
				rootName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
			} else {
				// it is a root network
				rootName = selectedNetwork.getRow(selectedNetwork).get(CyNetwork.NAME, String.class);
			}

			rootNetworkList.setSelectedValue(rootName);
		}

		// If there is only one registered renderer, we don't want to add it to the List Selection,
		// so the combo-box does not appear to the user, since there is nothing to select anyway.
		if (viewRenderers.size() > 1) {
			renderers = new ListSingleSelection<NetworkViewRenderer>(new ArrayList<>(viewRenderers));
			final NetworkViewRenderer defViewRenderer = appMgr.getDefaultNetworkViewRenderer();

			if (defViewRenderer != null && viewRenderers.contains(defViewRenderer))
				renderers.setSelectedValue(defViewRenderer);
		} else {
			renderers = new ListSingleSelection<>(Collections.EMPTY_LIST);
		}
	}

	@Override
	public void run(final TaskMonitor tm) {
		tm.setProgress(0.0);

		final String networkCollectionName = rootNetworkList.getSelectedValue();

		if (networkCollectionName == null || networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)) {
			// This is a new network collection, create a root network and a subnetwork, which is a base subnetwork
			subNetwork = (CySubNetwork) netFactory.createNetwork();
		} else {
			// Add a new subNetwork to the given collection
			subNetwork = this.name2RootMap.get(networkCollectionName).addSubNetwork();
		}

		tm.setProgress(0.2);

		final String networkName = namingUtil.getSuggestedNetworkTitle(name);
		subNetwork.getRow(subNetwork).set(CyNetwork.NAME, networkName);

		if (networkCollectionName == null || networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)) {
			// Set the name of new root network
			final CyNetwork rootNetwork = subNetwork.getRootNetwork();
			rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, networkName);
		}

		NetworkViewRenderer nvRenderer = null;
		
		try {
			nvRenderer = renderers.getSelectedValue();
		} catch (ClassCastException cce) {
			tm.showMessage(TaskMonitor.Level.WARN, "Unknown renderer: "+renderers.getSelectedValue()+
			                                       " using default renderer.");
		}

		if (nvRenderer == null)
			nvRenderer = appMgr.getDefaultNetworkViewRenderer();

		final CyNetworkViewFactory netViewFactory = nvRenderer.getNetworkViewFactory();

		tm.setProgress(0.4);
		view = netViewFactory.createNetworkView(subNetwork);
		tm.setProgress(0.6);
		netMgr.addNetwork(subNetwork, false);
		appMgr.setCurrentNetwork(subNetwork);
		tm.setProgress(0.8);
		final VisualStyle style = vmMgr.getCurrentVisualStyle(); // get the current style before registering the view!
		netViewMgr.addNetworkView(view);
		tm.setProgress(0.9);
		applyVisualStyle(style);
		tm.setProgress(1.0);
	}

	@Override
	public void cancel() {
		cancel = true;
	}

	public CyNetworkView getView() {
		return view;
	}

	private void applyVisualStyle(final VisualStyle style) {
		if (style != null) {
			vmMgr.setVisualStyle(style, view);
			style.apply(view);
			view.updateView();
		}
	}

	public HashMap<String, CyRootNetwork> getRootNetworkMap() {
		HashMap<String, CyRootNetwork> name2RootMap = new HashMap<>();

		for (CyNetwork net : netMgr.getNetworkSet()) {
			final CyRootNetwork rootNet = rootNetMgr.getRootNetwork(net);

			if (!name2RootMap.containsValue(rootNet))
				name2RootMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
		}

		return name2RootMap;
	}

	private void sort(final List<String> names) {
		if (!names.isEmpty()) {
			final Collator collator = Collator.getInstance(Locale.getDefault());

			Collections.sort(names, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					if (s1 == null && s2 == null) return 0;
					if (s1 == null) return -1;
					if (s2 == null) return 1;
					return collator.compare(s1, s2);
				}
			});
		}
	}

	public Object getResults(Class type) {
		if (type.equals(CyNetwork.class)) {
			return subNetwork;
		} else if (type.equals(String.class)){
			if (subNetwork == null)
				return "Network not created";
			return "Created new empty network "+subNetwork.toString();
		}  else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (subNetwork == null)
				return "{}";
			else {
				return ""+subNetwork.getSUID();
			}};
			return res;
		}
		return subNetwork;
	}

	public List<Class<?>> getResultClasses() {
		return Arrays.asList(CyNetwork.class, String.class, JSONResult.class);
	}

}
