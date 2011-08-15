package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportDefaultVizmapTaskFactory implements TaskFactory {

	private final VizmapReaderManager vizmapReaderMgr;
	private final VisualMappingManager vmm;
	private final CyApplicationConfiguration config;

	public ImportDefaultVizmapTaskFactory(VizmapReaderManager vizmapReaderMgr, VisualMappingManager vmm, final CyApplicationConfiguration config) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmm = vmm;
		this.config = config;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ImportDefaultVizmapTask(vizmapReaderMgr, vmm, config));
	}
}
