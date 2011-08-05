package org.cytoscape.task.internal.loaddatatable;


import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskFactory;


public class LoadAttributesURLTaskFactoryImpl implements TaskFactory {
	
	private CyTableReaderManager mgr;
	private CyTableManager tableMgr;
	
	public LoadAttributesURLTaskFactoryImpl(CyTableReaderManager mgr, CyTableManager tableMgr) {
		this.mgr = mgr;
		this.tableMgr = tableMgr;
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new LoadAttributesURLTask(mgr, tableMgr));
	}
}
