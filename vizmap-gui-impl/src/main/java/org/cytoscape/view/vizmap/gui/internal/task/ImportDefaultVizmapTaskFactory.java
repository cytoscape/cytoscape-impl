package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportDefaultVizmapTaskFactory extends AbstractTaskFactory {

	private final VizmapReaderManager vizmapReaderMgr;
	private final VisualMappingManager vmm;
	private final CyApplicationConfiguration config;

	public ImportDefaultVizmapTaskFactory(final VizmapReaderManager vizmapReaderMgr,
										  final VisualMappingManager vmm,
										  final CyApplicationConfiguration config) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmm = vmm;
		this.config = config;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ImportDefaultVizmapTask(vizmapReaderMgr, vmm, config));
	}
}
