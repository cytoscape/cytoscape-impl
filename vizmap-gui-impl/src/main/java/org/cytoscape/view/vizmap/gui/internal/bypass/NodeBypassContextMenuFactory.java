package org.cytoscape.view.vizmap.gui.internal.bypass;

import java.util.Collection;

import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;

final class NodeBypassContextMenuFactory implements CyNodeViewContextMenuFactory {

	private final VisualLexiconNode root;
	private final EditorManager editorManager;
	private final VisualMappingManager vmm;
	private Collection<VisualProperty<?>> vpSet;
	
	NodeBypassContextMenuFactory(final VisualLexiconNode root, final EditorManager editorManager,
			final VisualMappingManager vmm, final VisualLexicon lexicon) {
		this.root = root;
		this.editorManager = editorManager;
		this.vmm = vmm;
		this.vpSet = lexicon.getAllDescendants(BasicVisualLexicon.NODE);
	}

	@Override
	public CyMenuItem createMenuItem(final CyNetworkView netView, final View<CyNode> nodeView) {
		final BypassMenuBuilder menuBuilder = new BypassMenuBuilder(root, editorManager, vmm, vpSet);
		return menuBuilder.build(netView, nodeView);
	}
}
