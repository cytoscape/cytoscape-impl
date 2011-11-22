package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportDefaultVizmapTaskFactory implements TaskFactory {

	private final VizmapReaderManager vizmapReaderMgr;
	private final VisualMappingManager vmm;
	private final CyApplicationConfiguration config;
	
	private final CyEventHelper eventHelper;

	public ImportDefaultVizmapTaskFactory(VizmapReaderManager vizmapReaderMgr, VisualMappingManager vmm,
			final CyApplicationConfiguration config, final CyEventHelper eventHelper) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmm = vmm;
		this.config = config;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ImportDefaultVizmapTask(vizmapReaderMgr, vmm, config, eventHelper));
	}
}
