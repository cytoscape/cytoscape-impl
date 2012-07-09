package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class RenameVisualStyleTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Rename Visual Style";
	}

	@Tunable(description = "Enter new Visual Style name:")
	public String vsName;

	private final VisualMappingManager vmm;

	public RenameVisualStyleTask(final VisualMappingManager vmm) {
		this.vmm = vmm;
		this.vsName = vmm.getCurrentVisualStyle().getTitle();
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {

		final VisualStyle currentStyle = vmm.getCurrentVisualStyle();

		if (currentStyle.equals(this.vmm.getDefaultVisualStyle()))
			throw new IllegalArgumentException("You cannot rename the default style.");

		// Ignore if user does not enter new name.
		if (vsName == null)
			return;

		currentStyle.setTitle(vsName);
	}

}
