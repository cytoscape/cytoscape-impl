package org.cytoscape.view.vizmap.gui.internal.task;

import java.awt.Component;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class CopyVisualStyleTaskFactory extends AbstractTaskFactory {

	private final SelectedVisualStyleManager manager;
	private final VizMapperUtil util;
	private final Component parent;
	private final VisualMappingManager vmm;

	private final VisualStyleFactory factory;

	public CopyVisualStyleTaskFactory(final VisualMappingManager vmm,
			final VisualStyleFactory factory,
			final SelectedVisualStyleManager manager, final VizMapperUtil util,
			final Component parent) {
		this.manager = manager;
		this.vmm = vmm;
		this.util = util;
		this.factory = factory;
		this.parent = parent;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CopyVisualStyleTask(vmm, factory, manager,
				util, parent));
	}

}
