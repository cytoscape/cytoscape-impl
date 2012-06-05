package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class CreateLegendTaskFactory extends AbstractTaskFactory {

	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;

	public CreateLegendTaskFactory(final CyApplicationManager appManager, final VisualMappingManager vmm) {
		this.appManager = appManager;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateLegendTask(appManager, vmm));
	}

}
