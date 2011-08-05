package org.cytoscape.view.vizmap.gui.internal.task;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class RenameVisualStyleTask extends AbstractTask {

	private final SelectedVisualStyleManager manager;
	private final VizMapperUtil util;
	private final Component parent;

	public RenameVisualStyleTask(final SelectedVisualStyleManager manager,
			final VisualMappingManager vmm, final VizMapperUtil util, final Component parent) {
		
		this.manager = manager;
		this.util = util;
		this.parent = parent;

	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		
		final VisualStyle currentStyle = manager.getCurrentVisualStyle();

		if (currentStyle.equals(manager.getDefaultStyle())) {
			JOptionPane.showMessageDialog(null,
					"You cannot rename the default style.",
					"Cannot rename defalut style!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		final String name = util.getStyleName(parent,
				currentStyle);

		// Ignore if user does not enter new name.
		if (name == null)
			return;
		
		currentStyle.setTitle(name);
		
		//TODO: fire event here.
	}

}
