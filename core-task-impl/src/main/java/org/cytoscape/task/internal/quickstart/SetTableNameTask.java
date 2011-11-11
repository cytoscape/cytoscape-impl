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
		e.setProgress(0.0);
		final CyTable[] tables = reader.getCyTables();
		
		if(tables == null || tables.length == 0)
			throw new IllegalStateException("Could not find table to be renamed.");
		e.setProgress(0.2);
		tables[0].setTitle("Global Table: " + newName);
		e.setProgress(0.6);
		state.setImportedTable(tables[0]);
		e.setProgress(1.0);
	} 
}
