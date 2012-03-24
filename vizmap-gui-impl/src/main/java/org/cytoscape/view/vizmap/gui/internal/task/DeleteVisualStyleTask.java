package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * 
 */
public class DeleteVisualStyleTask extends AbstractTask {

	private final SelectedVisualStyleManager manager;
	private final VisualMappingManager vmm;

	public DeleteVisualStyleTask(final VisualMappingManager vmm,
			final SelectedVisualStyleManager manager) {
		this.vmm = vmm;
		this.manager = manager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		final VisualStyle currentStyle = manager.getCurrentVisualStyle();

		if (currentStyle.equals(manager.getDefaultStyle()))
			throw new IllegalArgumentException("You cannot delete the default style.");

		vmm.removeVisualStyle(currentStyle);
	}
}
