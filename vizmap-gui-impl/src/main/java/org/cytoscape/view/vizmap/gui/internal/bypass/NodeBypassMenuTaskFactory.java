package org.cytoscape.view.vizmap.gui.internal.bypass;

import java.awt.Component;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.work.TaskIterator;

public class NodeBypassMenuTaskFactory extends AbstractNodeViewTaskFactory {

	private final VisualProperty<?> vp;
	private final ValueEditor<?> editor;

	private final Component parent;
	private final VisualMappingManager vmm;
	
	final boolean clearOnly;

	NodeBypassMenuTaskFactory(final Component parent, final VisualProperty<?> vp, final ValueEditor<?> editor,
			final VisualMappingManager vmm, final boolean clearOnly) {
		this.vp = vp;
		this.editor = editor;
		this.parent = parent;
		this.vmm = vmm;

		this.clearOnly = clearOnly;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		if(clearOnly) {
			return new TaskIterator(new ClearBypassTask(vp, nodeView, netView));
		} else
			return new TaskIterator(new BypassTask<CyNode>(parent, editor, vp, nodeView, netView, vmm));
	}
}
