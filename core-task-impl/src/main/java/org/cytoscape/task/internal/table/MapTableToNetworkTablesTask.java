package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public final class MapTableToNetworkTablesTask extends AbstractTask {

	enum TableType {
		NODE_ATTR("Node Table", CyNode.class),
		EDGE_ATTR("Edge Table", CyEdge.class),
		NETWORK_ATTR("Network Table", CyNetwork.class),
		GLOBAL("Unassigned Tables", CyTable.class);

		private final String name;
		private final  Class<? extends CyIdentifiable> type;

		private TableType(final String name, Class<? extends CyIdentifiable> type) {
			this.name = name;
			this.type = type;
		}

		public Class<? extends CyIdentifiable> getType(){
			return this.type;
		}

		@Override 
		public String toString() {
			return name;
		}
	};

	private static Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	private static String NO_NETWORKS = "No Networks Found";
	
	private final CyTable globalTable;
	private final CyTableReader reader;
	private final boolean byReader;
	private Map<String, CyNetwork> name2NetworkMap;
	
	private final CyServiceRegistrar serviceRegistrar;

	@Tunable(description = "Import Data To:")
	public ListSingleSelection<TableType> dataTypeOptions;

	//******* we couldnt use the enum to check the dependency. So, update this line if you change the GLOBAL enum string 
	@Tunable(description = "Apply to Selected Networks Only",groups="Network Options", dependsOn="dataTypeOptions!=Unassigned Tables", params="displayState=collapsed")
	public boolean selectedNetworksOnly = false;

	@Tunable(description = "Network List",groups="Network Options",dependsOn="selectedNetworksOnly=true", params="displayState=collapsed")
	public ListMultipleSelection<String> networkList;

	@ProvidesTitle
	public String getTitle() {
		return "Import Data ";
	}

	public MapTableToNetworkTablesTask(CyTableReader reader, CyServiceRegistrar serviceRegistrar) {
		this.reader = reader;
		globalTable = null;
		this.byReader = true;
		this.name2NetworkMap = new HashMap<>();
		this.serviceRegistrar = serviceRegistrar;
		
		initTunable();
	}

	public MapTableToNetworkTablesTask(CyTable globalTable, CyServiceRegistrar serviceRegistrar) {
		this.globalTable = globalTable;
		this.byReader = false;
		this.reader = null;
		this.name2NetworkMap = new HashMap<>();
		this.serviceRegistrar = serviceRegistrar;

		initTunable();
	}

	private void initTunable() {
		final List<TableType> options = new ArrayList<>();
		
		for (TableType type : TableType.values())
			options.add(type);
		
		dataTypeOptions = new ListSingleSelection<>(options);
		dataTypeOptions.setSelectedValue(TableType.NODE_ATTR);

		var netManager = serviceRegistrar.getService(CyNetworkManager.class);
		
		for (var net : netManager.getNetworkSet()) {
			String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
			name2NetworkMap.put(netName, net);
		}
		
		List<String> names = new ArrayList<>();
		names.addAll(name2NetworkMap.keySet());
		
		if (names.isEmpty())
			networkList = new ListMultipleSelection<>(NO_NETWORKS);
		else
			networkList = new ListMultipleSelection<>(names);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Map Table to Network Tables");
		TableType tableType = dataTypeOptions.getSelectedValue();
		
		if (tableType == TableType.GLOBAL)
			return;

		if (!selectedNetworksOnly)
			mapTableToDefaultAttrs(tableType);
		else
			mapTableToLocalAttrs(tableType);
	}

	private void mapTableToLocalAttrs(TableType tableType) {
		List<CyNetwork> networks = new ArrayList<>();

		if (!networkList.getSelectedValues().get(0).equals(NO_NETWORKS))
			for (String netName : networkList.getSelectedValues())
				networks.add(name2NetworkMap.get(netName));

		for (CyNetwork network : networks) {
			CyTable targetTable = getTable(network, tableType, CyNetwork.LOCAL_ATTRS);

			if (targetTable != null)
				applyMapping(targetTable);
		}
	}

	private void mapTableToDefaultAttrs(TableType tableType) {
		List<CyRootNetwork> rootNetworkList = new ArrayList<>();
		var netManager = serviceRegistrar.getService(CyNetworkManager.class);
		var rootNetManager = serviceRegistrar.getService(CyRootNetworkManager.class);
		
		for (var net : netManager.getNetworkSet())
			if (!rootNetworkList.contains(rootNetManager.getRootNetwork(net)))
				rootNetworkList.add(rootNetManager.getRootNetwork(net));

		for (var root : rootNetworkList) {
			CyTable targetTable = getTable(root, tableType, CyNetwork.DEFAULT_ATTRS);

			if (targetTable != null)
				applyMapping(targetTable);
		}
	}

	private CyTable getTable(CyNetwork network, TableType tableType, String namespace) {
		if (tableType == TableType.NODE_ATTR)    return network.getTable(CyNode.class, namespace);
		if (tableType == TableType.EDGE_ATTR)    return network.getTable(CyEdge.class, namespace);
		if (tableType == TableType.NETWORK_ATTR) return network.getTable(CyNetwork.class, namespace);

		logger.warn("The selected table type is not valie. \nTable needs to be one of these types: " +TableType.NODE_ATTR +", " + TableType.EDGE_ATTR  + ", "+ TableType.NETWORK_ATTR +" or "+TableType.GLOBAL +".");
		return null;
	}

	private void applyMapping(CyTable targetTable) {
		if (byReader) {
			if (reader.getTables() != null && reader.getTables().length > 0) {
				for (CyTable sourceTable : reader.getTables())
					mapTable(targetTable, sourceTable);
			}
		} else {
			mapTable(targetTable, globalTable);
		}
	}

	private void mapTable(final CyTable localTable, final CyTable globalTable) {
		if (globalTable.getPrimaryKey().getType() != String.class)
			throw new IllegalStateException("Local table's primary key should be type String.");

		final CyColumn trgCol = localTable.getColumn(CyRootNetwork.SHARED_NAME);
		
		if (trgCol != null) {
			localTable.addVirtualColumns(globalTable, CyRootNetwork.SHARED_NAME, false);
			globalTable.setPublic(false);
		} else {
			logger.warn("Name column in the target table was not found.");
		}
	}
}
