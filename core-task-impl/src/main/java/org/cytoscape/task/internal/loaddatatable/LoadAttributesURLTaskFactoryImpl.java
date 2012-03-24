package org.cytoscape.task.internal.loaddatatable;


import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.task.loaddatatable.AttributesURLLoader;
import org.cytoscape.task.loaddatatable.AttributesFileLoader;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;


public class LoadAttributesURLTaskFactoryImpl extends AbstractTaskFactory implements AttributesURLLoader {
	
	private CyTableReaderManager mgr;
	
	private final TunableSetter tunableSetter; 
	
	public LoadAttributesURLTaskFactoryImpl(CyTableReaderManager mgr, TunableSetter tunableSetter) {
		this.mgr = mgr;
		this.tunableSetter = tunableSetter;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new LoadAttributesURLTask(mgr));
	}

	@Override
	public TaskIterator createTaskIterator(URL url) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("url", url);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 
	}
}
