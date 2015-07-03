package org.cytoscape.tableimport.internal.task;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class NetworkCollectionHelper extends AbstractTask {

	private LoadNetworkReaderTask importTask;
	private final CyServiceRegistrar serviceRegistrar;
	
	/**
	 * If this option is selected, reader should create new CyRootNetwork.
	 */
	public static final String CRERATE_NEW_COLLECTION_STRING ="Create new network collection";
	
	protected HashMap<String, CyRootNetwork> name2RootMap;
	protected Map<Object, CyNode> nMap = new HashMap<>(10000);

	//******** tunables ********************

	public ListSingleSelection<String> rootNetworkList;
	@Tunable(description = "Network Collection:", groups="Network", gravity=1.0)
	public ListSingleSelection<String> getRootNetworkList(){
		return rootNetworkList;
	}
	public void setRootNetworkList (ListSingleSelection<String> roots){
		final String rootNetName = rootNetworkList.getSelectedValue();
		
		if (rootNetName != null && !rootNetName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING))
			targetColumnList = getTargetColumns(name2RootMap.get(rootNetName));
		else
			targetColumnList = new ListSingleSelection<>();
	}
	
	public ListSingleSelection<String> targetColumnList;
	@Tunable(description = "Node Identifier Mapping Column:", groups="Network", gravity=2.0, listenForChange={"RootNetworkList"})
	public ListSingleSelection<String> getTargetColumnList(){
		return targetColumnList;
	}
	public void setTargetColumnList(ListSingleSelection<String> colList){
		this.targetColumnList = colList;
	}
	
	public ListSingleSelection<NetworkViewRenderer> rendererList;
	@Tunable(description = "Network View Renderer:", groups="Network", gravity=3.0)
	public ListSingleSelection<NetworkViewRenderer> getNetworkViewRendererList() {
		return rendererList;
	}
	
	public void setNetworkViewRendererList(final ListSingleSelection<NetworkViewRenderer> rendererList) {
		this.rendererList = rendererList;
	}
	
	public ListSingleSelection<String> getTargetColumns(final CyNetwork network) {
		final CyTable selectedTable = network.getTable(CyNode.class, CyRootNetwork.SHARED_ATTRS);
		final List<String> colNames = new ArrayList<>();
		
		for (CyColumn col : selectedTable.getColumns()) {
			// Exclude SUID from the mapping key list
			if (!col.getName().equalsIgnoreCase(CyIdentifiable.SUID) && !col.getName().endsWith(".SUID") &&
					(col.getType() == String.class || col.getType() == Integer.class || col.getType() == Long.class))
				colNames.add(col.getName());
		}
		
		sort(colNames);
		final ListSingleSelection<String> columns = new ListSingleSelection<>(colNames);
		
		return columns;
	}

	public NetworkCollectionHelper(final CyServiceRegistrar serviceRegistrar) {
		this(null, serviceRegistrar);
	}

	public NetworkCollectionHelper(final LoadNetworkReaderTask importTask, final CyServiceRegistrar serviceRegistrar) {
		this.importTask = importTask;
		this.serviceRegistrar = serviceRegistrar;
		initTunables();
	}
	
	void initTunables() {
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

		// initialize target attribute list
		targetColumnList = new ListSingleSelection<>();
		
		// initialize renderer list
		final List<NetworkViewRenderer> renderers = new ArrayList<>();
		final Set<NetworkViewRenderer> rendererSet =
				serviceRegistrar.getService(CyApplicationManager.class).getNetworkViewRendererSet();
		
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
	
	// Return the rootNetwork based on user selection, if not existed yet, create a new one
	public CyRootNetwork getRootNetwork() {
		final CyRootNetwork rootNetwork;
		final String rootNetName = rootNetworkList.getSelectedValue();
		
		if (rootNetName == null || rootNetName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)) {
			final CyNetwork newNet = serviceRegistrar.getService(CyNetworkFactory.class).createNetwork();
			rootNetwork = serviceRegistrar.getService(CyRootNetworkManager.class).getRootNetwork(newNet);
		} else {
			rootNetwork = name2RootMap.get(rootNetName);
		}

		return rootNetwork;
	}
	
	public CyNetworkViewFactory getNetworkViewFactory() {
		if (rendererList != null && rendererList.getSelectedValue() != null)
			return rendererList.getSelectedValue().getNetworkViewFactory();
		
		return serviceRegistrar.getService(CyApplicationManager.class).getDefaultNetworkViewRenderer()
				.getNetworkViewFactory();
	}
	
	// Build the key-node map for the entire root network
	// Note: The keyColName should start with "shared"
	protected void initNodeMap() {	
		final String rootNetName = rootNetworkList.getSelectedValue();
		
		if (rootNetName == null || rootNetName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING))
			return;

		String targetKeyColName = targetColumnList.getSelectedValue();
		
		if (targetKeyColName == null)
			targetKeyColName = CyRootNetwork.SHARED_NAME;
		
		final CyRootNetwork rootNetwork = name2RootMap.get(rootNetName);
		
		if (rootNetwork == null)
			return;
		
		final Iterator<CyNode> it = rootNetwork.getNodeList().iterator();
		
		while (it.hasNext()) {
			CyNode node = it.next();
			Object keyValue = rootNetwork.getRow(node).getRaw(targetKeyColName);
			nMap.put(keyValue, node);
		}
	}

	private HashMap<String, CyRootNetwork> getRootNetworkMap() {
		final HashMap<String, CyRootNetwork> name2RootMap = new HashMap<>();
		final Set<CyNetwork> networkSet = serviceRegistrar.getService(CyNetworkManager.class).getNetworkSet();
		final CyRootNetworkManager rootNetworkManager = serviceRegistrar.getService(CyRootNetworkManager.class);

		for (CyNetwork net : networkSet) {
			final CyRootNetwork rootNet = rootNetworkManager.getRootNetwork(net);

			if (!name2RootMap.containsValue(rootNet))
				name2RootMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
		}

		return name2RootMap;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		this.initNodeMap();
		
		if (importTask != null) {
			importTask.setNodeMap(getNodeMap());
			importTask.setRootNetwork(getRootNetwork());
			importTask.setNetworkViewFactory(getNetworkViewFactory());
		}
	}
	
	public Map<Object, CyNode> getNodeMap(){
		return this.nMap;
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
}
