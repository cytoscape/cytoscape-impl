package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CopyVisualStyleTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Copy Visual Style";
	}

	@Tunable(description = "Name of copied Visual Style:")
	public String vsName;

	private final SelectedVisualStyleManager manager;
	private final VisualMappingManager vmm;

	private final VisualStyleFactory factory;

	public CopyVisualStyleTask(final VisualMappingManager vmm, final VisualStyleFactory factory,
			final SelectedVisualStyleManager manager) {

		this.manager = manager;
		this.factory = factory;
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		final VisualStyle originalStyle = manager.getCurrentVisualStyle();

		// Ignore if user does not enter new name.
		if (vsName == null)
			return;

		final VisualStyle copiedStyle = factory.createVisualStyle(originalStyle);
		copiedStyle.setTitle(vsName);
		vmm.addVisualStyle(copiedStyle);
	}
}
