package org.cytoscape.task.internal.creation;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


/**
 * Create an empty network with view.
 */
public class NewEmptyNetworkTask extends AbstractTask {

	private final CyNetworkNaming namingUtil; 
	private final CyNetworkViewManager networkViewManager;
	private final VisualMappingManager vmm;
	private final CyApplicationManager cyApplicationManager;
	
	private boolean cancel;
	private CyNetworkView view;

	//
	public static final String CRERATE_NEW_COLLECTION_STRING ="Create new network collection";

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	protected final CyNetworkManager cyNetworkManager;
	protected final CyRootNetworkManager cyRootNetworkManager;

	//******** tunables ********************

	public ListSingleSelection<String> rootNetworkList;
	@Tunable(description = "Network Collection" ,groups=" ", gravity=1.0)
	public ListSingleSelection<String> getRootNetworkList(){
		return rootNetworkList;
	}
	public void setRootNetworkList (ListSingleSelection<String> roots){
		if (rootNetworkList.getSelectedValue().equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			// set default
			List<String> colNames = new ArrayList<String>();
			colNames.add("shared name");
			targetColumnList = new ListSingleSelection<String>(colNames);
			return;
		}
		targetColumnList = getTargetColumns(name2RootMap.get(rootNetworkList.getSelectedValue()));
	}
	
	public ListSingleSelection<String> targetColumnList;
	@Tunable(description = "Node Identifier Mapping Column:",groups=" ", listenForChange={"RootNetworkList"}, gravity=2.0)
	public ListSingleSelection<String> getTargetColumnList(){
		return targetColumnList;
	}
	public void setTargetColumnList(ListSingleSelection<String> colList){
		this.targetColumnList = colList;

		// looks like this does not have any effect, is this a bug?
		this.targetColumnList.setSelectedValue("shared name");
	}

	@Tunable(description = "Name of network: ", groups=" ", gravity=3.0)
	public String name = "Network";
	
	
	@ProvidesTitle
	public String getTitle() {
		return "Create New Network ";
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

	public NewEmptyNetworkTask(CyNetworkFactory cnf, CyNetworkViewFactory cnvf, CyNetworkManager netmgr,
				   final CyNetworkViewManager networkViewManager, final CyNetworkNaming namingUtil,
				   final VisualMappingManager vmm, final CyRootNetworkManager cyRootNetworkManager, final CyApplicationManager cyApplicationManager) {
		this.cyNetworkManager = netmgr;
		this.networkViewManager = networkViewManager;
		this.cyNetworkFactory = cnf;
		this.cyNetworkViewFactory = cnvf;
		this.namingUtil = namingUtil;
		this.vmm = vmm;
		this.cyRootNetworkManager = cyRootNetworkManager;
		this.cyApplicationManager = cyApplicationManager;
		
		// initialize the network Collection
		this.name2RootMap = getRootNetworkMap(this.cyNetworkManager, this.cyRootNetworkManager);
		
		List<String> rootNames = new ArrayList<String>();
		rootNames.add(CRERATE_NEW_COLLECTION_STRING);
		rootNames.addAll(name2RootMap.keySet());
		rootNetworkList = new ListSingleSelection<String>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));
		
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

		// initialize target attribute list
		List<String> colNames_target = new ArrayList<String>();
		colNames_target.add("shared name");
		this.targetColumnList = new ListSingleSelection<String>(colNames_target);
	}

	public void run(final TaskMonitor tm) {
		tm.setProgress(0.0);
		
		final String networkCollectionName =  this.rootNetworkList.getSelectedValue().toString();
		final CySubNetwork subNetwork;
		
		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			// This is a new network collection, create a root network and a subnetwork, which is a base subnetwork
			subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
		} else {
			// Add a new subNetwork to the given collection
			subNetwork = this.name2RootMap.get(networkCollectionName).addSubNetwork();
		}

		tm.setProgress(0.2);
		
		final String networkName = namingUtil.getSuggestedNetworkTitle(name);
		subNetwork.getRow(subNetwork).set(CyNetwork.NAME, networkName);

		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			// Set the name of new root network
			final CyNetwork rootNetwork = subNetwork.getRootNetwork();
			rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, networkName);
		}
		
		tm.setProgress(0.4);
		view = cyNetworkViewFactory.createNetworkView(subNetwork);		
		tm.setProgress(0.6);
		cyNetworkManager.addNetwork(subNetwork);
		tm.setProgress(0.8);
		final VisualStyle style = vmm.getCurrentVisualStyle(); // get the current style before registering the view!
		networkViewManager.addNetworkView(view);
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
			vmm.setVisualStyle(style, view);
			style.apply(view);
			view.updateView();
		}
	}
	
	public static HashMap<String, CyRootNetwork> getRootNetworkMap(CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager) {

		HashMap<String, CyRootNetwork> name2RootMap = new HashMap<String, CyRootNetwork>();

		for (CyNetwork net : cyNetworkManager.getNetworkSet()){
			final CyRootNetwork rootNet = cyRootNetworkManager.getRootNetwork(net);
			if (!name2RootMap.containsValue(rootNet ) )
				name2RootMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
		}

		return name2RootMap;
	}

}
