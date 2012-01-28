package org.cytoscape.view.vizmap.gui.internal.task;

import java.awt.Component;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.internal.task.ui.LegendDialog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CreateLegendTask extends AbstractTask {

	private final SelectedVisualStyleManager manager;
	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;

	private final Component parent;

	public CreateLegendTask(final SelectedVisualStyleManager manager, final CyApplicationManager appManager,
			final VisualMappingManager vmm, final Component parent) {
		this.manager = manager;
		this.parent = parent;
		this.appManager = appManager;
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		final VisualStyle selectedStyle = manager.getCurrentVisualStyle();
		final LegendDialog ld = new LegendDialog(selectedStyle, appManager, vmm);
		ld.showDialog(parent);
	}

}
