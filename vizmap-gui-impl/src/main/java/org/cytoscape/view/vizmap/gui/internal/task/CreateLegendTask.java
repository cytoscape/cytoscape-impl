package org.cytoscape.view.vizmap.gui.internal.task;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
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
	private final CySwingApplication desktop;

	public CreateLegendTask(final CySwingApplication desktop, final SelectedVisualStyleManager manager, final CyApplicationManager appManager,
			final VisualMappingManager vmm) {
		this.manager = manager;
		this.appManager = appManager;
		this.vmm = vmm;
		this.desktop = desktop;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		// Should be executed in EDT!
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final VisualStyle selectedStyle = manager.getCurrentVisualStyle();
				final LegendDialog ld = new LegendDialog(selectedStyle, appManager, vmm);
				ld.showDialog(desktop.getJFrame());
			}
		});
	}
}
