package org.cytoscape.task.internal.table;

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

public class JoinTablesTask extends AbstractTask {
	
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


	public ListSingleSelection<TableType> dataTypeOptions;

	@Tunable(description = "Import Data as:")
	public ListSingleSelection<TableType> getDataTypeOptions() {
		return dataTypeOptions;
	}

	public void setDataTypeOptions(ListSingleSelection<TableType> options) {
		columnList = getColumns(name2RootMap.get(rootNetworkList.getSelectedValue()),
				dataTypeOptions.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
	}

	public ListSingleSelection<String> rootNetworkList;

	@Tunable(description = "Network Collection", groups = "Select a Network Collection", dependsOn = "SelectedNetworksOnly=false", params = "displayState=uncollapsed")
	public ListSingleSelection<String> getRootNetworkList() {
		return rootNetworkList;
	}

	public void setRootNetworkList(ListSingleSelection<String> roots) {
		columnList = getColumns(name2RootMap.get(rootNetworkList.getSelectedValue()),
				dataTypeOptions.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
	}

	public ListSingleSelection<String> columnList;

	@Tunable(description = "Key Column for Network:", groups = "Select a Network Collection", listenForChange = {
			"DataTypeOptions", "RootNetworkList" }, dependsOn = "SelectedNetworksOnly=false", params = "displayState=uncollapsed")
	public ListSingleSelection<String> getColumnList() {
		return columnList;
	}

	public void setColumnList(ListSingleSelection<String> colList) {
		this.columnList = colList;
	}

	public boolean selectedNetworksOnly = false;

	@Tunable(description = "Or apply to selected networks only")
	public boolean getSelectedNetworksOnly() {
		return selectedNetworksOnly;
	}

	public void setSelectedNetworksOnly(boolean selectedOnly) {
		this.selectedNetworksOnly = selectedOnly;
	}

	public ListMultipleSelection<String> networkList;

	@Tunable(description = "Network List", groups = "Select Networks", dependsOn = "SelectedNetworksOnly=true", params = "displayState=collapsed")
	public ListMultipleSelection<String> getNetworkList() {
		return networkList;
	}

	public void setNetworkList(ListMultipleSelection<String> list) {
		this.networkList = list;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Import Data ";
	}



	public JoinTablesTask(CyTableReader reader, CyRootNetworkManager rootNetworkManeger, CyNetworkManager networkManager) {
		this.reader = reader;
		this.byReader = true;
		this.globalTable = null;

		init(rootNetworkManeger, networkManager);
	}

	public JoinTablesTask(CyTable globalTable, CyRootNetworkManager rootNetworkManeger, CyNetworkManager networkManager) {
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

		selectedNetworksOnly = false;

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
		rootNetworkList = new ListSingleSelection<String>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));

		columnList = getColumns(name2RootMap.get(rootNetworkList.getSelectedValue()),
				dataTypeOptions.getSelectedValue(), CyRootNetwork.SHARED_ATTRS);
	}

	public ListSingleSelection<String> getColumns(CyNetwork network, TableType tableType, String namespace) {
		CyTable selectedTable = getTable(network, tableType, CyRootNetwork.SHARED_ATTRS);

		List<String> colNames = new ArrayList<String>();
		for (CyColumn col : selectedTable.getColumns())
			colNames.add(col.getName());

		ListSingleSelection<String> columns = new ListSingleSelection<String>(colNames);
		return columns;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		TableType tableType = dataTypeOptions.getSelectedValue();

		if (!checkKeys()) {
			throw new IllegalArgumentException("Types of keys selected for tables are not matching.");
		}

		if (!selectedNetworksOnly)
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
		CyTable targetTable = getTable(name2RootMap.get(rootNetworkList.getSelectedValue()), tableType,
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
		if (byReader) {
			if (reader.getTables() != null && reader.getTables().length > 0) {
				for (CyTable sourceTable : reader.getTables()) {

					copyColumns(sourceTable, targetTable);
					copyRows(sourceTable, targetTable);
				}
			}
		} else {
			copyColumns(globalTable, targetTable);
			copyRows(globalTable, targetTable);
		}

	}

	private CyColumn getJoinTargetColumn(CyTable targetTable) {
		String joinKeyName = CyNetwork.NAME;
		if (!selectedNetworksOnly)
			joinKeyName = columnList.getSelectedValue();
		return targetTable.getColumn(joinKeyName);
	}

	private void copyRows(CyTable sourceTable, CyTable targetTable) {
		CyColumn targetKeyColumn = getJoinTargetColumn(targetTable);

		for (CyRow targetRow : targetTable.getAllRows()) {
			Object key = targetRow.get(targetKeyColumn.getName(), targetKeyColumn.getType());

			if (!sourceTable.rowExists(key))
				continue;

			CyRow sourceRow = sourceTable.getRow(key);

			if (sourceRow == null)
				continue;

			for (CyColumn col : sourceTable.getColumns()) {
				if (col == sourceTable.getPrimaryKey())
					continue;

				String targetColName = source2targetColumnMap.get(col.getName());

				if (col.getType() == List.class)
					targetRow.set(targetColName, sourceRow.getList(col.getName(), col.getListElementType()));
				else
					targetRow.set(targetColName, sourceRow.get(col.getName(), col.getType()));

			}
		}

	}

	private void copyColumns(CyTable sourceTable, CyTable targetTable) {

		for (CyColumn col : sourceTable.getColumns()) {
			if (col == sourceTable.getPrimaryKey())
				continue;
			String targetColName = getUniqueColumnName(targetTable, col.getName());

			source2targetColumnMap.put(col.getName(), targetColName);
			if (col.getType() == List.class)
				targetTable.createListColumn(targetColName, col.getListElementType(), col.isImmutable());
			else
				targetTable.createColumn(targetColName, col.getType(), col.isImmutable(), col.getDefaultValue());
		}
	}

	private String getUniqueColumnName(CyTable table, final String preferredName) {
		if (table.getColumn(preferredName) == null)
			return preferredName;

		String newUniqueName;
		int i = 0;
		do {
			++i;
			newUniqueName = preferredName + "-" + i;
		} while (table.getColumn(newUniqueName) != null);

		return newUniqueName;
	}

	public boolean checkKeys() {

		Class<?> joinTargetColumnType = String.class;
		if (!selectedNetworksOnly)
			joinTargetColumnType = getJoinTargetColumn(
					getTable(name2RootMap.get(rootNetworkList.getSelectedValue()), dataTypeOptions.getSelectedValue(),
							CyNetwork.DEFAULT_ATTRS)).getType();
		if (byReader) {
			for (CyTable readerTable : reader.getTables())
				if (readerTable.getPrimaryKey().getType() != joinTargetColumnType)
					return false;

		} else if (globalTable.getPrimaryKey().getType() != joinTargetColumnType)
			return false;

		return true;
	}

}
