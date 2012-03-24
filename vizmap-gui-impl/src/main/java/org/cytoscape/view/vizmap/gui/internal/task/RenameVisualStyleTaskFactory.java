package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class RenameVisualStyleTaskFactory extends AbstractTaskFactory {

	private final SelectedVisualStyleManager manager;

	public RenameVisualStyleTaskFactory(final SelectedVisualStyleManager manager) {
		this.manager = manager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RenameVisualStyleTask(manager));
	}
}
