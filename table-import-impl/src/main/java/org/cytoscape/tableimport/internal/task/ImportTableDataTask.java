package org.cytoscape.tableimport.internal.task;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.util.DataUtils;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListChangeListener;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

public class ImportTableDataTask extends AbstractTask implements TunableValidator, ObservableTask {
	
	enum TableType {
		NODE_ATTR("Node Table Columns", CyNode.class),
		EDGE_ATTR("Edge Table Columns", CyEdge.class),
		NETWORK_ATTR("Network Table Columns", CyNetwork.class);

		private final String name;
		private final Class<? extends CyIdentifiable> type;

		private TableType(String name, Class<? extends CyIdentifiable> type) {
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
		
		public static TableType fromType(Class<? extends CyIdentifiable> type) {
			if (type == CyNode.class) return NODE_ATTR;
			if (type == CyEdge.class) return EDGE_ATTR;
			if (type == CyNetwork.class) return NETWORK_ATTR;
			
			return null;
		}
	};

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	private static final String NO_NETWORKS = "No Networks Found";
	
	public static final String NETWORK_COLLECTION = "To a Network Collection";
	public static final String NETWORK_SELECTION = "To selected networks only";
	public static final String UNASSIGNED_TABLE = "To an unassigned table";
	
	private CyTableReader reader;
	private final TableImportContext tableImportContext;
	private final CyServiceRegistrar serviceRegistrar;
	
	private CyTable globalTable;
	private boolean byReader;
	private Map<String, CyNetwork> name2NetworkMap;
	private Map<String, CyRootNetwork> name2RootMap;
	private Map<String, String> source2targetColumnMap;
	private boolean networksPresent;
	private List<CyTable> mappedTables;
	
	
	public ListSingleSelection<String> whereImportTable;
	
	@Tunable(
			description = "Where to Import Table Data:",
			gravity = 1.0,
			groups = { "Target Table Data" },
			xorChildren = true, 
			longDescription = "Determines what network(s) the imported table will be associated with (if any).  "+
			                  "A table can be imported into a ```Network Collection```, ```Selected networks``` or ```to an unassigned table```.", 
			exampleStringValue = "To a Network Collection"
	)
	public ListSingleSelection<String> getWhereImportTable() {
		return whereImportTable;
	}
	public void setWhereImportTable(ListSingleSelection<String> chooser) {
		if (chooser != null && chooser != whereImportTable) {
			chooser.addListener(new ListChangeListener<String>() {
				@Override
				public void selectionChanged(ListSelection<String> source) {
					if (tableImportContext != null)
						tableImportContext.setKeyRequired(isKeyMandatory());
				}
				@Override
				public void listChanged(ListSelection<String> source) {
					// Ignore...
				}
			});
		}
		
		whereImportTable = chooser;
	}

	/* --- [ NETWORK_COLLECTION ]------------------------------------------------------------------------------------ */
	
	public ListSingleSelection<String> targetNetworkCollection;
	
	@Tunable(
			description = "Network Collection:",
			groups = { "Target Table Data", "Select a Network Collection" },
			gravity = 2.0,
			xorKey = NETWORK_COLLECTION, 
			longDescription="The network collection to use for the table import", 
			exampleStringValue = "galFiltered.sif"
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
			xorKey = NETWORK_COLLECTION, 
			longDescription="Select whether to import the data as ```Node Table Columns```, ```Edge Table Columns```, or ```Network Table Columns```", 
			exampleStringValue = "Node Table Columns"
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
			listenForChange = { "DataTypeTargetForNetworkCollection", "TargetNetworkCollection" },
			longDescription="The column in the network to use as the merge key", 
			exampleStringValue = "name"
	)
	public ListSingleSelection<String> getKeyColumnForMapping() {
		return keyColumnForMapping;
	}

	public void setKeyColumnForMapping(ListSingleSelection<String> colList) {
		keyColumnForMapping = colList;
	}
	
	@Tunable(
			description = "Case Sensitive Key Values:",
			groups = { "Target Table Data", "Select a Network Collection" },
			gravity = 3.3,
			xorKey = NETWORK_COLLECTION, 
			longDescription="Determines whether capitalization is considered in matching and sorting", 
			exampleStringValue = "false"
	)
	public boolean caseSensitiveNetworkCollectionKeys = true;

