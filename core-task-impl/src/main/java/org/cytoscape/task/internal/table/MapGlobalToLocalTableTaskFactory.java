package org.cytoscape.task.internal.table;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public final class MapGlobalToLocalTableTaskFactory implements TaskFactory {
	
	private final CyTableManager tableManager;
	private final CyNetworkManager networkManager;
	
	public MapGlobalToLocalTableTaskFactory(final CyTableManager tableManager, final CyNetworkManager networkManager) {
		this.tableManager = tableManager;
		this.networkManager = networkManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return  new TaskIterator(new MapGlobalToLocalTableTask(tableManager, networkManager));
	}

}
