package org.cytoscape.task.internal.table;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListSingleSelection;

public final class MapGlobalToLocalTableTaskFactoryImpl extends AbstractTaskFactory implements MapGlobalToLocalTableTaskFactory {
	
	private final CyTableManager tableManager;
	private final CyNetworkManager networkManager;
	
	private final TunableSetter tunableSetter; 

	
	public MapGlobalToLocalTableTaskFactoryImpl(final CyTableManager tableManager, final CyNetworkManager networkManager, TunableSetter tunableSetter) {
		this.tableManager = tableManager;
		this.networkManager = networkManager;
		this.tunableSetter = tunableSetter;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return  new TaskIterator(new MapGlobalToLocalTableTask(tableManager, networkManager));
	}

	@Override
	public TaskIterator createTaskIterator(String globalTable,
	                                       String localTable) {

		final Map<String, Object> m = new HashMap<String, Object>();

		ListSingleSelection<String> globalTables = new ListSingleSelection<String>(globalTable);
		globalTables.setSelectedValue(globalTable);

		ListSingleSelection<String> localTables = new ListSingleSelection<String>(localTable);
		localTables.setSelectedValue(localTable);

		m.put("globalTables", globalTables);
		m.put("localTables", localTables);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 
	}

}
