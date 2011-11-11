package org.cytoscape.task.internal.export.vizmap;

import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExportVizmapTaskFactory implements TaskFactory {

	private final VizmapWriterManager writerManager;
	private final VisualMappingManager vmMgr;

	public ExportVizmapTaskFactory(VizmapWriterManager writerManager, VisualMappingManager vmMgr) {
		this.writerManager = writerManager;
		this.vmMgr = vmMgr;
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(2,new VizmapWriter(writerManager, vmMgr));
	}
}