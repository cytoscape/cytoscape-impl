package org.cytoscape.view.vizmap.gui.internal.task;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.task.ui.LegendDialog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CreateLegendTask extends AbstractTask {

	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;

	public CreateLegendTask(final CyApplicationManager appManager, final VisualMappingManager vmm) {
		this.appManager = appManager;
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		// Should be executed in EDT!
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final VisualStyle selectedStyle = vmm.getCurrentVisualStyle();
				final LegendDialog ld = new LegendDialog(selectedStyle, appManager, vmm);
				ld.showDialog(null);
			}
		});
	}
}
