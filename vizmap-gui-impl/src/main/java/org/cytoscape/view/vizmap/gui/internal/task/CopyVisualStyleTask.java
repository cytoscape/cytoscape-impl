package org.cytoscape.view.vizmap.gui.internal.task;

import java.awt.Component;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CopyVisualStyleTask extends AbstractTask {

	private final SelectedVisualStyleManager manager;
	private final VizMapperUtil util;
	private final Component parent;
	private final VisualMappingManager vmm;

	private final VisualStyleFactory factory;

	public CopyVisualStyleTask(final VisualMappingManager vmm,
			final VisualStyleFactory factory,
			final SelectedVisualStyleManager manager, final VizMapperUtil util,
			final Component parent) {

		this.manager = manager;
		this.util = util;
		this.parent = parent;
		this.factory = factory;
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {

		final VisualStyle originalStyle = manager.getCurrentVisualStyle();
		final String name = util.getStyleName(parent, originalStyle);

		// Ignore if user does not enter new name.
		if (name == null)
			return;

		final VisualStyle copiedStyle = factory.getInstance(originalStyle);
		vmm.addVisualStyle(copiedStyle);
	}

}
