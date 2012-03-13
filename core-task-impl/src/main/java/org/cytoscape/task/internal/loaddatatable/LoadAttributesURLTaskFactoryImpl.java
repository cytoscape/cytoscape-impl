package org.cytoscape.task.internal.loaddatatable;


import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class LoadAttributesURLTaskFactoryImpl implements TaskFactory {
	
	private CyTableReaderManager mgr;
	
	public LoadAttributesURLTaskFactoryImpl(CyTableReaderManager mgr) {
		this.mgr = mgr;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new LoadAttributesURLTask(mgr));
	}
}
