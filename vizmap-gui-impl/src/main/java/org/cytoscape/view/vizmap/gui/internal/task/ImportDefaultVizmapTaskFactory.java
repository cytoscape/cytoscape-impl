package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportDefaultVizmapTaskFactory implements TaskFactory {

	private final VizmapReaderManager vizmapReaderMgr;
	private final VisualMappingManager vmm;

	public ImportDefaultVizmapTaskFactory(VizmapReaderManager vizmapReaderMgr, VisualMappingManager vmm) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ImportDefaultVizmapTask(vizmapReaderMgr, vmm));
	}
}
