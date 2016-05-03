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

import static org.cytoscape.work.TunableValidator.ValidationState.OK;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeTablesTask extends AbstractTask implements TunableValidator {
	
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
	public static final String NO_TABLES = "No Tables Found";
	
	public static final String NETWORK_COLLECTION = "a network collection";
	public static final String NETWORK_SELECTION = "selected networks only";
	public static final String UNASSIGNED_TABLE = "an unassigned table";
	
	public static final String COPY_COLUMNS = "Copy Columns";
	public static final String LINK_COLUMNS = "Link To Columns";
	
	private CyRootNetworkManager rootNetworkManager;
	private CyTableManager tableMgr;
	private Map<String, CyNetwork> name2NetworkMap;
	private Map<String, CyRootNetwork> name2RootMap;
	private Map<String, String> source2targetColumnMap;
	
	public ListSingleSelection<CyTable> sourceTable;

	@Tunable(description = "Source Table:", gravity = 0.1, groups = { "Source" })
	public ListSingleSelection<CyTable> getSourceTable() {
		return sourceTable;
	}
	
	public void setSourceTable(ListSingleSelection<CyTable> table) {
		final ListMultipleSelection<String> tempList = getColumnList(table.getSelectedValue());

		if (!sourceMergeColumns.getPossibleValues().containsAll(tempList.getPossibleValues())
				|| sourceMergeColumns.getPossibleValues().size() != tempList.getPossibleValues().size()) {
			sourceMergeColumns = tempList;
			sourceMergeKey = getKeyColumnList(table.getSelectedValue());
		}

		final List<Object> listOfGlobal = getPublicGlobalTables();
		
		if (listOfGlobal.contains(table.getSelectedValue())) {
			if (listOfGlobal.size() == 1) {
				listOfGlobal.clear();
				listOfGlobal.add(NO_TABLES);
				targetMergeKey = new ListSingleSelection<>(NO_TABLES);
				unassignedTable = new ListSingleSelection<>(listOfGlobal);
			} else {
				listOfGlobal.remove(table.getSelectedValue());
				unassignedTable = new ListSingleSelection<>(listOfGlobal);
				targetMergeKey = getKeyColumnList((CyTable) unassignedTable.getSelectedValue());
			}
		} else {
			if (listOfGlobal.size() == 1
					&& !(unassignedTable.getSelectedValue() instanceof CyTable)
					|| ((listOfGlobal.size() != unassignedTable.getPossibleValues().size()) && (listOfGlobal.size() > 0))) {
				unassignedTable = new ListSingleSelection<>(listOfGlobal);
				targetMergeKey = getKeyColumnList((CyTable) unassignedTable.getSelectedValue());
			}
		}
		
		if (!isTableGlobal(table.getSelectedValue())) {
			if (mergeType.getPossibleValues().size() > 1)
				mergeType = new ListSingleSelection<>(COPY_COLUMNS);
		} else {
			if (mergeType.getPossibleValues().size() < 2) {
				mergeType = new ListSingleSelection<>(COPY_COLUMNS, LINK_COLUMNS);
				mergeType.setSelectedValue(COPY_COLUMNS);
			}
		}
		
		sourceTable = table;
	}
	
	public ListMultipleSelection<String> sourceMergeColumns;

	@Tunable(description = "Select Columns:", gravity = 0.2, groups = { "Source", "Columns To Merge" }, listenForChange = { "SourceTable", "SelectAllColumns" })
	public ListMultipleSelection<String> getSourceMergeColumns() {
		return sourceMergeColumns;
	}
	
	public void setSourceMergeColumns(ListMultipleSelection<String> columns) {
		sourceMergeColumns = columns;
	}

	public ListSingleSelection<String> sourceMergeKey;

	@Tunable(description = "Key column to merge:", groups = { "Source" }, gravity = 0.4, listenForChange = { "SourceTable" })
	public ListSingleSelection<String> getSourceMergeKey() {
		return sourceMergeKey;
	}

	public void setSourceMergeKey(ListSingleSelection<String> key) {
		this.sourceMergeKey = key;
	}

	@Tunable(description = "Type of merge:", gravity = 0.5, groups = { "Source" }, listenForChange = { "SourceTable" })
	public ListSingleSelection<String> mergeType;

	public ListSingleSelection<String> whereMergeTable;

	@Tunable(description = "Merge table to:", gravity = 1.0, groups = { "Target" }, xorChildren = true)
	public ListSingleSelection<String> getWhereMergeTable() {
		return whereMergeTable;
	}

	public void setWhereMergeTable(ListSingleSelection<String> chooser) {
		this.whereMergeTable = chooser;
	}

	public ListSingleSelection<String> targetNetworkCollection;

	@Tunable(description = "Network Collection:", groups = { "Target", "Select Network Collection" }, gravity = 2.1, xorKey = NETWORK_COLLECTION)
	public ListSingleSelection<String> getTargetNetworkCollection() {
		return targetNetworkCollection;
	}

	public void setTargetNetworkCollection(ListSingleSelection<String> roots) {
		ListSingleSelection<String> tempList = getKeyColumnList(name2RootMap.get(targetNetworkCollection.getSelectedValue()),
				dataTypeTargetForNetworkCollection.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
		
		if (!targetKeyNetworkCollection.getPossibleValues().containsAll(tempList.getPossibleValues())
				|| targetKeyNetworkCollection.getPossibleValues().size() != tempList.getPossibleValues().size())
			targetKeyNetworkCollection = tempList;
	}

	public ListSingleSelection<TableType> dataTypeTargetForNetworkCollection;

	@Tunable(description = "Merge data in:", groups = { "Target", "Select Network Collection" }, gravity = 2.2, xorKey = NETWORK_COLLECTION)
	public ListSingleSelection<TableType> getDataTypeTargetForNetworkCollection() {
		return dataTypeTargetForNetworkCollection;
	}

	public void setDataTypeTargetForNetworkCollection(ListSingleSelection<TableType> options) {
		ListSingleSelection<String> tempList = getKeyColumnList(name2RootMap.get(targetNetworkCollection.getSelectedValue()),
				dataTypeTargetForNetworkCollection.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
		
		if (!targetKeyNetworkCollection.getPossibleValues().containsAll(tempList.getPossibleValues())
				|| targetKeyNetworkCollection.getPossibleValues().size() != tempList.getPossibleValues().size())
			targetKeyNetworkCollection = tempList;
	}
	
	public ListSingleSelection<String> targetKeyNetworkCollection;

	@Tunable(description = "Key column for network:", groups = { "Target", "Select Network Collection" }, gravity = 2.3, xorKey = NETWORK_COLLECTION,
			listenForChange = { "DataTypeTargetForNetworkCollection", "TargetNetworkCollection" })
	public ListSingleSelection<String> getTargetKeyNetworkCollection() {
		return targetKeyNetworkCollection;
	}

	public void setTargetKeyNetworkCollection(ListSingleSelection<String> colList) {
		this.targetKeyNetworkCollection = colList;
	}

	public ListMultipleSelection<String> targetNetworkList;

	@Tunable(description = "Networks:", groups = { "Target", "Select Networks" }, gravity = 3.1, xorKey = NETWORK_SELECTION)
	public ListMultipleSelection<String> getTargetNetworkList() {
		return targetNetworkList;
	}

	public void setTargetNetworkList(ListMultipleSelection<String> list) {
		this.targetNetworkList = list;
	}

	@Tunable(description = "Merge data in:", groups = { "Target", "Select Networks" }, gravity = 3.2, xorKey = NETWORK_SELECTION)
	public ListSingleSelection<TableType> dataTypeTargetForNetworkList;

	public ListSingleSelection<Object> unassignedTable;

	@Tunable(description = "Unassigned table:", groups = { "Target", "Select Unassigned Table" }, gravity = 4.1, xorKey = UNASSIGNED_TABLE,
			listenForChange = { "SourceTable" })
	public ListSingleSelection<Object> getUnassignedTable() {
		return unassignedTable;
	}

	public void setUnassignedTable(ListSingleSelection<Object> tables) {
		if (tables.getSelectedValue() instanceof CyTable) {
			ListSingleSelection<String> tempList = getKeyColumnList((CyTable) tables.getSelectedValue());
			
			if (!targetMergeKey.getPossibleValues().containsAll(tempList.getPossibleValues())
					|| targetMergeKey.getPossibleValues().size() != tempList.getPossibleValues().size()) {
				targetMergeKey = tempList;
				targetMergeKey = getKeyColumnList((CyTable) tables.getSelectedValue());
			}
		}
		
		this.unassignedTable = tables;
	}
	
	public ListSingleSelection<String> targetMergeKey;

	@Tunable(description = "Key column to merge:", groups = { "Target", "Select Unassigned Table" }, gravity = 4.2,
			listenForChange = { "UnassignedTable", "SourceTable" })
	public ListSingleSelection<String> getTargetMergeKey() {
		return targetMergeKey;
	}

	public void setTargetMergeKey(ListSingleSelection<String> key) {
		this.targetMergeKey = key;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Merge Data Table";
	}

	public MergeTablesTask(final CyTableManager tableMgr, final CyRootNetworkManager rootNetworkManager,
			final CyNetworkManager networkManager) {
		init(tableMgr, rootNetworkManager, networkManager);
	}

	private final void init(final CyTableManager tableMgr, final CyRootNetworkManager rootNetworkManeger,
			final CyNetworkManager networkManager) {
		this.rootNetworkManager = rootNetworkManeger;
		this.name2NetworkMap = new HashMap<>();
		this.name2RootMap = new HashMap<>();
		this.source2targetColumnMap = new HashMap<>();
		this.tableMgr = tableMgr;

		initTunable(tableMgr, networkManager);
	}

	private final void initTunable(final CyTableManager tabelMgr, final CyNetworkManager networkManager) {
		final List<CyTable> listOfTables = new ArrayList<>();
		final List<Object> listOfUTables = new ArrayList<>();

		for (CyTable tempTable : tabelMgr.getGlobalTables()) {
			if (tempTable.isPublic()) {
				listOfTables.add(tempTable);
				listOfUTables.add(tempTable);
			}
		}

		final Set<CyNetwork> networkSet = networkManager.getNetworkSet();
		
		if (!networkSet.isEmpty()) {
			whereMergeTable = new ListSingleSelection<>(NETWORK_COLLECTION, NETWORK_SELECTION, UNASSIGNED_TABLE);
			whereMergeTable.setSelectedValue(NETWORK_COLLECTION);
			final List<TableType> options = new ArrayList<>();
			
			for (TableType type : TableType.values())
				options.add(type);
			
			dataTypeTargetForNetworkCollection = new ListSingleSelection<>(options);
			dataTypeTargetForNetworkCollection.setSelectedValue(TableType.NODE_ATTR);
			dataTypeTargetForNetworkList = new ListSingleSelection<>(options);
			dataTypeTargetForNetworkList.setSelectedValue(TableType.NODE_ATTR);

			for (CyNetwork net : networkSet) {
				String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
				name2NetworkMap.put(netName, net);
			}
			
			final List<String> names = new ArrayList<>();
			names.addAll(name2NetworkMap.keySet());
			sort(names);
			
			if (names.isEmpty())
				targetNetworkList = new ListMultipleSelection<>(NO_NETWORKS);
			else
				targetNetworkList = new ListMultipleSelection<>(names);

			for (CyNetwork net : networkSet) {
				final CyRootNetwork rootNet = rootNetworkManager.getRootNetwork(net);
				
				if (!name2RootMap.containsValue(rootNet))
					name2RootMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
			}
			
			final List<String> rootNames = new ArrayList<>();
			rootNames.addAll(name2RootMap.keySet());
			sort(rootNames);
			targetNetworkCollection = new ListSingleSelection<>(rootNames);
			
			if (!rootNames.isEmpty()) {
				targetNetworkCollection.setSelectedValue(rootNames.get(0));

				targetKeyNetworkCollection = getKeyColumnList(name2RootMap.get(targetNetworkCollection.getSelectedValue()),
						dataTypeTargetForNetworkCollection.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
			}
			
			for (CyNetwork network : networkSet) {
				listOfTables.add(network.getDefaultNodeTable());
				listOfTables.add(network.getDefaultEdgeTable());
			}
		} else {
			whereMergeTable = new ListSingleSelection<>(UNASSIGNED_TABLE);
			whereMergeTable.setSelectedValue(UNASSIGNED_TABLE);
		}

		sourceTable = new ListSingleSelection<>(listOfTables);
		
		if (!isTableGlobal(sourceTable.getSelectedValue())) {
			mergeType = new ListSingleSelection<>(COPY_COLUMNS);
		} else {
			mergeType = new ListSingleSelection<>(COPY_COLUMNS, LINK_COLUMNS);
			mergeType.setSelectedValue(COPY_COLUMNS);
		}
		
		sourceMergeColumns = getColumnList(sourceTable.getSelectedValue());
		sourceMergeKey = getKeyColumnList(sourceTable.getSelectedValue());
		
		if (listOfUTables.size() > 1) {
			if (listOfUTables.contains(sourceTable.getSelectedValue()))
				listOfUTables.remove(sourceTable.getSelectedValue());
			
			unassignedTable = new ListSingleSelection<>(listOfUTables);
			targetMergeKey = getKeyColumnList((CyTable) unassignedTable.getSelectedValue());
		} else {
			listOfUTables.clear();
			listOfUTables.add(NO_TABLES);
			targetMergeKey = new ListSingleSelection<>(NO_TABLES);
			unassignedTable = new ListSingleSelection<>(listOfUTables);
		}
	}

	public ListSingleSelection<String> getKeyColumnList(CyNetwork network, TableType tableType, String namespace) {
		final CyTable table = getTable(network, tableType, CyRootNetwork.SHARED_ATTRS);
		final ListSingleSelection<String> columns = getKeyColumnList(table);
		
		if (columns.getPossibleValues().contains(CyRootNetwork.SHARED_NAME))
			columns.setSelectedValue(CyRootNetwork.SHARED_NAME);
		
		return columns;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// If we are here and there no networks loaded, we could only continue
		// if the merge is on an unassigned table
		if (!whereMergeTable.getSelectedValue().matches(UNASSIGNED_TABLE)) {
			if (name2RootMap.isEmpty())
				return;
		}

		if (!checkKeys()) {
			if(!isNewColumnVirtual())
				throw new IllegalArgumentException("Types of keys selected for tables are not valid.\n"
						+ "Keys must be of type Integer, Long, or String.");
			else
				throw new IllegalArgumentException("Types of keys selected for tables are not valid.\n"
						+ "Keys must be of type Integer, Long, or String, and must be the same type for a soft merge.");
		}

		if (whereMergeTable.getSelectedValue().matches(NETWORK_COLLECTION))
			mapTableToDefaultAttrs(getDataTypeOptions());
		else if (whereMergeTable.getSelectedValue().matches(NETWORK_SELECTION))
			mapTableToLocalAttrs(getDataTypeOptions());
		else if (whereMergeTable.getSelectedValue().matches(UNASSIGNED_TABLE))
			mapTableToUnassignedTable();
	}
	
	private boolean isMappableColumn(final CyColumn col) {
		final String name = col.getName();
		final Class<?> type = col.getType();
		
		return (type == Integer.class || type == Long.class || type == String.class) && 
				!name.equals(CyNetwork.SUID) && 
				!name.endsWith(".SUID");
	}
	
	private void mapTableToUnassignedTable() {
		if (!unassignedTable.getSelectedValue().toString().matches(NO_TABLES)) {
			CyTable tableChosen = (CyTable) unassignedTable.getSelectedValue();
			
			if (!tableChosen.equals(sourceTable))
				applyMapping(tableChosen);
		}
	}

	private void mapTableToLocalAttrs(TableType tableType) {
		List<CyNetwork> networks = new ArrayList<>();

		if (targetNetworkList.getSelectedValues().isEmpty())
			return;

		if (!targetNetworkList.getSelectedValues().get(0).equals(NO_NETWORKS))
			for (String netName : targetNetworkList.getSelectedValues())
				networks.add(name2NetworkMap.get(netName));

		for (CyNetwork network : networks) {
			CyTable targetTable = getTable(network, tableType, CyNetwork.LOCAL_ATTRS);

			if (targetTable != null)
				applyMapping(targetTable);
		}
	}

	private void mapTableToDefaultAttrs(TableType tableType) {
		CyTable targetTable = getTable(name2RootMap.get(targetNetworkCollection.getSelectedValue()), tableType,
				CyRootNetwork.SHARED_DEFAULT_ATTRS);
		
		if (targetTable != null)
			applyMapping(targetTable);
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
		ArrayList<CyColumn> columns = new ArrayList<>();
		CyColumn tempCol = null;

		for (String colName : sourceMergeColumns.getSelectedValues()) {
			tempCol = sourceTable.getSelectedValue().getColumn(colName);
			
			if (tempCol != null)
				columns.add(tempCol);
		}
		
		copyColumns(sourceTable.getSelectedValue(), columns, targetTable, isNewColumnVirtual());
		
		if (!isNewColumnVirtual())
			copyRows(sourceTable.getSelectedValue(), columns, targetTable);
	}

	private CyColumn getJoinTargetColumn(CyTable targetTable) {
		String joinKeyName = CyNetwork.NAME;
		
		if (whereMergeTable.getSelectedValue().matches(NETWORK_COLLECTION))
			joinKeyName = targetKeyNetworkCollection.getSelectedValue();
		if (whereMergeTable.getSelectedValue().matches(UNASSIGNED_TABLE))
			joinKeyName = targetMergeKey.getSelectedValue();// targetTable.getPrimaryKey().getName();
		
		return targetTable.getColumn(joinKeyName);
	}

	private void copyRows(CyTable inputTable, List<CyColumn> sourceColumns, CyTable targetTable) {
		CyRow sourceRow = null;
		CyColumn targetKeyColumn = getJoinTargetColumn(targetTable);

		for (CyRow targetRow : targetTable.getAllRows()) {
			Object key = targetRow.get(targetKeyColumn.getName(), targetKeyColumn.getType());
			
			if (key == null)
				continue;
			
			if (key.getClass() != getMergeKeyColumn().getType() ) {
				try {
					key = DataUtils.convertString(key.toString(), getMergeKeyColumn().getType());
				}
				catch (Exception e) {
					continue;
				}
			}

			if (isMergeColumnKeyColumn()) {
				if (!inputTable.rowExists(key))
					continue;

				sourceRow = inputTable.getRow(key);
			} else {
				if (inputTable.getMatchingRows(getMergeKeyColumn().getName(), key).isEmpty())
					continue;

				sourceRow = inputTable.getMatchingRows(getMergeKeyColumn().getName(), key).iterator().next();
			}

			if (sourceRow == null)
				continue;

			for (CyColumn col : sourceColumns) {
				if (col == getMergeKeyColumn())
					continue;

				String targetColName = source2targetColumnMap.get(col.getName());

				if (targetColName == null)
					continue; // skip this column

				if (col.getType() == List.class)
					targetRow.set(targetColName, sourceRow.getList(col.getName(), col.getListElementType()));
				else
					targetRow.set(targetColName, sourceRow.get(col.getName(), col.getType()));
			}
		}
	}

	private void copyColumns(CyTable inputTable, List<CyColumn> sourceColumns, CyTable targetTable, boolean addVirtual) {
		for (CyColumn col : sourceColumns) {
			if (col == getMergeKeyColumn())
				continue;

			// This is a bad idea!  It prevents users from updating data in existing columns, which is a common case
			// String targetColName = getUniqueColumnName(targetTable, col.getName());
			final String targetColName = col.getName();

			if (!isMergeColumnKeyColumn())
				addVirtual = false;

			if (targetTable.getColumn(targetColName) == null) {
				if (!addVirtual) {
					if (col.getType() == List.class)
						targetTable.createListColumn(targetColName, col.getListElementType(), col.isImmutable());
					else
						targetTable.createColumn(targetColName, col.getType(), col.isImmutable(),
								col.getDefaultValue());
				} else {
					targetTable.addVirtualColumn(targetColName, col.getName(), inputTable,
							getJoinTargetColumn(targetTable).getName(), false);
				}
			} else {
				CyColumn targetCol = targetTable.getColumn(targetColName);
				
				if ((targetCol.getType() != col.getType())
						|| (col.getType() == List.class && (targetCol.getListElementType() != col.getListElementType()))) {
					logger.error("Column '" + targetColName
							+ "' has a different type in the target table -- skipping column");
					continue;
				}
			}

			source2targetColumnMap.put(col.getName(), targetColName);
		}
	}

	public boolean checkKeys() {
		CyColumn joinTargetColumn = null;
		
		if (whereMergeTable.getSelectedValue().matches(NETWORK_COLLECTION)) {
			joinTargetColumn = getJoinTargetColumn(
					getTable(name2RootMap.get(targetNetworkCollection.getSelectedValue()), getDataTypeOptions(),
							CyNetwork.DEFAULT_ATTRS));
		}
		
		else if (whereMergeTable.getSelectedValue().matches(UNASSIGNED_TABLE)) {
			if (!unassignedTable.getSelectedValue().equals(NO_TABLES))
				joinTargetColumn = getJoinTargetColumn((CyTable) unassignedTable.getSelectedValue());
		}
		
		
		if (!isMappableColumn(getMergeKeyColumn()))
			return false;
		
		if(joinTargetColumn != null) {
			if(!isNewColumnVirtual()) {
				if (!isMappableColumn(joinTargetColumn))
					return false;
			}

			else {
				//Don't need to check if mappable since equality implies this
				if(joinTargetColumn.getType() != getMergeKeyColumn().getType())
					return false;
			}
		}
		
		return true;
	}
	
	private ListMultipleSelection<String> getColumnList(final CyTable table) {
		String tempName = null;
		final List<String> colNames = new ArrayList<>();
		
		for (CyColumn col : table.getColumns()) {
			tempName = col.getName();
			
			if (!tempName.matches(CyRootNetwork.SHARED_NAME) && !tempName.matches(CyRootNetwork.NAME)
					&& !tempName.matches(CyRootNetwork.SUID) && !tempName.matches(CyRootNetwork.SELECTED))
				colNames.add(tempName);
		}
		
		sort(colNames);
		ListMultipleSelection<String> columns = new ListMultipleSelection<>(colNames);

		return columns;
	}

	private ListSingleSelection<String> getKeyColumnList(final CyTable table) {
		final List<String> colNames = new ArrayList<>();
		
		for (CyColumn col : table.getColumns()) {			
			if (isMappableColumn(col)) {
				colNames.add(col.getName());
			}
		}

		sort(colNames);
		final ListSingleSelection<String> columns = new ListSingleSelection<>(colNames);
		
		if (colNames.contains(CyRootNetwork.NAME))
			columns.setSelectedValue(CyRootNetwork.NAME);
		else if (!table.getPrimaryKey().getName().matches(CyRootNetwork.SUID) &&
				colNames.contains(table.getPrimaryKey().getName()))
			columns.setSelectedValue(table.getPrimaryKey().getName());

		return columns;
	}
	
	private void sort(final List<String> names) {
		if (!names.isEmpty()) {
			final Collator collator = Collator.getInstance(Locale.getDefault());
			
			Collections.sort(names, (s1, s2) -> {
                if (s1 == null && s2 == null) return 0;
                if (s1 == null) return -1;
                if (s2 == null) return 1;
                return collator.compare(s1, s2);
            });
		}
	}
	
	private boolean isNewColumnVirtual() {
		return (mergeType.getSelectedValue() == LINK_COLUMNS);
	}

	private boolean isMergeColumnKeyColumn() {
		return sourceTable.getSelectedValue().getPrimaryKey() == getMergeKeyColumn();
	}

	private CyColumn getMergeKeyColumn() {
		return sourceTable.getSelectedValue().getColumn(sourceMergeKey.getSelectedValue());
	}

	private List<Object> getPublicGlobalTables() {
		final List<Object> listTables = new ArrayList<>();

		for (CyTable tempTable : tableMgr.getGlobalTables()) {
			if (tempTable.isPublic())
				listTables.add(tempTable);
		}

		return listTables;
	}

	private boolean isTableGlobal(CyTable table) {
		for (CyTable tempTable : tableMgr.getGlobalTables()) {
			if (tempTable.equals(table))
				return true;
		}

		return false;
	}

	private TableType getDataTypeOptions() {
		if (whereMergeTable.getSelectedValue().matches(NETWORK_COLLECTION))
			return dataTypeTargetForNetworkCollection.getSelectedValue();
		else
			return dataTypeTargetForNetworkList.getSelectedValue();
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if (!isMergeColumnKeyColumn() && isNewColumnVirtual()) {
			try {
				mergeType.setSelectedValue(COPY_COLUMNS);
				errMsg.append("Source Key column needs to be the key column of source table to apply a soft merge.\n");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			
			return ValidationState.INVALID;
		}

		return OK;
	}
}
