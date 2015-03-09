package org.cytoscape.psi_mi.internal.plugin;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class PsiMiTabReader extends AbstractTask implements CyNetworkReader {
	
//	@Tunable(description="Import all columns in the data file")
//	public Boolean importFull;
	
	private InputStream inputStream;

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyLayoutAlgorithmManager layouts;

	private final PsiMiTabParser parser;
	private CyNetwork network;

	private TaskMonitor parentTaskMonitor;
	private final CyProperty<Properties> prop;

	private final CyNetworkFactory cyNetworkFactory;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	/**
	 * If this option is selected, reader should create new CyRootNetwork.
	 */
	public static final String CRERATE_NEW_COLLECTION_STRING = "Create new network collection";

	//******** tunables ********************

	public ListSingleSelection<String> rootNetworkList;
	@Tunable(description = "Network Collection:", gravity = 1.0)
	public ListSingleSelection<String> getRootNetworkList(){
		return rootNetworkList;
	}
	public void setRootNetworkList (ListSingleSelection<String> roots){
		if (rootNetworkList.getSelectedValue().equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			return;
		}
		targetColumnList = getTargetColumns(name2RootMap.get(rootNetworkList.getSelectedValue()));
	}
	
	public ListSingleSelection<String> targetColumnList;
	@Tunable(description = "Node Identifier Mapping Column:", gravity = 2.0, listenForChange={"RootNetworkList"})
	public ListSingleSelection<String> getTargetColumnList(){
		return targetColumnList;
	}
	public void setTargetColumnList(ListSingleSelection<String> colList){
		this.targetColumnList = colList;
	}
	
	private ListSingleSelection<NetworkViewRenderer> rendererList;
	@Tunable(description = "Network View Renderer:", gravity = 3.0)
	public ListSingleSelection<NetworkViewRenderer> getNetworkViewRendererList() {
		return rendererList;
	}
	
	public void setNetworkViewRendererList(final ListSingleSelection<NetworkViewRenderer> rendererList) {
		this.rendererList = rendererList;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Import Network ";
	}

	
	public ListSingleSelection<String> getTargetColumns (CyNetwork network) {
		CyTable selectedTable = network.getTable(CyNode.class, CyRootNetwork.SHARED_ATTRS);
		
		List<String> colNames = new ArrayList<String>();
		for(CyColumn col: selectedTable.getColumns()) {
			// Exclude SUID from the mapping key list
			if (col.getName().equalsIgnoreCase("SUID")){
				continue;
			}
			colNames.add(col.getName());
		}
		
		ListSingleSelection<String> columns = new ListSingleSelection<String>(colNames);
		
		//columns.setSelectedValue("shared name"); this does not work, why
		return columns;
	}

	
	protected HashMap<String, CyRootNetwork> name2RootMap;
	protected Map<Object, CyNode> nMap = new HashMap<Object, CyNode>(10000);

	
	// Return the rootNetwork based on user selection, if not existed yet, create a new one
	private CyRootNetwork getRootNetwork(){
		String networkCollectionName = this.rootNetworkList.getSelectedValue().toString();
		CyRootNetwork rootNetwork = this.name2RootMap.get(networkCollectionName);

		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			CyNetwork newNet = this.cyNetworkFactory.createNetwork();
			return this.cyRootNetworkManager.getRootNetwork(newNet);
		}

		return rootNetwork;
	}
	
	// Build the key-node map for the entire root network
	// Note: The keyColName should start with "shared"
	private void initNodeMap(){	
		
		String networkCollectionName = this.rootNetworkList.getSelectedValue().toString();
		CyRootNetwork rootNetwork = this.name2RootMap.get(networkCollectionName);
		
		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			return;
		}

		String targetKeyColName = this.targetColumnList.getSelectedValue();
		
		if (rootNetwork == null){
			return;
		}
		
		Iterator<CyNode> it = rootNetwork.getNodeList().iterator();
		
		while (it.hasNext()){
			CyNode node = it.next();
			Object keyValue =  rootNetwork.getRow(node).getRaw(targetKeyColName);
			this.nMap.put(keyValue, node);				
		}
	}

	
	private static HashMap<String, CyRootNetwork> getRootNetworkMap(CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager) {

		HashMap<String, CyRootNetwork> name2RootMap = new HashMap<String, CyRootNetwork>();

		for (CyNetwork net : cyNetworkManager.getNetworkSet()){
			final CyRootNetwork rootNet = cyRootNetworkManager.getRootNetwork(net);
			if (!name2RootMap.containsValue(rootNet ) )
				name2RootMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
		}

		return name2RootMap;
	}
	
	private CyNetworkViewFactory getNetworkViewFactory() {
		if (rendererList != null && rendererList.getSelectedValue() != null)
			return rendererList.getSelectedValue().getNetworkViewFactory();
		
		return cyNetworkViewFactory;
	}

	
	public PsiMiTabReader(
			final InputStream is,
			final CyApplicationManager cyApplicationManager,
			final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory,
			final CyLayoutAlgorithmManager layouts,
			final CyProperty<Properties> prop, 
			final CyNetworkManager cyNetworkManager,
			final CyRootNetworkManager cyRootNetworkManager
	) {
		if (is == null)
			throw new NullPointerException("Input stream is null");
		
		this.inputStream = is;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.layouts = layouts;
		this.prop = prop;

		this.cyNetworkFactory = cyNetworkFactory;
		this.cyRootNetworkManager = cyRootNetworkManager;
		
		// initialize the network Collection
		this.name2RootMap = getRootNetworkMap(cyNetworkManager, cyRootNetworkManager);
		
		List<String> rootNames = new ArrayList<>();
		rootNames.add(CRERATE_NEW_COLLECTION_STRING);
		rootNames.addAll(name2RootMap.keySet());
		rootNetworkList = new ListSingleSelection<>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));
		
		// initialize target attribute list
		List<String> colNames_target = new ArrayList<>();
		colNames_target.add("shared name");
		this.targetColumnList = new ListSingleSelection<String>(colNames_target);
		targetColumnList.setSelectedValue("shared name");
		
		// initialize renderer list
		final List<NetworkViewRenderer> renderers = new ArrayList<>();
		
		final Set<NetworkViewRenderer> rendererSet = cyApplicationManager.getNetworkViewRendererSet();
		
		// If there is only one registered renderer, we don't want to add it to the List Selection,
		// so the combo-box does not appear to the user, since there is nothing to select anyway.
		if (rendererSet.size() > 1) {
			renderers.addAll(rendererSet);
			Collections.sort(renderers, new Comparator<NetworkViewRenderer>() {
				@Override
				public int compare(NetworkViewRenderer r1, NetworkViewRenderer r2) {
					return r1.toString().compareToIgnoreCase(r2.toString());
				}
			});
		}
		
		rendererList = new ListSingleSelection<>(renderers);
		
		parser = new PsiMiTabParser(is);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.parentTaskMonitor = taskMonitor;
		
		// support to add network into existing collection
		this.initNodeMap();
		CyRootNetwork rootNetwork = getRootNetwork();
		
		parser.setRootNetwork(rootNetwork);
		parser.setNodeMap(this.nMap);
		
		try {
			createNetwork(taskMonitor);
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	private void createNetwork(TaskMonitor taskMonitor) throws IOException {
		taskMonitor.setTitle("Loading PSIMI-TAB File");
		taskMonitor.setStatusMessage("Loading PSI-MI-TAB25 file.");
		taskMonitor.setProgress(0.01d);

		network = parser.parse(taskMonitor);

		taskMonitor.setProgress(1.0d);
	}

	@Override
	public CyNetwork[] getNetworks() {
		return new CyNetwork[] { network };
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		if (cancelled) {
			if(network != null) {
				network.dispose();
				network = null;
			}
			throw new RuntimeException("Network loading canceled by user.");
		}
		
		final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);

		String pref = CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME;
		if (prop != null)
			pref = prop.getProperties().getProperty("preferredLayoutAlgorithm", pref);

		final CyLayoutAlgorithm layout = layouts.getLayout(pref);
		// Force to run this task here to avoid concurrency problem.
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		Task nextTask = itr.next();
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		return view;
	}

	@Override
	public void cancel() {
		super.cancel();
		parser.cancel();
	}
}
