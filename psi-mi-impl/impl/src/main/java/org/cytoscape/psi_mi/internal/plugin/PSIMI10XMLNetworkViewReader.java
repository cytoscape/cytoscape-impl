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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.cytoscape.psi_mi.internal.cyto_mapper.MapToCytoscape;
import org.cytoscape.psi_mi.internal.data_mapper.MapPsiOneToInteractions;
import org.cytoscape.psi_mi.internal.model.Interaction;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PSIMI10XMLNetworkViewReader extends AbstractTask implements CyNetworkReader {
	
	private static final Logger logger = LoggerFactory.getLogger(PSIMI10XMLNetworkViewReader.class);
	
	private static final int BUFFER_SIZE = 16384;

	private final CyNetworkViewFactory networkViewFactory;
	private final CyNetworkFactory networkFactory;
	
	private InputStream inputStream;
	private CyNetwork network;

	private CyLayoutAlgorithmManager layouts;
	
	private TaskMonitor parentTaskMonitor;
	private final CyNetworkManager cyNetworkManager;
	
	
	//private final CyNetworkFactory cyNetworkFactory;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	protected HashMap<String, CyRootNetwork> name2RootMap;
	protected Map<Object, CyNode> nMap = new HashMap<>(10000);
	
	/**
	 * If this option is selected, reader should create new CyRootNetwork.
	 */
	public static final String CRERATE_NEW_COLLECTION_STRING = "-- Create new network collection --";

	//******** tunables ********************

	public ListSingleSelection<String> rootNetworkList;
	@Tunable(description = "Network Collection:", gravity = 1.0)
	public ListSingleSelection<String> getRootNetworkList() {
		return rootNetworkList;
	}
	public void setRootNetworkList (ListSingleSelection<String> roots){
		if (rootNetworkList.getSelectedValue().equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)) {
			return;
		}
		targetColumnList = getTargetColumns(name2RootMap.get(rootNetworkList.getSelectedValue()));
	}
	
	public ListSingleSelection<String> targetColumnList;
	@Tunable(description = "Node Identifier Mapping Column:", gravity = 2.0, listenForChange={"RootNetworkList"})
	public ListSingleSelection<String> getTargetColumnList() {
		return targetColumnList;
	}
	public void setTargetColumnList(ListSingleSelection<String> colList) {
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
		
		List<String> colNames = new ArrayList<>();
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

	// Return the rootNetwork based on user selection, if not existed yet, create a new one
	private CyRootNetwork getRootNetwork(){
		String networkCollectionName = this.rootNetworkList.getSelectedValue().toString();
		CyRootNetwork rootNetwork = this.name2RootMap.get(networkCollectionName);

		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			CyNetwork newNet = this.networkFactory.createNetwork();
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
	
	private static HashMap<String, CyRootNetwork> getRootNetworkMap(CyNetworkManager cyNetworkManager,
			CyRootNetworkManager cyRootNetworkManager) {
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
		
		return networkViewFactory;
	}
	
	public PSIMI10XMLNetworkViewReader(
			final InputStream inputStream,
			final CyApplicationManager cyApplicationManager,
			final CyNetworkFactory networkFactory, 
			final CyNetworkViewFactory networkViewFactory,
			final CyLayoutAlgorithmManager layouts, 
			final CyNetworkManager cyNetworkManager,
			final CyRootNetworkManager cyRootNetworkManager
		) {
		this.inputStream = inputStream;
		this.networkFactory = networkFactory;
		this.networkViewFactory = networkViewFactory;
		this.layouts = layouts;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		
		// initialize the network Collection
		this.name2RootMap = getRootNetworkMap(this.cyNetworkManager, this.cyRootNetworkManager);
		
		List<String> rootNames = new ArrayList<String>();
		rootNames.add(CRERATE_NEW_COLLECTION_STRING);
		rootNames.addAll(name2RootMap.keySet());
		rootNetworkList = new ListSingleSelection<String>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));
		
//		if (!SessionUtil.isReadingSessionFile()) {
//			final List<CyNetwork> selectedNetworks = cyApplicationManager.getSelectedNetworks();
//			
//			if (selectedNetworks != null && selectedNetworks.size() > 0){
//				CyNetwork selectedNetwork = this.cyApplicationManager.getSelectedNetworks().get(0);
//				String rootName = "";
//				if (selectedNetwork instanceof CySubNetwork){
//					CySubNetwork subnet = (CySubNetwork) selectedNetwork;
//					CyRootNetwork rootNet = subnet.getRootNetwork();
//					rootName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
//				} else {
//					// it is a root network
//					rootName = selectedNetwork.getRow(selectedNetwork).get(CyNetwork.NAME, String.class);
//				}
//				
//				rootNetworkList.setSelectedValue(rootName);
//			}
//		}
		
		// initialize target attribute list
		List<String> colNames_target = new ArrayList<String>();
		colNames_target.add("shared name");
		this.targetColumnList = new ListSingleSelection<String>(colNames_target);
		
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
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		parentTaskMonitor = taskMonitor;
		long start = System.currentTimeMillis();
		
		// support to add network into existing collection
		this.initNodeMap();
		
		taskMonitor.setStatusMessage("Loading PSI-MI 1.x XML file...");
		taskMonitor.setProgress(0.05d);
		String xml = readString(inputStream);

		List<Interaction> interactions = new ArrayList<Interaction>();

		final MapPsiOneToInteractions mapper = new MapPsiOneToInteractions(xml, interactions);
		mapper.doMapping();
		
		taskMonitor.setProgress(0.4d);

		//  Now map to Cytoscape network objects.
		//network = networkFactory.createNetwork();
		String networkCollectionName =  this.rootNetworkList.getSelectedValue().toString();
		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			// This is a new network collection, create a root network and a subnetwork, which is a base subnetwork
			network = networkFactory.createNetwork();
		}
		else {
			// Add a new subNetwork to the given collection
			network = this.name2RootMap.get(networkCollectionName).addSubNetwork();
		}
		
		final MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.SPOKE_VIEW);
		mapper2.doMapping();

		taskMonitor.setProgress(1.0d);
		logger.info("PSI-MI XML Data Import finihsed in " + (System.currentTimeMillis() - start) + " msec.");
	}


	/**
	 * Create big String object from the entire XML file
	 * TODO: is this OK for huge data files?
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	private static String readString(InputStream source) throws IOException {
		final StringWriter writer = new StringWriter();
	
		final BufferedReader reader = new BufferedReader(new InputStreamReader(source));
		try {
			char[] buffer = new char[BUFFER_SIZE];
			int charactersRead = reader.read(buffer, 0, buffer.length);
			while (charactersRead != -1) {
				writer.write(buffer, 0, charactersRead);
				charactersRead = reader.read(buffer, 0, buffer.length);
			}
		} finally {
			reader.close();
		}
		return writer.toString();
	}

	@Override
	public CyNetwork[] getNetworks() {
		return new CyNetwork[] { network };
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);
		final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS,"");
		Task nextTask = itr.next();
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		parentTaskMonitor.setProgress(1.0d);
		return view;		
	}
}
