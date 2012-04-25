package org.cytoscape.task.internal.loaddatatable;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.task.loaddatatable.LoadTableFileTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;


public class LoadAttributesFileTaskFactoryImpl extends AbstractTaskFactory implements LoadTableFileTaskFactory{
	private CyTableReaderManager mgr;
	
	private final TunableSetter tunableSetter; 

	
	public LoadAttributesFileTaskFactoryImpl(CyTableReaderManager mgr, TunableSetter tunableSetter) {
		this.mgr = mgr;
		this.tunableSetter = tunableSetter;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new LoadAttributesFileTask(mgr));
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("file", file);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 
	}
}
