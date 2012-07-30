package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTask.TableType;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

public final class MapTableToNetworkTablesTaskFactoryImpl extends AbstractTableTaskFactory implements MapTableToNetworkTablesTaskFactory{
	
	
	private final CyNetworkManager networkManager;
	private final TunableSetter tunableSetter; 
	private final UpdateAddedNetworkAttributes updateAddedNetworkAttributes;
	
	public MapTableToNetworkTablesTaskFactoryImpl( final CyNetworkManager networkManager, final TunableSetter tunableSetter, final UpdateAddedNetworkAttributes updateAddedNetworkAttributes ){
	
		this.networkManager = networkManager;
		this.tunableSetter = tunableSetter;
		this.updateAddedNetworkAttributes = updateAddedNetworkAttributes;
	}

	@Override
	public TaskIterator createTaskIterator(CyTable globalTable) {
		return new TaskIterator(new MapTableToNetworkTablesTask(networkManager, globalTable, updateAddedNetworkAttributes));
	}

	@Override
	public TaskIterator createTaskIterator(CyTable globalTable, boolean selectedNetworksOnly, List<CyNetwork> networksList,  Class<? extends CyIdentifiable> type ) {
		
		TableType tableType = getTableType(type);
		if(tableType == null)
			throw new IllegalArgumentException("The specified type " + type + " is not acceptable.");
		ListSingleSelection<TableType> tableTypes = new ListSingleSelection<TableType>(tableType);
		tableTypes.setSelectedValue(tableType);
		
		List<String> networkNames = new ArrayList<String>();
		for(CyNetwork net: networksList){
			networkNames.add(net.getRow(net).get(CyNetwork.NAME, String.class));
		}
	
		ListMultipleSelection<String> networksListTunable = new ListMultipleSelection<String>(networkNames);
		networksListTunable.setSelectedValues(networkNames);
		
		final Map<String, Object> m = new HashMap<String, Object>();
		
		m.put("dataTypeOptions", tableTypes);
		m.put("selectedNetworksOnly", selectedNetworksOnly);
		m.put("networkList", networksListTunable);
		
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
