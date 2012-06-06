package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;

/**
 * This class is for updating the tables for a newly added network 
 * and map the global tables that are required to to the specified
 * tables. The core-task-impl package may not be the best place for
 * this class. In case this class needs to be moved to another package
 * an event needs to be added which is fired when an imported table
 * is mapped to all network related tables.
 * @author rozagh
 *
 */

public class UpdateAddedNetworkAttributes implements NetworkAddedListener{

	private static Map<CyTable, List< Class<? extends CyIdentifiable>>> tableMappings;
	private final MapGlobalToLocalTableTaskFactory mappingTF;
	private final SynchronousTaskManager<?> syncTaskManager;

	public UpdateAddedNetworkAttributes(MapGlobalToLocalTableTaskFactory mappingTF, final SynchronousTaskManager<?> syncTaskManager){
		tableMappings = new HashMap<CyTable, List<Class<? extends CyIdentifiable>>>();
		this.mappingTF = mappingTF;
		this.syncTaskManager = syncTaskManager;
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		for (CyTable mappedTable: tableMappings.keySet()){
			for( Class<? extends CyIdentifiable> tableType: tableMappings.get(mappedTable)){
				if(! tableAlreadyMapped( getTable(e.getNetwork(),tableType), mappedTable )){
					List<CyTable> targetTables = new ArrayList<CyTable>();
					targetTables.add( getTable(e.getNetwork(),tableType));
					syncTaskManager.execute(mappingTF.createTaskIterator(mappedTable, targetTables));
					}
			}
		}

	}

	private boolean tableAlreadyMapped(CyTable sourceTable, CyTable mappedTable) {
		for(CyColumn col:sourceTable.getColumns() ){
			if (col.getVirtualColumnInfo().isVirtual())
				if (col.getVirtualColumnInfo().getSourceTable().equals(mappedTable))
					return true;
		}
		return false;
	}

	final static void addMappingToList(CyTable importedTable,  Class<? extends CyIdentifiable> mappedTableType){
		//When an imported table is mapped add it to the list with the type of table it has been mapped to.
		if (!tableMappings.containsKey(importedTable))
			tableMappings.put(importedTable, new ArrayList< Class<? extends CyIdentifiable>>());

		tableMappings.get(importedTable).add(mappedTableType);
	}

	private CyTable getTable(CyNetwork network,  Class<? extends CyIdentifiable> tableType){
		if (tableType == CyNode.class)
			return network.getDefaultNodeTable();
		if (tableType == CyEdge.class)
			return network.getDefaultEdgeTable();
		if (tableType == CyNetwork.class)
			return network.getDefaultNetworkTable();
		return null;
	}

}