	/* --- [ NETWORK_SELECTION ]------------------------------------------------------------------------------------- */
	
	public ListMultipleSelection<String> targetNetworkList;
	
	@Tunable(
			description = "Network List:",
			groups = { "Target Table Data","Select Networks" },
			gravity = 3.1,
			xorKey = NETWORK_SELECTION, 
			longDescription="The list of networks into which the table is imported", 
			exampleStringValue = "all"
	)
	public ListMultipleSelection<String> getTargetNetworkList() {	return targetNetworkList;	}

	public void setTargetNetworkList(ListMultipleSelection<String> list) {
		this.targetNetworkList = list;
		updateKeyColumnForMappingNetworkList();
	}
	
	public ListSingleSelection<TableType> dataTypeTargetForNetworkList;
	
	@Tunable(
			description = "Import Data as:",
			groups = { "Target Table Data", "Select Networks" },
			gravity = 3.2,
			xorKey = NETWORK_SELECTION, 
			longDescription="The data type of the targets", 
			exampleStringValue = "int"
	)
	public ListSingleSelection<TableType> getDataTypeTargetForNetworkList() {	return dataTypeTargetForNetworkList;	}

	public void setDataTypeTargetForNetworkList(ListSingleSelection<TableType> options) {
		dataTypeTargetForNetworkList = options;
		updateKeyColumnForMappingNetworkList();
	}
	
	public ListSingleSelection<String> keyColumnForMappingNetworkList;
	
	@Tunable(
			description = "Key Column for Networks:",
			groups = { "Target Table Data", "Select Networks" },
			gravity = 3.3,
			xorKey = NETWORK_SELECTION,
			listenForChange = { "DataTypeTargetForNetworkList", "TargetNetworkList" }, 
			longDescription="The column in the network to use as the merge key", 
			exampleStringValue = "name"
	)
	public ListSingleSelection<String> getKeyColumnForMappingNetworkList() {	return keyColumnForMappingNetworkList;	}
	
	public void setKeyColumnForMappingNetworkList(ListSingleSelection<String> colList) {	keyColumnForMappingNetworkList = colList;	}
	
	@Tunable(
			description = "Case Sensitive Key Values:",
			groups = { "Target Table Data", "Select Networks" },
			gravity = 3.4,
			xorKey = NETWORK_SELECTION, 
			longDescription="Determines whether capitalization is considered in matching and sorting", 
			exampleStringValue = "false"
	)
	public boolean caseSensitiveNetworkKeys = true;
	
	/* --- [ UNASSIGNED_TABLE ]-------------------------------------------------------------------------------------- */
	
	@Tunable(
			description = "New Table Name:",
			groups = { "Target Table Data", "Set New Table Name" },
			gravity = 5.0,
			xorKey = UNASSIGNED_TABLE, longDescription="The title of the new table", exampleStringValue = "Supplemental Info"
	)
	public String newTableName;
	
//	@Tunable(
//			description = "Network View Renderer:",
//			groups = { "Target Table Data", "Select Renderer" },
//			gravity = 6.0, longDescription="", exampleStringValue = ""
//	)
//	public ListSingleSelection<NetworkViewRenderer> renderers;
	
	@ProvidesTitle
	public String getTitle() {
		return "Import Data";
	}

	public ImportTableDataTask(
			CyTableReader reader,
			TableImportContext tableImportContext,
			CyServiceRegistrar serviceRegistrar
	) {
		this.reader = reader;
		this.tableImportContext = tableImportContext;
		this.serviceRegistrar = serviceRegistrar;
		this.byReader = true;
		init();
	}

	public ImportTableDataTask(
			CyTable globalTable, 
			TableImportContext tableImportContext,
			CyServiceRegistrar serviceRegistrar
	) {
		this.byReader = false;
		this.tableImportContext = tableImportContext;
		this.serviceRegistrar = serviceRegistrar;
		this.globalTable = globalTable;

		init();
	}
	
