package org.cytoscape.view.vizmap.gui.internal.bypass;

import java.awt.Component;

import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.work.TaskIterator;

public class EdgeBypassMenuTaskFactory extends AbstractEdgeViewTaskFactory {

	private final VisualProperty<?> vp;
	private final ValueEditor<?> editor;

	private final Component parent;
	private final VisualMappingManager vmm;
	private final boolean clearOnly;

	public EdgeBypassMenuTaskFactory(final Component parent, final VisualProperty<?> vp, final ValueEditor<?> editor,
			final VisualMappingManager vmm, final boolean clearOnly) {
		this.vp = vp;
		this.editor = editor;
		this.parent = parent;
		this.vmm = vmm;
		this.clearOnly = clearOnly;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyEdge> edgeView, CyNetworkView netView) {
		if(clearOnly)
			return new TaskIterator(new ClearBypassTask(vp, edgeView, netView));
		else
			return new TaskIterator(new BypassTask<CyEdge>(parent, editor, vp, edgeView, netView, vmm));
	}
}
