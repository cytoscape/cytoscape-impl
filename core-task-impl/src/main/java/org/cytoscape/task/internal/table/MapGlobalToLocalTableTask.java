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
import org.cytoscape.work.util.ListSingleSelection;

/**
 * Very simple mapping function. Simply map all available columns in the global
 * table to selected local one.
 * 
 */
public final class MapGlobalToLocalTableTask extends AbstractTask {

	@Tunable(description = "Select a Global Table:")
	public ListSingleSelection<String> globalTables;

	@Tunable(description = "Map selected Global Table to:")
	public ListSingleSelection<String> localTables;

	private final Map<String, CyTable> name2tableMap;

	MapGlobalToLocalTableTask(final CyTableManager tableManager, final CyNetworkManager networkManager) {

		final Set<CyNetwork> allNetworks = networkManager.getNetworkSet();
		if (allNetworks.size() == 0)
			throw new IllegalStateException("No network in current session.  You need at least one network.");

		this.name2tableMap = new HashMap<String, CyTable>();

		final List<String> locals = new ArrayList<String>();
		final List<String> globals = new ArrayList<String>();

		for (final CyNetwork network : allNetworks) {
			locals.add(network.getDefaultNodeTable().getTitle());
			name2tableMap.put(network.getDefaultNodeTable().getTitle(), network.getDefaultNodeTable());
			
			locals.add(network.getDefaultEdgeTable().getTitle());
			name2tableMap.put(network.getDefaultEdgeTable().getTitle(), network.getDefaultEdgeTable());
			
			locals.add(network.getDefaultNetworkTable().getTitle());
			name2tableMap.put(network.getDefaultNetworkTable().getTitle(), network.getDefaultNetworkTable());
		}
		
		final Set<CyTable> globalTableSet = tableManager.getGlobalTables();
		for (final CyTable table : globalTableSet){
			globals.add(table.getTitle());
			name2tableMap.put(table.getTitle(), table);
		}

		if (globals.size() == 0)
			throw new IllegalStateException("No Global Table in current session!");

		this.localTables = new ListSingleSelection<String>(locals);
		this.globalTables = new ListSingleSelection<String>(globals);

		this.localTables.setSelectedValue(locals.get(0));
		this.globalTables.setSelectedValue(globals.get(0));
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final CyTable globalTable = name2tableMap.get(globalTables.getSelectedValue());
		final CyTable localTable = name2tableMap.get(localTables.getSelectedValue());
		mapTable(localTable, globalTable);
	}

	private void mapTable(final CyTable localTable, final CyTable globalTable) {
		if (globalTable.getPrimaryKey().getType() != String.class)
			throw new IllegalStateException("Local table's primary key should be type String!");

		final CyColumn trgCol = localTable.getColumn(CyNetwork.NAME);
		if (trgCol != null)
			localTable.addVirtualColumns(globalTable, CyNetwork.NAME, false);
	}

}
