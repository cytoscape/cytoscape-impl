package org.cytoscape.view.vizmap.gui.internal.bypass;

import java.awt.Component;

import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.work.TaskIterator;

public class EdgeBypassMenuTaskFactory extends AbstractEdgeViewTaskFactory {

	private final VisualProperty<?> vp;
	private final ValueEditor<?> editor;

	private final Component parent;

	private final SelectedVisualStyleManager selectedManager;

	public EdgeBypassMenuTaskFactory(final Component parent,
			final VisualProperty<?> vp, final ValueEditor<?> editor,
			final SelectedVisualStyleManager selectedManager) {
		this.vp = vp;
		this.editor = editor;
		this.parent = parent;
		this.selectedManager = selectedManager;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyEdge> edgeView, CyNetworkView netView) {
		return new TaskIterator(new BypassTask<CyEdge>(parent, editor, vp, edgeView, netView, selectedManager));
	}
}
