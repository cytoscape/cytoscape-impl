package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


public final class MapTableToNetworkTablesTask extends AbstractTask {
	private static enum TableType {
		NODE_ATTR("Node Attributes"), EDGE_ATTR("Edge Attributes"), NETWORK_ATTR("Network Attributes"), GLOBAL("Tables");

		private final String name;

		private TableType(final String name) {
			this.name = name;
		}

		@Override public String toString() {
			return name;
		}
	};
	
	private final CyNetworkManager networkManager;
	private final CyTable globalTable;
	private final  CyTableReader reader;
	private final boolean byReader;
	
	@Tunable(description = "Import Data To:")
	public ListSingleSelection<TableType> dataTypeOptions;

	@ProvidesTitle
	public String getTitle() {
		return "Import Data";
	}

	public MapTableToNetworkTablesTask(final CyNetworkManager networkManager, final CyTableReader reader)
	{
		this.reader = reader;
		globalTable = null;
		this.byReader = true;
		this.networkManager = networkManager;
		initTunable();
	}
	
	public MapTableToNetworkTablesTask(final CyNetworkManager networkManager, final CyTable globalTable){
		this.networkManager = networkManager;
		this.globalTable = globalTable;
		this.byReader = false;
		this.reader = null;
		
		initTunable();
	}
	
	private void initTunable(){
		final List<TableType> options = new ArrayList<TableType>();
		if (networkManager.getNetworkSet().size() > 0 ) {
			for(TableType type: TableType.values())
				options.add(type);
		} else
			options.add(TableType.GLOBAL);

		dataTypeOptions = new ListSingleSelection<TableType>(options);
		dataTypeOptions.setSelectedValue(TableType.NODE_ATTR);
	}
	
	
	public void run(TaskMonitor taskMonitor) throws Exception {	

		TableType tableType = dataTypeOptions.getSelectedValue();
		if (tableType != tableType.GLOBAL ){ //The table has already been added to the global tables
			
			for (CyNetwork network: networkManager.getNetworkSet()){
				CyTable targetTable = getTable(network, tableType);
				if (targetTable != null){
					if(byReader){
						if (reader.getTables() != null && reader.getTables().length >0){
							for(CyTable sourceTable : reader.getTables())
								mapTable(targetTable, sourceTable);
						}
					}else{
						mapTable(targetTable, globalTable);
					}
				}
			}
		}
	}


	
	private CyTable getTable(CyNetwork network, TableType tableType){
		if (tableType == TableType.NODE_ATTR)
			return network.getDefaultNodeTable();
		if (tableType == TableType.EDGE_ATTR)
			return network.getDefaultEdgeTable();
		if (tableType == TableType.NETWORK_ATTR)
			return network.getDefaultNetworkTable();
		
		return null;
	}
	
	private void mapTable(final CyTable localTable, final CyTable globalTable) {
		if (globalTable.getPrimaryKey().getType() != String.class)
			throw new IllegalStateException("Local table's primary key should be type String!");

		final CyColumn trgCol = localTable.getColumn(CyNetwork.NAME);
		if (trgCol != null)
			localTable.addVirtualColumns(globalTable, CyNetwork.NAME, false);
	}
}
