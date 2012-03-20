package org.cytoscape.view.vizmap.gui.internal.task;

import java.awt.Component;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class CreateLegendTaskFactory extends AbstractTaskFactory {

	private final SelectedVisualStyleManager manager;
	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;
	
	private final Component parent;

	public CreateLegendTaskFactory(final SelectedVisualStyleManager manager, final CyApplicationManager appManager,
			final VisualMappingManager vmm, final Component parent) {
		this.manager = manager;
		this.parent = parent;
		
		this.appManager = appManager;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateLegendTask(manager, appManager, vmm, parent));
	}

}