	private final void init() {
		this.name2NetworkMap = new HashMap<>();
		this.name2RootMap = new HashMap<>();
		this.source2targetColumnMap = new HashMap<>();
		this.mappedTables = new ArrayList<>();

		var appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		var netMgr = serviceRegistrar.getService(CyNetworkManager.class);
		
		if (!netMgr.getNetworkSet().isEmpty()) {
			setWhereImportTable(new ListSingleSelection<>(NETWORK_COLLECTION, NETWORK_SELECTION, UNASSIGNED_TABLE));
			getWhereImportTable().setSelectedValue(NETWORK_COLLECTION);
			networksPresent = true;
		} else {
			setWhereImportTable(new ListSingleSelection<>(UNASSIGNED_TABLE));
			getWhereImportTable().setSelectedValue(UNASSIGNED_TABLE);
		}
		
		// Force-update the TableImportContext!
		if (tableImportContext != null)
			tableImportContext.setKeyRequired(isKeyMandatory());
		
		if (byReader) {
			if (reader != null && reader.getTables() != null)
				newTableName = reader.getTables()[0].getTitle();
		} else {
			newTableName = globalTable.getTitle();
		}
		
		if (networksPresent) {
			var tableType = tableImportContext.getTableType();
			var options = new ArrayList<TableType>();

			if (tableType == null) {
				tableImportContext.setTableType(TableType.NODE_ATTR);
				tableType = tableImportContext.getTableType();
			}
			
			for (var type : TableType.values())
				options.add(type);
			
			var currNet = appMgr.getCurrentNetwork();
			
			dataTypeTargetForNetworkCollection = new ListSingleSelection<>(options);
			dataTypeTargetForNetworkList = new ListSingleSelection<>(options);
			
			if (tableType == null) {
				getWhereImportTable().setSelectedValue(UNASSIGNED_TABLE);
			} else {
				dataTypeTargetForNetworkCollection.setSelectedValue(tableType);
				dataTypeTargetForNetworkList.setSelectedValue(tableType);
			}
	
			for (var net : netMgr.getNetworkSet()) {
				final String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
				name2NetworkMap.put(netName, net);
			}
			
			var names = new ArrayList<String>();
			names.addAll(name2NetworkMap.keySet());
			sort(names);
			
			if (names.isEmpty()) {
				targetNetworkList = new ListMultipleSelection<>(NO_NETWORKS);
			} else {
				targetNetworkList = new ListMultipleSelection<>(names);
				
				if (currNet != null) {
					final String currName = currNet.getRow(currNet).get(CyNetwork.NAME, String.class);
					
					if (currName != null && targetNetworkList.getPossibleValues().contains(currName))
						targetNetworkList.setSelectedValues(Collections.singletonList(currName));
				}
				
				var selectedNetworks = new ArrayList<CyNetwork>();
				
				for (var netName : targetNetworkList.getSelectedValues()) {
					if (name2NetworkMap.containsKey(netName))
						selectedNetworks.add(name2NetworkMap.get(netName));
				}
				
				keyColumnForMappingNetworkList = getColumns(
						selectedNetworks,
						dataTypeTargetForNetworkList.getSelectedValue(),
						CyRootNetwork.DEFAULT_ATTRS
				);
			}
	
			var rootNetMgr = serviceRegistrar.getService(CyRootNetworkManager.class);
			
			for (var net : netMgr.getNetworkSet()) {
				var rootNet = rootNetMgr.getRootNetwork(net);
				
				if (!name2RootMap.containsValue(rootNet))
					name2RootMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
			}
			
			var rootNames = new ArrayList<String>();
			rootNames.addAll(name2RootMap.keySet());
			sort(rootNames);
			targetNetworkCollection = new ListSingleSelection<>(rootNames);
			
			if (!rootNames.isEmpty()) {
				targetNetworkCollection.setSelectedValue(rootNames.get(0));
				var currRootNet = currNet instanceof CySubNetwork ? rootNetMgr.getRootNetwork(currNet) : null;
		
				if (currRootNet != null) {
					var currName = currRootNet.getRow(currRootNet).get(CyNetwork.NAME, String.class);
					
					if (currName != null && targetNetworkCollection.getPossibleValues().contains(currName))
						targetNetworkCollection.setSelectedValue(currName);
				}
						
				keyColumnForMapping = getColumns(
						Collections.singletonList(name2RootMap.get(targetNetworkCollection.getSelectedValue())),
						dataTypeTargetForNetworkCollection.getSelectedValue(),
						CyRootNetwork.SHARED_ATTRS
				);
			}
		}
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (!checkKeys()) {
			if (byReader)
				throw new IllegalArgumentException("Types of keys selected for tables are not valid.\n"
						+ "Keys must be of type Integer, Long, or String.");
			else
				throw new IllegalArgumentException("Types of keys selected for tables are not valid.\n"
						+ "Keys must be of type Integer, Long, or String, and must be the same type for a soft merge.");
		}
		
		if (networksPresent) {
			if (name2RootMap.isEmpty())
				return;
		}

		var where = getWhereImportTable().getSelectedValue();
		var tableType = getDataTypeOptions();
    int rowsCopied = 0;
		
		if (where.matches(NETWORK_COLLECTION)) {
			// Import to shared columns first, because if a column has to be created,
			// we must create them as a shared one, as requested by the user
			rowsCopied = mapTableToDefaultAttrs(tableType);
			
			// Now try to import to local columns as well (but this should not create local columns!)
			var rootNet = name2RootMap.get(targetNetworkCollection.getSelectedValue());
			
      // Why do we need to want to do this?
			if (rootNet != null) {
			 	var networks = rootNet.getSubNetworkList();
			 	mapTableToLocalAttrs(tableType, networks);
			}
      if (rowsCopied > 0)
        tm.setStatusMessage("Copied "+rowsCopied+" rows");
      else {
        tm.setTitle("Possible import error!");
        tm.showMessage(TaskMonitor.Level.ERROR, "No rows copied!! Check that 'Key Column for Network' matches imported key column", 10);
        // try { Thread.sleep(5000); } catch (Exception e) {}
      }
		} else if (where.matches(NETWORK_SELECTION)) {
			if (!targetNetworkList.getSelectedValues().isEmpty()) {
				var networks = new HashSet<CyNetwork>();

				if (!targetNetworkList.getSelectedValues().get(0).equals(NO_NETWORKS)) {
					for (var netName : targetNetworkList.getSelectedValues()) {
						var net = name2NetworkMap.get(netName);

						if (net != null)
							networks.add(net);
					}

					rowsCopied += mapTableToLocalAttrs(tableType, networks);
				}
			}
      if (rowsCopied > 0)
        tm.setStatusMessage("Copied "+rowsCopied+" rows");
      else {
        tm.showMessage(TaskMonitor.Level.WARN, "Copied 0 rows!! Check that 'Key Column for Network' matches imported key column", 2);
        // tm.setStatusMessage("Copied 0 rows!! Check that 'Key Column for Network' matches imported key column");
      }

		} else if (where.matches(UNASSIGNED_TABLE)) {
			addTable();
		}
	}
	
