package org.cytoscape.task.internal.loaddatatable;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 class AddImportedTableTask extends AbstractTask {

	private static Logger logger = LoggerFactory.getLogger(AddImportedTableTask.class);
	
	private final CyTableManager tableMgr;
	private final CyTableReader reader;
	
	AddImportedTableTask(	final CyTableManager tableMgr, final CyTableReader reader){
		this.tableMgr = tableMgr;
		this.reader = reader;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if( this.reader != null && this.reader.getTables() != null)
			for (CyTable table : reader.getTables())
				tableMgr.addTable(table);
		else{
			if (reader == null)
				logger.warn("reader is null!" );
			else
				logger.warn("No tables in reader!");
		}
	}

}
