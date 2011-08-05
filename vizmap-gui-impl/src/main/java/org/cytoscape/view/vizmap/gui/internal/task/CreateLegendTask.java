package org.cytoscape.view.vizmap.gui.internal.task;

import java.awt.Component;

import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.internal.task.ui.LegendDialog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CreateLegendTask extends AbstractTask {

	private final SelectedVisualStyleManager manager;
	private final Component parent;


	public CreateLegendTask(final SelectedVisualStyleManager manager, final Component parent) {
		this.manager = manager;
		this.parent = parent;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {

		final LegendDialog ld = new LegendDialog(manager.getCurrentVisualStyle());
		ld.showDialog(parent);
	}

}
