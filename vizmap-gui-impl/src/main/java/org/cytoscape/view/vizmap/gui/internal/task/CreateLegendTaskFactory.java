package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class CreateLegendTaskFactory extends AbstractTaskFactory {

	private final SelectedVisualStyleManager manager;
	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;

	private final CySwingApplication desktop;

	public CreateLegendTaskFactory(final CySwingApplication desktop, final SelectedVisualStyleManager manager,
			final CyApplicationManager appManager, final VisualMappingManager vmm) {
		this.manager = manager;
		this.appManager = appManager;
		this.vmm = vmm;
		this.desktop = desktop;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateLegendTask(desktop, manager, appManager, vmm));
	}

}
