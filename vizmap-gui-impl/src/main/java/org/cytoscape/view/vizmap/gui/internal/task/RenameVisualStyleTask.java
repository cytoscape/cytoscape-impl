package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
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

	private final SelectedVisualStyleManager manager;

	public RenameVisualStyleTask(final SelectedVisualStyleManager manager) {
		this.manager = manager;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {

		final VisualStyle currentStyle = manager.getCurrentVisualStyle();

		if (currentStyle.equals(manager.getDefaultStyle()))
			throw new IllegalArgumentException("You cannot rename the default style.");

		// Ignore if user does not enter new name.
		if (vsName == null)
			return;

		currentStyle.setTitle(vsName);
	}

}
