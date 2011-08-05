package org.cytoscape.task.internal.loadvizmap;

import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class LoadVizmapFileTaskFactoryImpl implements TaskFactory {

	private final VizmapReaderManager vizmapReaderMgr;
	private final VisualMappingManager vmMgr;
	
	public LoadVizmapFileTaskFactoryImpl(VizmapReaderManager vizmapReaderMgr, VisualMappingManager vmMgr) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmMgr = vmMgr;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new LoadVizmapFileTask(vizmapReaderMgr, vmMgr));
	}

}
