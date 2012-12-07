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

public final class MapTableToNetworkTablesTask extends AbstractTask {

	enum TableType {
		NODE_ATTR("Node Table", CyNode.class), EDGE_ATTR("Edge Table", CyEdge.class), NETWORK_ATTR("Network Table", CyNetwork.class), GLOBAL("Unassigned Tables", CyTable.class);

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

	private static Logger logger = LoggerFactory.getLogger(MapTableToNetworkTablesTask.class);
	private static String NO_NETWORKS = "No Networks Found";
	private final CyNetworkManager networkManager;
	private final CyRootNetworkManager rootNetworkManager;
	private final CyTable globalTable;
	private final  CyTableReader reader;
	private final boolean byReader;
	private Map<String, CyNetwork> name2NetworkMap;


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


	public MapTableToNetworkTablesTask(final CyNetworkManager networkManager, final CyTableReader reader, final CyRootNetworkManager rootNetworkManager ){
		this.reader = reader;
		globalTable = null;
		this.byReader = true;
		this.networkManager = networkManager;
		this.name2NetworkMap = new HashMap<String, CyNetwork>();
		this.rootNetworkManager = rootNetworkManager;
		
		initTunable(networkManager);

	}

	public MapTableToNetworkTablesTask(final CyNetworkManager networkManager, final CyTable globalTable, final CyRootNetworkManager rootNetworkManager){
		this.networkManager = networkManager;
		this.globalTable = globalTable;
		this.byReader = false;
		this.reader = null;
		this.name2NetworkMap = new HashMap<String, CyNetwork>();
		this.rootNetworkManager = rootNetworkManager;

		initTunable(networkManager);
	}

	private void initTunable(CyNetworkManager networkManage){
		final List<TableType> options = new ArrayList<TableType>();
		for(TableType type: TableType.values())
			options.add(type);
		dataTypeOptions = new ListSingleSelection<TableType>(options);
		dataTypeOptions.setSelectedValue(TableType.NODE_ATTR);

		for(CyNetwork net: networkManage.getNetworkSet()){
			String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
			name2NetworkMap.put(netName, net);
		}
		List<String> names = new ArrayList<String>();
		names.addAll(name2NetworkMap.keySet());
		if(names.isEmpty())
			networkList = new ListMultipleSelection<String>(NO_NETWORKS);
		else
			networkList = new ListMultipleSelection<String>(names);

	}


	public void run(TaskMonitor taskMonitor) throws Exception {	
		TableType tableType = dataTypeOptions.getSelectedValue();
		if (tableType == TableType.GLOBAL )
			return;

		if (!selectedNetworksOnly)
			mapTableToDefaultAttrs (tableType);
		else
			mapTableToLocalAttrs (tableType);		 
	}



	private void mapTableToLocalAttrs(TableType tableType) {
		List<CyNetwork> networks = new ArrayList<CyNetwork>();

		if(!networkList.getSelectedValues().get(0).equals(NO_NETWORKS))
			for(String netName: networkList.getSelectedValues())
				networks.add(name2NetworkMap.get(netName));

		for (CyNetwork network: networks){
			CyTable targetTable = getTable(network, tableType, CyNetwork.LOCAL_ATTRS);
			if (targetTable != null)
				applyMapping(targetTable);
		}

	}


	private void mapTableToDefaultAttrs(TableType tableType) {
		List<CyRootNetwork> rootNetworkList = new ArrayList<CyRootNetwork>();
		for (CyNetwork net : networkManager.getNetworkSet())
			if (! rootNetworkList.contains(rootNetworkManager.getRootNetwork(net)))
				rootNetworkList.add( rootNetworkManager.getRootNetwork(net));

		for (CyRootNetwork root: rootNetworkList ){
			CyTable targetTable = getTable(root, tableType, CyNetwork.DEFAULT_ATTRS);
			if (targetTable != null){
				applyMapping(targetTable);
			}
		}
	}


	private CyTable getTable(CyNetwork network, TableType tableType, String namespace){
		if (tableType == TableType.NODE_ATTR)
			return network.getTable(CyNode.class, namespace);
		if (tableType == TableType.EDGE_ATTR)
			return network.getTable(CyEdge.class, namespace);
		if (tableType == TableType.NETWORK_ATTR)
			return network.getTable(CyNetwork.class, namespace);

		logger.warn("The selected table type is not valie. \nTable needs to be one of these types: " +TableType.NODE_ATTR +", " + TableType.EDGE_ATTR  + ", "+ TableType.NETWORK_ATTR +" or "+TableType.GLOBAL +".");
		return null;
	}


	private void applyMapping(CyTable targetTable){
		if(byReader){
			if (reader.getTables() != null && reader.getTables().length >0){
				for(CyTable sourceTable : reader.getTables())
					mapTable(targetTable, sourceTable);
			}
		}else
			mapTable(targetTable, globalTable);
	}

	private void mapTable(final CyTable localTable, final CyTable globalTable) {
		if (globalTable.getPrimaryKey().getType() != String.class)
			throw new IllegalStateException("Local table's primary key should be type String.");

		final CyColumn trgCol = localTable.getColumn(CyRootNetwork.SHARED_NAME);
		if (trgCol != null){
			localTable.addVirtualColumns(globalTable, CyRootNetwork.SHARED_NAME, false);
			globalTable.setPublic(false);
		}
		else
			logger.warn("Name column in the target table was not found.");
	}
}
