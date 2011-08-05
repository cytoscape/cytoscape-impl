package org.cytoscape.task.internal.quickstart;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SetTableNameTask extends AbstractTask {

	private final QuickStartState state;
	private final String newName;
	private final CyTableReader reader;
	
	public SetTableNameTask(final QuickStartState state, final CyTableReader reader, final String name) {
		super();
		this.newName = name;
		this.reader = reader;
		this.state = state;
	}

	public void run(TaskMonitor e) {
		final CyTable[] tables = reader.getCyTables();
		
		if(tables == null || tables.length == 0)
			throw new IllegalStateException("Could not find table to be renamed.");
		
		tables[0].setTitle("Global Table: " + newName);
		state.setImportedTable(tables[0]);
	} 
}
