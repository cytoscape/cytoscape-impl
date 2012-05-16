package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

public final class MapGlobalToLocalTableTaskFactoryImpl extends AbstractTableTaskFactory implements MapGlobalToLocalTableTaskFactory {
	
	private final CyTableManager tableManager;
	private final CyNetworkManager networkManager;
	
	private final TunableSetter tunableSetter; 

	
	public MapGlobalToLocalTableTaskFactoryImpl(final CyTableManager tableManager, final CyNetworkManager networkManager, TunableSetter tunableSetter) {
		this.tableManager = tableManager;
		this.networkManager = networkManager;
		this.tunableSetter = tunableSetter;
	}


	@Override
	public TaskIterator createTaskIterator(CyTable globalTable) {
		return new TaskIterator(new MapGlobalToLocalTableTask(globalTable, tableManager, networkManager) );
	}

	@Override
	public TaskIterator createTaskIterator(CyTable globalTable,
			Collection<CyTable> localTables) {
		final Map<String, Object> m = new HashMap<String, Object>();

		List<String> localTableTitles = new ArrayList<String>();
		for(CyTable local: localTables){
			localTableTitles.add(local.getTitle());
		}
		
		ListMultipleSelection<String> localTablesTunable = new ListMultipleSelection<String>(localTableTitles);
		localTablesTunable.setSelectedValues(localTableTitles);

		m.put("localTables", localTablesTunable);

		return tunableSetter.createTaskIterator(this.createTaskIterator(globalTable), m); 

	}
}
