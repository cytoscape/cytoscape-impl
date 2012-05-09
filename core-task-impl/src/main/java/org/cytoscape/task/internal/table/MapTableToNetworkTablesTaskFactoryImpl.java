package org.cytoscape.task.internal.table;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListSingleSelection;

public final class MapTableToNetworkTablesTaskFactoryImpl extends AbstractTableTaskFactory implements MapTableToNetworkTablesTaskFactory{
	
	
	private final CyNetworkManager networkManager;
	private final TunableSetter tunableSetter; 
	
	public MapTableToNetworkTablesTaskFactoryImpl( final CyNetworkManager networkManager, final TunableSetter tunableSetter ){
	
		this.networkManager = networkManager;
		this.tunableSetter = tunableSetter;
	}

	@Override
	public TaskIterator createTaskIterator(CyTable globalTable) {
		return new TaskIterator(new MapTableToNetworkTablesTask(networkManager, globalTable));
	}

	@Override
	public TaskIterator createTaskIterator(CyTable globalTable, String tableType) {
		ListSingleSelection<String> tableTypes = new ListSingleSelection<String>(tableType);
		tableTypes.setSelectedValue(tableType);
		
		final Map<String, Object> m = new HashMap<String, Object>();
		
		m.put("dataTypeOptions", tableTypes);
		
		return tunableSetter.createTaskIterator(createTaskIterator(globalTable), m);
		
	}


}