	private void updateKeyColumnForMapping() {
		var tempList = getColumns(
				Collections.singletonList(name2RootMap.get(targetNetworkCollection.getSelectedValue())),
				dataTypeTargetForNetworkCollection.getSelectedValue(),
				CyRootNetwork.SHARED_ATTRS
		);
		
		if (!keyColumnForMapping.getPossibleValues().containsAll(tempList.getPossibleValues())
				|| keyColumnForMapping.getPossibleValues().size() != tempList.getPossibleValues().size())
			keyColumnForMapping = tempList;
	}
	
	private void updateKeyColumnForMappingNetworkList() {
		var selectedNetworks = new ArrayList<CyNetwork>();
		
		for (var netName : targetNetworkList.getSelectedValues()) {
			if (name2NetworkMap.containsKey(netName))
				selectedNetworks.add(name2NetworkMap.get(netName));
		}
		
		var tempList = getColumns(
				selectedNetworks,
				dataTypeTargetForNetworkList.getSelectedValue(),
				CyRootNetwork.DEFAULT_ATTRS
		);
		
		if (!keyColumnForMappingNetworkList.getPossibleValues().containsAll(tempList.getPossibleValues())
				|| keyColumnForMappingNetworkList.getPossibleValues().size() != tempList.getPossibleValues().size())
			keyColumnForMappingNetworkList = tempList;
	}
	
