package org.cytoscape.task.internal.loaddatatable;


import java.util.Set;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


class FinalStatusMessageUpdateTask extends AbstractTask {
	private final CyTableReader reader;
	private CyTableManager tableMgr;
	private String name;
	
	FinalStatusMessageUpdateTask(final CyTableReader reader, CyTableManager tableMgr, String name) {
		this.reader = reader;
		this.tableMgr = tableMgr;
		this.name = name;
	}

	public void run(final TaskMonitor taskMonitor) throws Exception {
		
		if (reader.getTables().length == 1){
			// If this is a global table, update its title
			String tableTitle = "ThisIsAGlobalTable";
			CyTable globalTable = getTableByTitle(tableMgr, tableTitle);
			if (globalTable != null){
				globalTable.setTitle(name);
			}
		}
		
		for (CyTable table : reader.getTables())
			taskMonitor.setStatusMessage("Successfully loaded attribute table: " + table.getTitle());

		taskMonitor.setProgress(1.0);
	}
	

	private CyTable getTableByTitle(CyTableManager tableMgr, String tableTitle){
		CyTable retValue = null;

		Set<CyTable> tableSet = tableMgr.getAllTables(false);

		for (CyTable tbl : tableSet){
			if(tbl.getTitle().equalsIgnoreCase(tableTitle)){
				retValue = tbl;
				break;
			}
		}	

		return retValue;
	}
}
