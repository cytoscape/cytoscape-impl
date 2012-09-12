package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class CreateLegendTaskFactory extends AbstractTaskFactory {

	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;
	private final VisualMappingFunctionFactory continuousMappingFactory;

	public CreateLegendTaskFactory(final CyApplicationManager appManager, final VisualMappingManager vmm, VisualMappingFunctionFactory continuousMappingFactory) {
		this.appManager = appManager;
		this.vmm = vmm;
		this.continuousMappingFactory = continuousMappingFactory;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateLegendTask(appManager, vmm, continuousMappingFactory));
	}

}
