package org.cytoscape.view.vizmap.gui.internal.bypass;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;

final class NodeBypassContextMenuFactory implements CyNodeViewContextMenuFactory {

	private final VisualLexiconNode root;
	private final EditorManager editorManager;
	private final VisualMappingManager vmm;
	
	NodeBypassContextMenuFactory(final VisualLexiconNode root, final EditorManager editorManager,
			final VisualMappingManager vmm) {
		this.root = root;
		this.editorManager = editorManager;
		this.vmm = vmm;
	}

	@Override
	public CyMenuItem createMenuItem(final CyNetworkView netView, final View<CyNode> nodeView) {
		final BypassMenuBuilder menuBuilder = new BypassMenuBuilder(root, editorManager, vmm);
		return menuBuilder.build(netView, nodeView);
	}
}
