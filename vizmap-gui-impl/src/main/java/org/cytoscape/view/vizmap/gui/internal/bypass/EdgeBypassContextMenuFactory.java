package org.cytoscape.view.vizmap.gui.internal.bypass;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;

public class EdgeBypassContextMenuFactory implements CyEdgeViewContextMenuFactory {

	private final VisualLexiconNode root;
	private final EditorManager editorManager;
	private final VisualMappingManager vmm;
	
	EdgeBypassContextMenuFactory(final VisualLexiconNode root, final EditorManager editorManager,
			final VisualMappingManager vmm) {
		this.root = root;
		this.editorManager = editorManager;
		this.vmm = vmm;
	}

	@Override
	public CyMenuItem createMenuItem(final CyNetworkView netView, final View<CyEdge> edgeView) {
		final BypassMenuBuilder menuBuilder = new BypassMenuBuilder(root, editorManager, vmm);
		return menuBuilder.build(netView, edgeView);
	}
}
