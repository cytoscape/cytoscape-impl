package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.edit.JoinTablesTaskTaskFactory;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTask.TableType;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

public class JoinTablesTaskTaskFactoryImpl extends AbstractTableTaskFactory implements JoinTablesTaskTaskFactory {
	
	private final CyNetworkManager networkManager;
	private final TunableSetter tunableSetter; 
	private final CyRootNetworkManager rootNetMgr;
	
	public JoinTablesTaskTaskFactoryImpl( final CyNetworkManager networkManager, final TunableSetter tunableSetter, final CyRootNetworkManager rootNetMgr){
		this.networkManager = networkManager;
		this.tunableSetter = tunableSetter;
		this.rootNetMgr = rootNetMgr;
	}
	
	
	@Override
	public TaskIterator createTaskIterator(CyTable table) {
		return new TaskIterator(new JoinTablesTask(table, rootNetMgr, networkManager));
	}


	@Override
	public TaskIterator createTaskIterator(CyTable globalTable,
			boolean selectedNetworksOnly, List<CyNetwork> networkList,
			CyRootNetwork rootNetwork, CyColumn targetJoinColumn,
			Class<? extends CyIdentifiable> type) {
		
		
		TableType tableType = getTableType(type);
		if(tableType == null)
			throw new IllegalArgumentException("The specified type " + type + " is not acceptable.");
		ListSingleSelection<TableType> tableTypes = new ListSingleSelection<TableType>(tableType);
		tableTypes.setSelectedValue(tableType);
		
		List<String> networkNames = new ArrayList<String>();
		for(CyNetwork net: networkList){
			networkNames.add(net.getRow(net).get(CyNetwork.NAME, String.class));
		}
	
		ListMultipleSelection<String> networksListTunable = new ListMultipleSelection<String>(networkNames);
		networksListTunable.setSelectedValues(networkNames);
		
		List<String> rootNetworkNames = new ArrayList<String>();
		ListSingleSelection<String> rootNetworkList = new ListSingleSelection<String>();
		if (rootNetwork != null){
			rootNetworkNames.add( rootNetwork.getRow(rootNetwork).get(CyNetwork.NAME, String.class));
			rootNetworkList = new ListSingleSelection<String>(rootNetworkNames);
			rootNetworkList.setSelectedValue(rootNetworkNames.get(0));
		}

		List<String> columnNames = new ArrayList<String>();
		ListSingleSelection<String> columnNamesList = new ListSingleSelection<String>();
		if (targetJoinColumn != null){
			columnNames.add(targetJoinColumn.getName());
			columnNamesList = new ListSingleSelection<String>(columnNames);
			columnNamesList.setSelectedValue(columnNames.get(0));
		}
		
		final Map<String, Object> m = new HashMap<String, Object>();
		
		m.put("DataTypeOptions", tableTypes);
		m.put("SelectedNetworksOnly", selectedNetworksOnly);
		m.put("NetworkList", networksListTunable);
		m.put("ColumnList", columnNamesList);
		m.put("RootNetworkList", rootNetworkList);
		
		return tunableSetter.createTaskIterator(createTaskIterator(globalTable), m);
		
	}

	private TableType getTableType( Class<? extends CyIdentifiable> type) {
		if (type.equals(TableType.GLOBAL.getType()))
			return TableType.GLOBAL;
		if (type.equals(TableType.EDGE_ATTR.getType()))
			return TableType.EDGE_ATTR;
		if (type.equals(TableType.NETWORK_ATTR.getType()))
			return TableType.NETWORK_ATTR;
		if (type.equals(TableType.NODE_ATTR.getType()))
			return TableType.NODE_ATTR;
		return null;
	}
}
