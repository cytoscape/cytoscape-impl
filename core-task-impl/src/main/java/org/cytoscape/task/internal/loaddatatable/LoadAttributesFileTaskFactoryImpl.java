package org.cytoscape.task.internal.loaddatatable;


import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskFactory;


public class LoadAttributesFileTaskFactoryImpl implements TaskFactory {
	private CyTableReaderManager mgr;
	private CyTableManager tableMgr;
	public LoadAttributesFileTaskFactoryImpl(CyTableReaderManager mgr, CyTableManager tableMgr) {
		this.mgr = mgr;
		this.tableMgr = tableMgr;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new LoadAttributesFileTask(mgr, tableMgr));
	}
}
