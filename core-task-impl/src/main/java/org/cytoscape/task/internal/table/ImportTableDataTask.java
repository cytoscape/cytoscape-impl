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

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportTableDataTask extends AbstractTask implements TunableValidator {
	
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
	
	public static final String NETWORK_COLLECTION = "To a Network Collection";
	public static final String NETWORK_SELECTION = "To selected networks only";
	public static final String UNASSIGNED_TABLE = "To an unassigned table";
	
	private CyTableReader reader;
	private final CyServiceRegistrar serviceRegistrar;
	
	private CyTable globalTable;
	private boolean byReader;
	private Map<String, CyNetwork> name2NetworkMap;
	private Map<String, CyRootNetwork> name2RootMap;
	private Map<String, String> source2targetColumnMap;
	private boolean networksPresent;
	
	
	public ListSingleSelection<String> whereImportTable ;
	
	@Tunable(
			description = "Where to Import Table Data:",
			gravity = 1.0,
			groups = { "Target Table Data" },
			xorChildren = true
	)
	public ListSingleSelection<String> getWhereImportTable() {
		return whereImportTable;
	}

	public void setWhereImportTable(ListSingleSelection<String> chooser) {
		this.whereImportTable = chooser;
	}

	/* --- [ NETWORK_COLLECTION ]------------------------------------------------------------------------------------ */
	
	public ListSingleSelection<String> targetNetworkCollection;
	
	@Tunable(
			description = "Network Collection:",
			groups = { "Target Table Data", "Select a Network Collection" },
			gravity = 2.0,
			xorKey = NETWORK_COLLECTION
	)
	public ListSingleSelection<String> getTargetNetworkCollection() {
		return targetNetworkCollection;
	}

	public void setTargetNetworkCollection(ListSingleSelection<String> roots) {
		targetNetworkCollection = roots;
		updateKeyColumnForMapping();
	}

	public ListSingleSelection<TableType> dataTypeTargetForNetworkCollection;

	@Tunable(
			description = "Import Data as:",
			groups = { "Target Table Data", "Select a Network Collection" },
			gravity = 3.1,
			xorKey = NETWORK_COLLECTION
	)
	public ListSingleSelection<TableType> getDataTypeTargetForNetworkCollection() {
		return dataTypeTargetForNetworkCollection;
	}

	public void setDataTypeTargetForNetworkCollection(ListSingleSelection<TableType> options) {
		dataTypeTargetForNetworkCollection = options;
		updateKeyColumnForMapping();
	}
	
	public ListSingleSelection<String> keyColumnForMapping;
	
	@Tunable(
			description = "Key Column for Network:",
			groups = { "Target Table Data", "Select a Network Collection" },
			gravity = 3.2,
			xorKey = NETWORK_COLLECTION,
			listenForChange = { "DataTypeTargetForNetworkCollection", "TargetNetworkCollection" }
	)
	public ListSingleSelection<String> getKeyColumnForMapping() {
		return keyColumnForMapping;
	}

	public void setKeyColumnForMapping(ListSingleSelection<String> colList) {
		this.keyColumnForMapping = colList;
	}
	
	@Tunable(
			description = "Case Sensitive Key Values:",
			groups = { "Target Table Data", "Select a Network Collection" },
			gravity = 3.3,
			xorKey = NETWORK_COLLECTION
	)
	public boolean caseSensitiveNetworCollectionKeys = true;

	/* --- [ NETWORK_SELECTION ]------------------------------------------------------------------------------------- */
	
	public ListMultipleSelection<String> targetNetworkList;
	
	@Tunable(
			description = "Network List:",
			groups = { "Target Table Data","Select Networks" },
			gravity = 3.1,
			xorKey = NETWORK_SELECTION
	)
	public ListMultipleSelection<String> getTargetNetworkList() {
		return targetNetworkList;
	}

	public void setTargetNetworkList(ListMultipleSelection<String> list) {
		this.targetNetworkList = list;
	}
	
	public ListSingleSelection<TableType> dataTypeTargetForNetworkList;
	
	@Tunable(
			description = "Import Data as:",
			groups = { "Target Table Data", "Select Networks" },
			gravity = 3.2,
			xorKey = NETWORK_SELECTION
	)
	public ListSingleSelection<TableType> getDataTypeTargetForNetworkList() {
		return dataTypeTargetForNetworkList;
	}

	public void setDataTypeTargetForNetworkList(ListSingleSelection<TableType> options) {
		dataTypeTargetForNetworkList = options;
	}
	
	@Tunable(
			description = "Case Sensitive Key Values:",
			groups = { "Target Table Data", "Select Networks" },
			gravity = 3.4,
			xorKey = NETWORK_SELECTION
	)
	public boolean caseSensitiveNetworkKeys = true;
	
	/* --- [ UNASSIGNED_TABLE ]-------------------------------------------------------------------------------------- */
	
	@Tunable(
			description = "New Table Name:",
			groups = { "Target Table Data", "Set New Table Name" },
			gravity = 5.0,
			xorKey = UNASSIGNED_TABLE
	)
	public String newTableName;
	
	@Tunable(
			description = "Network View Renderer:",
			groups = { "Target Table Data", "Select Renderer" },
			gravity = 6.0
	)
	public ListSingleSelection<NetworkViewRenderer> renderers;
	
	@ProvidesTitle
	public String getTitle() {
		return "Import Data";
	}

	public ImportTableDataTask(final CyTableReader reader, final CyServiceRegistrar serviceRegistrar) {
		this.reader = reader;
		this.serviceRegistrar = serviceRegistrar;
		this.byReader = true;

		init();
	}

	public ImportTableDataTask(final CyTable globalTable, final CyServiceRegistrar serviceRegistrar) {
		this.byReader = false;
		this.serviceRegistrar = serviceRegistrar;
		this.globalTable = globalTable;

		init();
	}
	
	private final void init() {
		this.name2NetworkMap = new HashMap<>();
		this.name2RootMap = new HashMap<>();
		this.source2targetColumnMap = new HashMap<>();

		final CyNetworkManager netMgr = serviceRegistrar.getService(CyNetworkManager.class);
		
		if (netMgr.getNetworkSet().size() > 0) {
			whereImportTable = new ListSingleSelection<String>(NETWORK_COLLECTION, NETWORK_SELECTION, UNASSIGNED_TABLE);
			whereImportTable.setSelectedValue(NETWORK_COLLECTION);
			networksPresent = true;
		} else {
			whereImportTable = new ListSingleSelection<String>(UNASSIGNED_TABLE);
			whereImportTable.setSelectedValue(UNASSIGNED_TABLE);
		}
		
		if (byReader) {
			if (this.reader != null && this.reader.getTables() != null)
				newTableName = reader.getTables()[0].getTitle();
		} else {
			newTableName = globalTable.getTitle();
		}

		if (networksPresent) {
			final List<TableType> options = new ArrayList<TableType>();
			
			for (TableType type : TableType.values())
				options.add(type);
			
			dataTypeTargetForNetworkCollection = new ListSingleSelection<TableType>(options);
			dataTypeTargetForNetworkCollection.setSelectedValue(TableType.NODE_ATTR);
			
			dataTypeTargetForNetworkList = new ListSingleSelection<TableType>(options);
			dataTypeTargetForNetworkList.setSelectedValue(TableType.NODE_ATTR);
	
			for (CyNetwork net : netMgr.getNetworkSet()) {
				final String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
				name2NetworkMap.put(netName, net);
			}
			
			final List<String> names = new ArrayList<>();
			names.addAll(name2NetworkMap.keySet());
			
			final Collator collator = Collator.getInstance(Locale.getDefault());
			
			Collections.sort(names, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					if (s1 == null || s2 == null) {
						if (s2 != null) return -1;
						if (s1 != null) return 1;
						return 0;
					}
					return collator.compare(s1, s2);
				}
			});
			
			if (names.isEmpty()) {
				targetNetworkList = new ListMultipleSelection<String>(NO_NETWORKS);
			} else {
				targetNetworkList = new ListMultipleSelection<String>(names);
				
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				final CyNetwork currNet = appMgr.getCurrentNetwork();
				
				if (currNet != null) {
					final String currName = currNet.getRow(currNet).get(CyNetwork.NAME, String.class);
					
					if (currName != null && targetNetworkList.getPossibleValues().contains(currName))
					targetNetworkList.setSelectedValues(Collections.singletonList(currName));
				}
			}
	
			final CyRootNetworkManager rootNetMgr = serviceRegistrar.getService(CyRootNetworkManager.class);
			
			for (CyNetwork net : netMgr.getNetworkSet()) {
				final CyRootNetwork rootNet = rootNetMgr.getRootNetwork(net);
				
				if (!name2RootMap.containsValue(rootNet))
					name2RootMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
			}
			
			final List<String> rootNames = new ArrayList<String>();
			rootNames.addAll(name2RootMap.keySet());
			targetNetworkCollection = new ListSingleSelection<String>(rootNames);
			
			if (!rootNames.isEmpty()) {
				targetNetworkCollection.setSelectedValue(rootNames.get(0));
		
				keyColumnForMapping = getColumns(name2RootMap.get(targetNetworkCollection.getSelectedValue()),
						dataTypeTargetForNetworkCollection.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
			}
		}
	}

	public ListSingleSelection<String> getColumns(final CyNetwork network, TableType tableType, String namespace) {
		CyTable selectedTable = getTable(network, tableType, CyRootNetwork.SHARED_ATTRS);
		List<String> colNames = new ArrayList<String>();
		
		for (CyColumn col : selectedTable.getColumns()) {
			if (col.getName().matches(CyNetwork.SUID))
				continue;
			
			colNames.add(col.getName());
		}

		ListSingleSelection<String> columns = new ListSingleSelection<String>(colNames);
		columns.setSelectedValue(CyRootNetwork.SHARED_NAME);
		
		return columns;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (networksPresent) {
			if (name2RootMap.isEmpty())
				return;
		
			if (!checkKeys())
				throw new IllegalArgumentException("Types of keys selected for tables are not matching.");
		}

		if (whereImportTable.getSelectedValue().matches(NETWORK_COLLECTION))
			mapTableToDefaultAttrs(getDataTypeOptions());
		if (whereImportTable.getSelectedValue().matches(NETWORK_SELECTION))
			mapTableToLocalAttrs(getDataTypeOptions());
		if (whereImportTable.getSelectedValue().matches(UNASSIGNED_TABLE))
			addTable();
	}
	
	private void updateKeyColumnForMapping() {
		ListSingleSelection<String> tempList = getColumns(name2RootMap.get(targetNetworkCollection.getSelectedValue()),
				dataTypeTargetForNetworkCollection.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
		
		if (!keyColumnForMapping.getPossibleValues().containsAll(tempList.getPossibleValues())
				|| keyColumnForMapping.getPossibleValues().size() != tempList.getPossibleValues().size())
			keyColumnForMapping = tempList;
	}
	
	private void mapTableToLocalAttrs(final TableType tableType) {
		final List<CyNetwork> networks = new ArrayList<CyNetwork>();

		if (targetNetworkList.getSelectedValues().isEmpty())
			return;

		if (!targetNetworkList.getSelectedValues().get(0).equals(NO_NETWORKS))
			for (String netName : targetNetworkList.getSelectedValues())
				networks.add(name2NetworkMap.get(netName));

		for (CyNetwork network : networks) {
			CyTable targetTable = getTable(network, tableType, CyNetwork.LOCAL_ATTRS);
			
			if (targetTable != null)
				applyMapping(targetTable, caseSensitiveNetworkKeys);
		}
	}

	private void mapTableToDefaultAttrs(final TableType tableType) {
		final CyTable targetTable = getTable(name2RootMap.get(targetNetworkCollection.getSelectedValue()), tableType,
				CyRootNetwork.SHARED_DEFAULT_ATTRS);
		
		if (targetTable != null)
			applyMapping(targetTable, caseSensitiveNetworCollectionKeys);
	}

	private CyTable getTable(CyNetwork network, TableType tableType, String namespace) {
		if (tableType == TableType.NODE_ATTR)
			return network.getTable(CyNode.class, namespace);
		if (tableType == TableType.EDGE_ATTR)
			return network.getTable(CyEdge.class, namespace);
		if (tableType == TableType.NETWORK_ATTR)
			return network.getTable(CyNetwork.class, namespace);

		logger.warn("The selected table type is not valid. \nTable needs to be one of these types: "
				+ TableType.NODE_ATTR + ", " + TableType.EDGE_ATTR + ", " + TableType.NETWORK_ATTR + ".");
		
		return null;
	}

	private void applyMapping(final CyTable targetTable, final boolean caseSensitive) {
		ArrayList<CyColumn> columns = new ArrayList<CyColumn>();
		
		if (byReader) {
			if (reader.getTables() != null && reader.getTables().length > 0) {
				for (CyTable sourceTable : reader.getTables()) {
					columns.addAll(sourceTable.getColumns());
					copyColumns(sourceTable, columns, targetTable, false);
					copyRows(sourceTable, columns, targetTable, caseSensitive);
				}
			}
		} else {
			if (globalTable != null) {
				columns.addAll(globalTable.getColumns());
				copyColumns(globalTable, columns, targetTable, true);
				copyRows(globalTable, columns, targetTable, caseSensitive);
			}
		}
	}

	private CyColumn getJoinTargetColumn(final CyTable targetTable) {
		String joinKeyName = CyNetwork.NAME;
		
		if (whereImportTable.getSelectedValue().matches(NETWORK_COLLECTION))
			joinKeyName = keyColumnForMapping.getSelectedValue();
		
		return targetTable.getColumn(joinKeyName);
	}

	private void copyRows(final CyTable sourceTable, final List<CyColumn> sourceColumns, final CyTable targetTable,
			final boolean caseSensitive) {
		final CyColumn targetKeyColumn = getJoinTargetColumn(targetTable);
		final Map<String, String> normalizedSourceKeys = new HashMap<>();
		
		if (!caseSensitive) {
			final CyColumn pk = sourceTable.getPrimaryKey();
			
			if (pk.getType() == String.class) {
				for (CyRow row : sourceTable.getAllRows()) {
					final String key = row.get(pk.getName(), String.class);
					
					if (key != null)
						normalizedSourceKeys.put(key.toLowerCase().trim(), key);
				}
			}
		}
		
		for (CyRow targetRow : targetTable.getAllRows()) {
			Object key = targetRow.get(targetKeyColumn.getName(), targetKeyColumn.getType());
			
			if (key == null)
				continue; // Should never happen!

			if (!caseSensitive)
				key = normalizedSourceKeys.get(key.toString().toLowerCase().trim());
			
			if (key == null || !sourceTable.rowExists(key))
				continue;
				
			final CyRow sourceRow = sourceTable.getRow(key);

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
				if (!addVirtual) {
					if (col.getType() == List.class)
						targetTable.createListColumn(targetColName, col.getListElementType(), col.isImmutable());
					else
						targetTable.createColumn(targetColName, col.getType(), col.isImmutable(), col.getDefaultValue());
				} else {
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
	
	private void addTable(){
		final CyTableManager tableMgr = serviceRegistrar.getService(CyTableManager.class);
		
		if (byReader) {
			if (this.reader != null && this.reader.getTables() != null) {
				for (CyTable table : reader.getTables()) {
					if (!newTableName.isEmpty())
						table.setTitle(newTableName);
					
					tableMgr.addTable(table);
				}
			} else{
				if (reader == null)
					logger.warn("reader is null." );
				else
					logger.warn("No tables in reader.");
			}
		} else {
			if (tableMgr.getTable(globalTable.getSUID()) != null) {
				tableMgr.deleteTable(globalTable.getSUID());
				globalTable.setPublic(true);
			}
			
			if (!newTableName.isEmpty())
				globalTable.setTitle(newTableName);
			
			tableMgr.addTable(globalTable);
		}
	}
	
	private TableType getDataTypeOptions() {
		if (whereImportTable.getSelectedValue().matches(NETWORK_COLLECTION))
			return dataTypeTargetForNetworkCollection.getSelectedValue();
		
		return dataTypeTargetForNetworkList.getSelectedValue();
	}

	public boolean checkKeys() {
		Class<?> joinTargetColumnType = String.class;

		if (whereImportTable.getSelectedValue().matches(NETWORK_COLLECTION))
			joinTargetColumnType = getJoinTargetColumn(
					getTable(name2RootMap.get(targetNetworkCollection.getSelectedValue()), getDataTypeOptions(),
							CyNetwork.DEFAULT_ATTRS)).getType();

		if (byReader) {
			for (CyTable readerTable : reader.getTables()) {
				if (readerTable.getPrimaryKey().getType() != joinTargetColumnType)
					return false;
			}
		} else {
			if (globalTable.getPrimaryKey().getType() != joinTargetColumnType)
				return false;
		}

		return true;
	}
	
	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if (!whereImportTable.getSelectedValue().matches(UNASSIGNED_TABLE) || newTableName.isEmpty())
			return ValidationState.OK;

		final CyTableManager tableMgr = serviceRegistrar.getService(CyTableManager.class);

		for (CyTable table : tableMgr.getGlobalTables()) {
			try {
				if (table.getTitle().matches(newTableName)) {
					errMsg.append(
							"There already exists a table with name: " + newTableName
							+ ". Please select another table name.\n");
					return ValidationState.INVALID;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
		}

		return ValidationState.OK;
	}
}
