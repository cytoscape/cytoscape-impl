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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.psi_mi.internal.data_mapper.PSIMI25EntryMapper;
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

import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.EntrySet;

public class PSIMI25XMLNetworkViewReader extends AbstractTask implements CyNetworkReader {
	
	private static final Logger logger = LoggerFactory.getLogger(PSIMI25XMLNetworkViewReader.class);
	
	
	private final CyNetworkViewFactory networkViewFactory;
	
	private InputStream inputStream;
	private CyNetwork network;

	private CyLayoutAlgorithmManager layouts;
	
	private TaskMonitor parentTaskMonitor;
	
	private PSIMI25EntryMapper mapper;
	
	private boolean cancelFlag = false;
	private final CyNetworkManager cyNetworkManager;

	private final CyNetworkFactory cyNetworkFactory;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	/**
	 * If this option is selected, reader should create new CyRootNetwork.
	 */
	public static final String CRERATE_NEW_COLLECTION_STRING ="Create new network collection";

	//******** tunables ********************

	public ListSingleSelection<String> rootNetworkList;
	@Tunable(description = "Network Collection" ,groups=" ")
	public ListSingleSelection<String> getRootNetworkList(){
		return rootNetworkList;
	}
	public void setRootNetworkList (ListSingleSelection<String> roots){
		if (rootNetworkList.getSelectedValue().equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			return;
		}
		targetColumnList = getTargetColumns(name2RootMap.get(rootNetworkList.getSelectedValue()));
	}

	public ListSingleSelection<String> sourceColumnList;
	@Tunable(description = "Mapping Column for New Network:", groups=" ")
	public ListSingleSelection<String> getSourceColumnList(){
		return sourceColumnList;
	}
	public void setSourceColumnList(ListSingleSelection<String> colList){
		this.sourceColumnList = colList;
	}
	
	public ListSingleSelection<String> targetColumnList;
	@Tunable(description = "Mapping Column for Existing Network:",groups=" ", listenForChange={"RootNetworkList"})
	public ListSingleSelection<String> getTargetColumnList(){
		return targetColumnList;
	}
	public void setTargetColumnList(ListSingleSelection<String> colList){
		this.targetColumnList = colList;
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


	
	public PSIMI25XMLNetworkViewReader(InputStream inputStream, CyNetworkFactory networkFactory, CyNetworkViewFactory networkViewFactory, 
			CyLayoutAlgorithmManager layouts, final CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager) {
		this.inputStream = inputStream;
		this.cyNetworkFactory = networkFactory;
		this.networkViewFactory = networkViewFactory;
		this.layouts = layouts;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		
		// initialize the network Collection
		this.name2RootMap = getRootNetworkMap(cyNetworkManager, cyRootNetworkManager);
		
		List<String> rootNames = new ArrayList<String>();
		rootNames.add(CRERATE_NEW_COLLECTION_STRING);
		rootNames.addAll(name2RootMap.keySet());
		rootNetworkList = new ListSingleSelection<String>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));
		
		// initialize target attribute list
		List<String> colNames_target = new ArrayList<String>();
		colNames_target.add("shared name");
		this.targetColumnList = new ListSingleSelection<String>(colNames_target);
		targetColumnList.setSelectedValue("shared name");
	
		// initialize source attribute list
		List<String> colNames_source = new ArrayList<String>();
		colNames_source.add("shared name");
		this.sourceColumnList = new ListSingleSelection<String>(colNames_source);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		parentTaskMonitor = taskMonitor;
		long start = System.currentTimeMillis();
		
		taskMonitor.setProgress(0.01d);
		taskMonitor.setTitle("Loading PSI-MI 2.5.x XML File ");
		taskMonitor.setStatusMessage("Loading data file in PSI-MI 2.5 XML format.");

		// support to add network into existing collection
		this.initNodeMap();
		
		PsimiXmlReader reader = new PsimiXmlReader();
		EntrySet result = reader.read(inputStream);		
		taskMonitor.setProgress(0.4d);
		taskMonitor.setStatusMessage("Data Loaded.  Mapping Data to Network...");

		if(cancelFlag) {
			inputStream.close();
			reader = null;
			result = null;
			return;
		}
		
		//network = cyNetworkFactory.createNetwork();
		String networkCollectionName =  this.rootNetworkList.getSelectedValue().toString();
		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			// This is a new network collection, create a root network and a subnetwork, which is a base subnetwork
			network = cyNetworkFactory.createNetwork();
		}
		else {
			// Add a new subNetwork to the given collection
			network = this.name2RootMap.get(networkCollectionName).addSubNetwork();
		}
		
		
		mapper = new PSIMI25EntryMapper(network, result);
		mapper.map();
		
		taskMonitor.setProgress(1.0d);
		logger.info("PSI-MI XML Data Import finihsed in " + (System.currentTimeMillis() - start) + " msec.");
	}

	@Override
	public CyNetwork[] getNetworks() {
		return new CyNetwork[] { network };
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		final CyNetworkView view = networkViewFactory.createNetworkView(network);
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
	
	@Override
	public void cancel() {
		cancelFlag = true;
		mapper.cancel();
	}
}
