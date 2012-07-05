package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

/**
 * Very simple mapping function. Simply map all available columns in the global
 * table to selected local one.
 * 
 */
public final class MapGlobalToLocalTableTask extends AbstractTask {

	private final CyTable globalTable;

	@Tunable(description = "Link Selected Table to:")
	public ListMultipleSelection<String> localTables;

	private final Map<String, CyTable> name2tableMap;

	MapGlobalToLocalTableTask(final CyTable globalTable, final CyTableManager tableManager, final CyNetworkManager networkManager) {

		final Set<CyNetwork> allNetworks = networkManager.getNetworkSet();
		if (globalTable == null)
			throw new IllegalStateException("No Global Table is selected.");

		if (allNetworks.size() == 0)
			throw new IllegalStateException("No network in current session.  You need at least one network.");

		this.name2tableMap = new HashMap<String, CyTable>();
		this.globalTable = globalTable;
		

		final List<String> locals = new ArrayList<String>();
		
		for (final CyNetwork network : allNetworks) {
			locals.add(network.getDefaultNodeTable().getTitle());
			name2tableMap.put(network.getDefaultNodeTable().getTitle(), network.getDefaultNodeTable());
			
			locals.add(network.getDefaultEdgeTable().getTitle());
			name2tableMap.put(network.getDefaultEdgeTable().getTitle(), network.getDefaultEdgeTable());
			
			locals.add(network.getDefaultNetworkTable().getTitle());
			name2tableMap.put(network.getDefaultNetworkTable().getTitle(), network.getDefaultNetworkTable());
		}
		name2tableMap.put(this.globalTable.getTitle(), this.globalTable);

		this.localTables = new ListMultipleSelection<String>(locals);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		for(String selected : localTables.getSelectedValues()){
			final CyTable localTable = name2tableMap.get(selected);
			mapTable(localTable, globalTable);
		}
	}

	private void mapTable(final CyTable localTable, final CyTable globalTable) {
		if (globalTable.getPrimaryKey().getType() != String.class)
			throw new IllegalStateException("Local table's primary key should be type String.");

		final CyColumn trgCol = localTable.getColumn(CyNetwork.NAME);
		if (trgCol != null)
			localTable.addVirtualColumns(globalTable, CyNetwork.NAME, false);
	}

}
