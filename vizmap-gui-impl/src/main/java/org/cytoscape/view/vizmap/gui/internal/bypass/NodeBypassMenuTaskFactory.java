package org.cytoscape.view.vizmap.gui.internal.bypass;

import java.awt.Component;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.work.TaskIterator;

public class NodeBypassMenuTaskFactory extends AbstractNodeViewTaskFactory {

	private final VisualProperty<?> vp;
	private final ValueEditor<?> editor;

	private final Component parent;

	private final SelectedVisualStyleManager selectedManager;

	NodeBypassMenuTaskFactory(final Component parent, final VisualProperty<?> vp, final ValueEditor<?> editor,
			final SelectedVisualStyleManager selectedManager) {
		this.vp = vp;
		this.editor = editor;
		this.parent = parent;
		this.selectedManager = selectedManager;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		return new TaskIterator(new BypassTask<CyNode>(parent, editor, vp, nodeView, netView, selectedManager));
	}
}