	private ListSingleSelection<String> getColumns(Collection<? extends CyNetwork> networkList, TableType tableType,
			String namespace) {
		Set<ColumnDescriptor> colDescSet = null;
		
		// Get set of columns with same name and type that are common to all networks
		for (var network : networkList) {
			var table = getTable(network, tableType, namespace);
			var subSet = new HashSet<ColumnDescriptor>();
			
			for (var col : table.getColumns()) {
				if (isMappableColumn(col))
					subSet.add(new ColumnDescriptor(col.getName(), col.getType()));
			}
			
			if (colDescSet == null)
				colDescSet = subSet; // First network? Just save the mappable columns...
			else
				colDescSet.retainAll(subSet); // From now on just keep the common columns...
		}
		
		var columnNames = new ArrayList<String>();
		
		if (colDescSet != null) {
			for (var cd : colDescSet)
				columnNames.add(cd.name);
			
			sort(columnNames);
		}
		
		var columns = new ListSingleSelection<String>(columnNames);
		
		if (columns.getPossibleValues().contains(CyRootNetwork.SHARED_NAME))
			columns.setSelectedValue(CyRootNetwork.SHARED_NAME);
		
		return columns;
	}

	private boolean isMappableColumn(CyColumn col) {
		var name = col.getName();
		var type = col.getType();
		
		return (type == Integer.class || type == Long.class || type == String.class) && 
				!name.equals(CyNetwork.SUID) && 
				!name.endsWith(".SUID");
	}
	
	private int mapTableToLocalAttrs(TableType tableType, Collection<? extends CyNetwork> networks) {
    int rowsCopied = 0;
		for (var net : networks) {
			var targetTable = getTable(net, tableType, CyNetwork.LOCAL_ATTRS);
			
			if (targetTable != null) {
				mappedTables.add(targetTable);
				rowsCopied += applyMapping(targetTable, caseSensitiveNetworkKeys);
			}
		}
    return rowsCopied;
	}

