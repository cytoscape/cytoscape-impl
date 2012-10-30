package org.cytoscape.task.internal.export.vizmap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.task.write.ExportVizmapTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class ExportVizmapTaskFactoryImpl extends AbstractTaskFactory implements ExportVizmapTaskFactory{

	private final VizmapWriterManager writerManager;
	private final VisualMappingManager vmMgr;

	private final TunableSetter tunableSetter; 

	
	public ExportVizmapTaskFactoryImpl(VizmapWriterManager writerManager, VisualMappingManager vmMgr, TunableSetter tunableSetter) {
		this.writerManager = writerManager;
		this.vmMgr = vmMgr;
		this.tunableSetter = tunableSetter; 
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(2,new VizmapWriter(writerManager, vmMgr));
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("OutputFile", file);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 

	}
}