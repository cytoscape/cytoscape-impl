package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * 
 */
public class DeleteVisualStyleTask extends AbstractTask {

	private final VisualMappingManager vmm;

	public DeleteVisualStyleTask(final VisualMappingManager vmm) {
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		final VisualStyle currentStyle = vmm.getCurrentVisualStyle();

		if (currentStyle.equals(this.vmm.getDefaultVisualStyle()))
			throw new IllegalArgumentException("You cannot delete the default style.");

		vmm.removeVisualStyle(currentStyle);
	}
}