	private int mapTableToDefaultAttrs(TableType tableType) {
		var targetTable = getTable(name2RootMap.get(targetNetworkCollection.getSelectedValue()), tableType,
				CyRootNetwork.SHARED_DEFAULT_ATTRS);
		
		if (targetTable != null) {
			mappedTables.add(targetTable);
			return applyMapping(targetTable, caseSensitiveNetworkCollectionKeys);
		}
    return 0;
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

	private int applyMapping(CyTable targetTable, boolean caseSensitive) {
		var columns = new ArrayList<CyColumn>();
		
		if (byReader) {
			if (reader.getTables() != null && reader.getTables().length > 0) {
        int rowsCopied = 0;
				for (CyTable sourceTable : reader.getTables()) {
					columns.addAll(sourceTable.getColumns());
					copyColumns(sourceTable, columns, targetTable, false);
			    rowsCopied = copyRows(sourceTable, columns, targetTable, caseSensitive);
				}
        return rowsCopied;
			}
		} else {
			if (globalTable != null) {
				columns.addAll(globalTable.getColumns());
				copyColumns(globalTable, columns, targetTable, true);
				//copyRows(globalTable, columns, targetTable, caseSensitive);
			}
		}
    return 0;
	}

	private CyColumn getJoinTargetColumn(CyTable targetTable) {
		var joinKeyName = CyNetwork.NAME;
		
		if (getWhereImportTable().getSelectedValue().matches(NETWORK_COLLECTION))
			joinKeyName = keyColumnForMapping.getSelectedValue();
		else if (getWhereImportTable().getSelectedValue().matches(NETWORK_SELECTION))
			joinKeyName = keyColumnForMappingNetworkList.getSelectedValue();
		
		return targetTable.getColumn(joinKeyName);
	}

	private int copyRows(CyTable sourceTable, List<CyColumn> sourceColumns, CyTable targetTable,
			boolean caseSensitive) {
		var targetKeyColumn = getJoinTargetColumn(targetTable);
		var normalizedSourceKeys = new HashMap<String, String>();

		if (!caseSensitive) {
			var pk = sourceTable.getPrimaryKey();

			if (pk.getType() == String.class) {
				for (var row : sourceTable.getAllRows()) {
					var key = row.get(pk.getName(), String.class);

					if (key != null)
						normalizedSourceKeys.put(key.toLowerCase().trim(), key);
				}
			}
		}
		
    int rowsCopied = 0;
		for (var targetRow : targetTable.getAllRows()) {
			var key = targetRow.get(targetKeyColumn.getName(), targetKeyColumn.getType());

			if (key == null)
				continue;

			if (!caseSensitive)
				key = normalizedSourceKeys.get(key.toString().toLowerCase().trim());

			if (key == null)
				continue;

			if (key.getClass() != sourceTable.getPrimaryKey().getType()) {
				try {
					key = DataUtils.convertString(key.toString(), sourceTable.getPrimaryKey().getType());
				} catch (Exception e) {
					continue;
				}
			}

			if (key == null || !sourceTable.rowExists(key))
				continue;

			var sourceRow = sourceTable.getRow(key);

			for (var col : sourceColumns) {
				if (col == sourceTable.getPrimaryKey())
					continue;

				var targetColName = source2targetColumnMap.get(col.getName());

				if (targetColName != null && targetTable.getColumn(targetColName) != null) {
					targetRow.set(targetColName, sourceRow.getRaw(col.getName()));
          rowsCopied++;
        }
			}
		}
    return rowsCopied;
	}

	private void copyColumns(CyTable sourceTable, List<CyColumn> sourceColumns, CyTable targetTable, boolean addVirtual) {
		var rootNet = name2RootMap.get(targetNetworkCollection.getSelectedValue());
		var columnNames = rootNet != null ? getAllColumnNames(rootNet) : Collections.emptySet();
		
		for (var col : sourceColumns) {
			if (col == sourceTable.getPrimaryKey())
				continue;
			
			var name = col.getName();
			boolean exists = targetTable.getColumn(name) != null;
			
			// Also check if the column exists in the root network or other subnetworks in the same collection
			if (!exists)
				exists = columnNames.contains(name.toLowerCase());
			
			if (!exists) {
				if (!addVirtual) {
					if (col.getType() == List.class)
						targetTable.createListColumn(name, col.getListElementType(), col.isImmutable());
					else
						targetTable.createColumn(name, col.getType(), col.isImmutable(), col.getDefaultValue());
				} else {
					targetTable.addVirtualColumn(name, col.getName(), sourceTable, getJoinTargetColumn(targetTable).getName(), false);
				}
			} else {
				var targetCol = targetTable.getColumn(name);
				
				if (targetCol != null // TODO what if it's null because it's not a shared table column, but a local one?
						&& (targetCol.getType() != col.getType()) ||
				    (col.getType() == List.class && (targetCol.getListElementType() != col.getListElementType()))) {
					logger.error("Column '" + name + "' has a different type in the target table -- skipping column");
					
					continue;
				}
			}

			source2targetColumnMap.put(col.getName(), name);
		}
	}
	
	/**
	 * @return All column names in lower case.
	 */
	private Set<String> getAllColumnNames(CyRootNetwork rootNet) {
		var tables = new ArrayList<CyTable>();
		var type = getDataTypeTargetForNetworkCollection().getSelectedValue().type;
		var netTableMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		var rootTbl = netTableMgr.getTable(rootNet, type, CyNetwork.LOCAL_ATTRS);
		
		if (rootTbl != null)
			tables.add(rootTbl);
		
		for (var sub : rootNet.getSubNetworkList()) {
			var netTbl = netTableMgr.getTable(sub, type, CyNetwork.LOCAL_ATTRS);
			
			if (netTbl != null)
				tables.add(netTbl);
		}
		
		var set = new HashSet<String>();
		
		for (var table: tables) {
			var columns = table.getColumns();
			
			for (var col : columns)
				set.add(col.getName().toLowerCase());
		}
		
		return set;
	}
	
	private void addTable(){
		var tableMgr = serviceRegistrar.getService(CyTableManager.class);
		CyTable currentTable = null;
		
		if (byReader) {
			if (reader != null && reader.getTables() != null) {
				for (var table : reader.getTables()) {
					if (newTableName != null && !newTableName.isEmpty())
						table.setTitle(newTableName);
					
					currentTable = table;
					tableMgr.addTable(table);
				}
			} else {
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
			
			if (newTableName != null && !newTableName.isEmpty())
				globalTable.setTitle(newTableName);
			
			currentTable = globalTable;
			tableMgr.addTable(globalTable);
			mappedTables.add(globalTable);
		}
		
		if (currentTable != null && currentTable.isPublic())
			serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(currentTable);
	}

	@Override
	public List<Class<?>> getResultClasses() {	
		return Arrays.asList(List.class, String.class, JSONResult.class);	
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getResults(Class requestedType) {
		if (requestedType.equals(List.class))
			return mappedTables;
		if (requestedType.equals(String.class)) {
			String str = "Mapped to tables:\n";
			for (CyTable table: mappedTables) {
				str += "   "+table.toString()+"\n";
			}
			return str;
		}
		if (requestedType.equals(JSONResult.class)) {
			CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
			JSONResult res = () -> {		
				if (mappedTables.isEmpty()) return "{}";
				return "{\"mappedTables\":"+cyJSONUtil.cyIdentifiablesToJson(mappedTables)+"}";
			};
			return res;
		}
		return null;
	}

	private TableType getDataTypeOptions() {
		if (getWhereImportTable().getSelectedValue().matches(NETWORK_COLLECTION)) {
			if (dataTypeTargetForNetworkCollection != null)
				return dataTypeTargetForNetworkCollection.getSelectedValue();
			return null;
		}

		if (dataTypeTargetForNetworkList != null)
			return dataTypeTargetForNetworkList.getSelectedValue();
		
		return null;
	}

	private boolean isKeyMandatory() {
		return !getWhereImportTable().getSelectedValue().matches(UNASSIGNED_TABLE);
	}
	
	public boolean checkKeys() {
		List<CyColumn> joinTargetColumns = new ArrayList<>();

		if (getWhereImportTable().getSelectedValue().matches(NETWORK_COLLECTION)) {
			joinTargetColumns.add(getJoinTargetColumn(
					getTable(name2RootMap.get(targetNetworkCollection.getSelectedValue()), getDataTypeOptions(),
							CyNetwork.DEFAULT_ATTRS)));
		} else if (getWhereImportTable().getSelectedValue().matches(NETWORK_SELECTION)) {
			for (String targetNetwork: targetNetworkList.getSelectedValues()) {
				joinTargetColumns.add(getJoinTargetColumn(
						getTable(name2NetworkMap.get(targetNetwork), getDataTypeOptions(),
								CyNetwork.DEFAULT_ATTRS)));
			}
		}

		if (byReader) {
			if (tableImportContext.isKeyRequired()) {
				for (CyTable readerTable : reader.getTables()) {
					if (!isMappableColumn(readerTable.getPrimaryKey()))
						return false;
				}
			}

			for (CyColumn joinTargetColumn : joinTargetColumns) {
				if (!isMappableColumn(joinTargetColumn))
					return false;
			}
		} else {
			if (tableImportContext.isKeyRequired() && !isMappableColumn(globalTable.getPrimaryKey()))
				return false;

			// Don't need to check if mappable since equality implies this
			for (CyColumn joinTargetColumn : joinTargetColumns) {
				if (joinTargetColumn.getType() != globalTable.getPrimaryKey().getType())
					return false;
			}
		}

		return true;
	}
	
	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if (getWhereImportTable().getSelectedValue().matches(NETWORK_SELECTION) && 
				targetNetworkList.getSelectedValues().isEmpty()) {
			try {
				errMsg.append("Please select at least one network.");
				return ValidationState.INVALID;
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
		}
		
		if (!getWhereImportTable().getSelectedValue().matches(UNASSIGNED_TABLE)
				|| newTableName == null || newTableName.isEmpty())
			return ValidationState.OK;

		var tableMgr = serviceRegistrar.getService(CyTableManager.class);

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
	
	private void sort(List<String> names) {
		var collator = Collator.getInstance(Locale.getDefault());
		
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
	}
	
	private static class ColumnDescriptor {
		
		private final String name;
		private final Class<?> type;
		
		ColumnDescriptor(String name, Class<?> type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 7;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((type == null || type.getCanonicalName() == null) ? 0 : type.getCanonicalName().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof ColumnDescriptor)) return false;
			
			var other = (ColumnDescriptor) obj;
			
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (type == null) {
				if (other.type != null) return false;
			} else if (type != other.type) {
				return false;
			}
			
			return true;
		}
	}
}
