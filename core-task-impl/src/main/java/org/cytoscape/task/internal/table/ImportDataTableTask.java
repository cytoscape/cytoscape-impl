package org.cytoscape.task.internal.table;

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

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportDataTableTask extends AbstractTask {
	
	enum TableType {
		NODE_ATTR("Node Table Columns", CyNode.class),
		EDGE_ATTR("Edge Table Columns", CyEdge.class),
		NETWORK_ATTR("Network Table Columns", CyNetwork.class);

		private final String name;
		private final Class<? extends CyIdentifiable> type;

		private TableType(final String name, Class<? extends CyIdentifiable> type) {
			this.name = name;
			this.type = type;
		}

		public Class<? extends CyIdentifiable> getType() {
			return this.type;
		}

		@Override
		public String toString() {
			return name;
		}
	};

	private static final Logger logger = LoggerFactory.getLogger(MapTableToNetworkTablesTask.class);
	private static final String NO_NETWORKS = "No Networks Found";
	
	private boolean byReader;
	private CyTableReader reader;
	private CyTable globalTable;
	private CyRootNetworkManager rootNetworkManager;
	private CyNetworkManager networkManager;
	private Map<String, CyNetwork> name2NetworkMap;
	private Map<String, CyRootNetwork> name2RootMap;
	private Map<String, String> source2targetColumnMap;
	
	
	public ListSingleSelection<String> importTypeChooser ;
	@Tunable(description="Where to import Table Data",gravity=1.0, groups={"Target Network Data"}, xorChildren=true)
	
	public ListSingleSelection<String> getImportTypeChooser() {
		return importTypeChooser;
	}

	public void setImportTypeChooser(ListSingleSelection<String> chooser) {
		this.importTypeChooser = chooser;
	}

	public ListSingleSelection<String> networkCollectionsList;
	@Tunable(description = "Network Collection", groups = {"Target Network Data","Select a Network Collection"},gravity=2.0,  xorKey="To a Network Collection")
	public ListSingleSelection<String> getNetworkCollectionsList() {
		return networkCollectionsList;
	}

	public void setNetworkCollectionsList(ListSingleSelection<String> roots) {
		ListSingleSelection<String> tempList = getColumns(name2RootMap.get(networkCollectionsList.getSelectedValue()),
				dataTypeOptions.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
		if(!keyColumnForMapping.getPossibleValues().containsAll(tempList.getPossibleValues())
				|| keyColumnForMapping.getPossibleValues().size() != tempList.getPossibleValues().size())
			keyColumnForMapping = tempList;
	}

	public ListSingleSelection<String> keyColumnForMapping;
	@Tunable(description = "Key Column for Network:", groups = {"Target Network Data","Select a Network Collection"},gravity=3.0, xorKey="To a Network Collection", listenForChange = {
			"DataTypeOptions", "RootNetworkList" })
	public ListSingleSelection<String> getKeyColumnForMapping() {
		return keyColumnForMapping;
	}

	public void setKeyColumnForMapping(ListSingleSelection<String> colList) {
		this.keyColumnForMapping = colList;
	}

	public ListMultipleSelection<String> networkList;
	@Tunable(description = "Network List", groups = {"Target Network Data","Select Networks"},gravity=4.0, xorKey="To selected networks only", params = "displayState=collapsed")
	public ListMultipleSelection<String> getNetworkList() {
		return networkList;
	}

	public void setNetworkList(ListMultipleSelection<String> list) {
		this.networkList = list;
	}
	
	public ListSingleSelection<TableType> dataTypeOptions;

	@Tunable(description = "Import Data as:", gravity=5.0, groups={"Importing Type"})
	public ListSingleSelection<TableType> getDataTypeOptions() {
		return dataTypeOptions;
	}

	public void setDataTypeOptions(ListSingleSelection<TableType> options) {
		ListSingleSelection<String> tempList = getColumns(name2RootMap.get(networkCollectionsList.getSelectedValue()),
				dataTypeOptions.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
		if(!keyColumnForMapping.getPossibleValues().containsAll(tempList.getPossibleValues()) 
				|| keyColumnForMapping.getPossibleValues().size() != tempList.getPossibleValues().size())
			keyColumnForMapping = tempList;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Import Data ";
	}



	public ImportDataTableTask(CyTableReader reader, CyRootNetworkManager rootNetworkManeger, CyNetworkManager networkManager) {
		this.reader = reader;
		this.byReader = true;
		this.globalTable = null;

		init(rootNetworkManeger, networkManager);
	}

	public ImportDataTableTask(CyTable globalTable, CyRootNetworkManager rootNetworkManeger, CyNetworkManager networkManager) {
		this.reader = null;
		this.byReader = false;
		this.globalTable = globalTable;

		init(rootNetworkManeger, networkManager);
	}

	
	private final void init(CyRootNetworkManager rootNetworkManeger, CyNetworkManager networkManager) {
		this.rootNetworkManager = rootNetworkManeger;
		this.networkManager = networkManager;
		this.name2NetworkMap = new HashMap<String, CyNetwork>();
		this.name2RootMap = new HashMap<String, CyRootNetwork>();
		this.source2targetColumnMap = new HashMap<String, String>();

		initTunable(networkManager);
	}

	private final void initTunable(CyNetworkManager networkManage) {
		
		importTypeChooser = new ListSingleSelection<String>("To a Network Collection","To selected networks only");
		importTypeChooser.setSelectedValue("To a Network Collection");

		final List<TableType> options = new ArrayList<TableType>();
		for (TableType type : TableType.values())
			options.add(type);
		dataTypeOptions = new ListSingleSelection<TableType>(options);
		dataTypeOptions.setSelectedValue(TableType.NODE_ATTR);

		for (CyNetwork net : networkManage.getNetworkSet()) {
			String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
			name2NetworkMap.put(netName, net);
		}
		List<String> names = new ArrayList<String>();
		names.addAll(name2NetworkMap.keySet());
		if (names.isEmpty())
			networkList = new ListMultipleSelection<String>(NO_NETWORKS);
		else
			networkList = new ListMultipleSelection<String>(names);

		for (CyNetwork net : networkManager.getNetworkSet()) {
			final CyRootNetwork rootNet = rootNetworkManager.getRootNetwork(net);
			if (!name2RootMap.containsValue(rootNet))
				name2RootMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
		}
		List<String> rootNames = new ArrayList<String>();
		rootNames.addAll(name2RootMap.keySet());
		networkCollectionsList = new ListSingleSelection<String>(rootNames);
		if(!rootNames.isEmpty())
		{
			networkCollectionsList.setSelectedValue(rootNames.get(0));
	
			keyColumnForMapping = getColumns(name2RootMap.get(networkCollectionsList.getSelectedValue()),
					dataTypeOptions.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
		}
	}

	public ListSingleSelection<String> getColumns(CyNetwork network, TableType tableType, String namespace) {
		CyTable selectedTable = getTable(network, tableType, CyRootNetwork.SHARED_ATTRS);

		List<String> colNames = new ArrayList<String>();
		for (CyColumn col : selectedTable.getColumns())
			colNames.add(col.getName());

		ListSingleSelection<String> columns = new ListSingleSelection<String>(colNames);
		columns.setSelectedValue(CyRootNetwork.SHARED_NAME);
		return columns;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		TableType tableType = dataTypeOptions.getSelectedValue();
		
		if(name2RootMap.isEmpty())
			return;

		if (!checkKeys()) {
			throw new IllegalArgumentException("Types of keys selected for tables are not matching.");
		}

		if(importTypeChooser.getSelectedValue().matches("To a Network Collection"))
			mapTableToDefaultAttrs(tableType);
		else
			mapTableToLocalAttrs(tableType);

	}

	private void mapTableToLocalAttrs(TableType tableType) {
		List<CyNetwork> networks = new ArrayList<CyNetwork>();

		if (networkList.getSelectedValues().isEmpty())
			return;

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
		CyTable targetTable = getTable(name2RootMap.get(networkCollectionsList.getSelectedValue()), tableType,
				CyRootNetwork.SHARED_DEFAULT_ATTRS);
		if (targetTable != null) {
			applyMapping(targetTable);
		}
	}

	private CyTable getTable(CyNetwork network, TableType tableType, String namespace) {
		if (tableType == TableType.NODE_ATTR)
			return network.getTable(CyNode.class, namespace);
		if (tableType == TableType.EDGE_ATTR)
			return network.getTable(CyEdge.class, namespace);
		if (tableType == TableType.NETWORK_ATTR)
			return network.getTable(CyNetwork.class, namespace);

		logger.warn("The selected table type is not valie. \nTable needs to be one of these types: "
				+ TableType.NODE_ATTR + ", " + TableType.EDGE_ATTR + ", " + TableType.NETWORK_ATTR + ".");
		return null;
	}

	private void applyMapping(CyTable targetTable) {
		ArrayList<CyColumn> columns = new ArrayList<CyColumn>();
		if (byReader) {
			if (reader.getTables() != null && reader.getTables().length > 0) {
				for (CyTable sourceTable : reader.getTables()) {
					columns.addAll(sourceTable.getColumns());
					copyColumns(sourceTable,columns, targetTable,false);
					copyRows(sourceTable,columns, targetTable);
				}
			}
		} else {
			if(globalTable != null)
			{
				columns.addAll(globalTable.getColumns());
				copyColumns(globalTable,columns, targetTable, true);
				copyRows(globalTable,columns, targetTable);
			}
			
		}

	}

	private CyColumn getJoinTargetColumn(CyTable targetTable) {
		String joinKeyName = CyNetwork.NAME;
		if(importTypeChooser.getSelectedValue().matches("To a Network Collection"))
			joinKeyName = keyColumnForMapping.getSelectedValue();
		return targetTable.getColumn(joinKeyName);
	}

	private void copyRows(CyTable sourceTable, List<CyColumn> sourceColumns, CyTable targetTable) {
		CyColumn targetKeyColumn = getJoinTargetColumn(targetTable);

		for (CyRow targetRow : targetTable.getAllRows()) {
			Object key = targetRow.get(targetKeyColumn.getName(), targetKeyColumn.getType());

			if (!sourceTable.rowExists(key))
				continue;

			CyRow sourceRow = sourceTable.getRow(key);

			if (sourceRow == null)
				continue;

			for (CyColumn col : sourceColumns) {
				
				if (col == sourceTable.getPrimaryKey())
					continue;

				String targetColName = source2targetColumnMap.get(col.getName());
				
				if (targetColName == null)
					continue;  // skip this column

				if (col.getType() == List.class)
					targetRow.set(targetColName, sourceRow.getList(col.getName(), col.getListElementType()));
				else
					targetRow.set(targetColName, sourceRow.get(col.getName(), col.getType()));

			}
		}

	}

	private void copyColumns(CyTable sourceTable, List<CyColumn> sourceColumns,CyTable targetTable, boolean addVirtual) {

		for (CyColumn col : sourceColumns) {
			
			if (col == sourceTable.getPrimaryKey())
				continue;
			
			// This is a bad idea!  It prevents users from updating data in existing
			// columns, which is a common case
			// String targetColName = getUniqueColumnName(targetTable, col.getName());
			String targetColName = col.getName();

			if (targetTable.getColumn(targetColName) == null) {
				if(!addVirtual)
				{
					if (col.getType() == List.class)
						targetTable.createListColumn(targetColName, col.getListElementType(), col.isImmutable());
					else
						targetTable.createColumn(targetColName, col.getType(), col.isImmutable(), col.getDefaultValue());
				}
				else
				{
					targetTable.addVirtualColumn(targetColName, col.getName(), sourceTable, getJoinTargetColumn(targetTable).getName(), false);
				}
			} else {
				CyColumn targetCol = targetTable.getColumn(targetColName);
				if ((targetCol.getType() != col.getType()) ||
				    (col.getType() == List.class && (targetCol.getListElementType() != col.getListElementType()))) {
					logger.error("Column '"+targetColName+"' has a different type in the target table -- skipping column");
					continue;
				}
			}

			source2targetColumnMap.put(col.getName(), targetColName);
		}
	}

	public boolean checkKeys() {

		Class<?> joinTargetColumnType = String.class;
		if(importTypeChooser.getSelectedValue().matches("To a Network Collection"))
			joinTargetColumnType = getJoinTargetColumn(
					getTable(name2RootMap.get(networkCollectionsList.getSelectedValue()), dataTypeOptions.getSelectedValue(),
							CyNetwork.DEFAULT_ATTRS)).getType();
		if (byReader) {
			for (CyTable readerTable : reader.getTables())
				if (readerTable.getPrimaryKey().getType() != joinTargetColumnType)
					return false;

		} else 
		{
			
			if (globalTable.getPrimaryKey().getType() != joinTargetColumnType)
				return false;
			
		}

		return true;
	}

}
