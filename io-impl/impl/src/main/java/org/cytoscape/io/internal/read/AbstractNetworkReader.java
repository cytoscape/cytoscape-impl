package org.cytoscape.io.internal.read;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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


//import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


public abstract class AbstractNetworkReader extends AbstractTask implements CyNetworkReader {
	
	/**
	 * If this option is selected, reader should create new CyRootNetwork.
	 */
	public static final String CREATE_NEW_COLLECTION_STRING ="Create new network collection";

	protected CyNetwork[] cyNetworks;

	protected VisualStyle[] visualstyles;
	protected InputStream inputStream;

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	protected final CyNetworkManager cyNetworkManager;;
	protected final CyRootNetworkManager cyRootNetworkManager;

	//******** tunables ********************

	public ListSingleSelection<String> rootNetworkList;
	@Tunable(description = "Network Collection" ,groups=" ")
	public ListSingleSelection<String> getRootNetworkList(){
		return rootNetworkList;
	}
	public void setRootNetworkList (ListSingleSelection<String> roots){
		if (rootNetworkList.getSelectedValue().equalsIgnoreCase(CREATE_NEW_COLLECTION_STRING)){
			// set default
			List<String> colNames = new ArrayList<String>();
			colNames.add("shared name");
			targetColumnList = new ListSingleSelection<String>(colNames);
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

		// looks like this does not have any effect, is this a bug?
		this.targetColumnList.setSelectedValue("shared name");
	}
	
	
	@ProvidesTitle
	public String getTitle() {
		return "Import Network ";
	}

	
	public ListSingleSelection<String> getTargetColumns (CyNetwork network) {
		CyTable selectedTable = network.getTable(CyNode.class, CyRootNetwork.SHARED_ATTRS);

		List<String> colNames = new ArrayList<String>();
		
		// Work-around to make the "shared name" the first in the list
		boolean containSharedName = false;
		// check if "shared name" column exist
		if (CyTableUtil.getColumnNames(selectedTable).contains("shared name")){
			containSharedName = true;
			colNames.add("shared name");
		}
		
		for(CyColumn col: selectedTable.getColumns()) {
			// Exclude SUID from the mapping key list
			if (col.getName().equalsIgnoreCase("SUID")){
				continue;
			}
			
			if (col.getName().equalsIgnoreCase("shared name") && containSharedName){
				// "shared name" is already added in the first
				continue;
			}
			colNames.add(col.getName());
		}
		
		ListSingleSelection<String> columns = new ListSingleSelection<String>(colNames);
		
		return columns;
	}

	
	protected HashMap<String, CyRootNetwork> name2RootMap;
	protected Map<Object, CyNode> nMap = new HashMap<Object, CyNode>(10000);

	protected CyApplicationManager cyApplicationManager;
	
	public AbstractNetworkReader(InputStream inputStream, final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager, final CyRootNetworkManager cyRootNetworkManager,
			CyApplicationManager cyApplicationManager) {
		if (inputStream == null)
			throw new NullPointerException("Input stream is null");
		if (cyNetworkViewFactory == null)
			throw new NullPointerException("CyNetworkViewFactory is null");
		if (cyNetworkFactory == null)
			throw new NullPointerException("CyNetworkFactory is null");

		this.inputStream = inputStream;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		this.cyApplicationManager = cyApplicationManager;
		
		// initialize the network Collection
		this.name2RootMap = ReadUtils.getRootNetworkMap(this.cyNetworkManager, this.cyRootNetworkManager);
		
		List<String> rootNames = new ArrayList<String>();
		rootNames.add(CREATE_NEW_COLLECTION_STRING);
		rootNames.addAll(name2RootMap.keySet());
		rootNetworkList = new ListSingleSelection<String>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));
		
		if (!SessionUtil.isReadingSessionFile()) {
			final List<CyNetwork> selectedNetworks = cyApplicationManager.getSelectedNetworks();
			
			if (selectedNetworks != null && selectedNetworks.size() > 0){
				CyNetwork selectedNetwork = this.cyApplicationManager.getSelectedNetworks().get(0);
				String rootName = "";
				if (selectedNetwork instanceof CySubNetwork){
					CySubNetwork subnet = (CySubNetwork) selectedNetwork;
					CyRootNetwork rootNet = subnet.getRootNetwork();
					rootName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
				} else {
					// it is a root network
					rootName = selectedNetwork.getRow(selectedNetwork).get(CyNetwork.NAME, String.class);
				}
				
				rootNetworkList.setSelectedValue(rootName);
			}
		}
		
		// initialize target attribute list
		List<String> colNames_target = new ArrayList<String>();
		colNames_target.add("shared name");
		this.targetColumnList = new ListSingleSelection<String>(colNames_target);
	
		// initialize source attribute list
		List<String> colNames_source = new ArrayList<String>();
		colNames_source.add("shared name");
		this.sourceColumnList = new ListSingleSelection<String>(colNames_source);
	
	}

	@Override
	public CyNetwork[] getNetworks() {
		return cyNetworks;
	}
	
	// Return the rootNetwork based on user selection, if not existed yet, create a new one
	protected CyRootNetwork getRootNetwork(){
		String networkCollectionName = this.rootNetworkList.getSelectedValue().toString();
		CyRootNetwork rootNetwork = this.name2RootMap.get(networkCollectionName);

		if (networkCollectionName.equalsIgnoreCase(CREATE_NEW_COLLECTION_STRING)){
			CyNetwork newNet = this.cyNetworkFactory.createNetwork();
			return this.cyRootNetworkManager.getRootNetwork(newNet);
		}

		return rootNetwork;
	}
	
	// Build the key-node map for the entire root network
	// Note: The keyColName should start with "shared"
	protected void initNodeMap(){	
		
		String networkCollectionName = this.rootNetworkList.getSelectedValue().toString();
		CyRootNetwork rootNetwork = this.name2RootMap.get(networkCollectionName);
		
		if (networkCollectionName.equalsIgnoreCase(CREATE_NEW_COLLECTION_STRING)){
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
	
	// The following is replaced by the above one, it should be removed late
	
	// Build the key-node map for the entire root network
	protected void initNodeMap(CyRootNetwork rootNetwork, String keyColName){	
		// Note: The keyColName should start with "shared"
		
		if (rootNetwork == null){
			return;
		}
		
		Iterator<CyNode> it = rootNetwork.getNodeList().iterator();
		
		while (it.hasNext()){
			CyNode node = it.next();
			Object keyValue =  rootNetwork.getRow(node).getRaw(keyColName);
			this.nMap.put(keyValue, node);				
		}
	}
}
