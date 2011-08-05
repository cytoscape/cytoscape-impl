package org.cytoscape.view.vizmap.gui.internal.task;

import javax.swing.JOptionPane;

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

		if (currentStyle.equals(manager.getDefaultStyle())) {
			JOptionPane.showMessageDialog(null,
					"You cannot delete default style.",
					"Cannot remove defalut style!", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// make sure the user really wants to do this
		final String styleName = currentStyle.getTitle();
		final String checkString = "Are you sure you want to permanently delete"
				+ " the visual style '" + styleName + "'?";
		int ich = JOptionPane.showConfirmDialog(null, checkString,
				"Confirm Delete Style", JOptionPane.YES_NO_OPTION);

		if (ich == JOptionPane.YES_OPTION)
			vmm.removeVisualStyle(currentStyle);
	}
}
