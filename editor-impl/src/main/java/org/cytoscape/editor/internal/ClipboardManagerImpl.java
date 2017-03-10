package org.cytoscape.editor.internal;

import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * The ClipboardManager provides a simple wrapper around a Clipboard.  This
 * allows us to manipulate the "current" clipboard.  In the future, we might
 * also want to provide for multiple clipboards....
 */
public final class ClipboardManagerImpl {
	
	private ClipboardImpl currentClipboard;
	private final CyServiceRegistrar serviceRegistrar;

	public ClipboardManagerImpl(final CyServiceRegistrar serviceRegistrar) { 
		this.serviceRegistrar = serviceRegistrar;
	}

	public boolean clipboardHasData() {
		if (currentClipboard == null)
			return false;
		return currentClipboard.clipboardHasData();
	}

	public ClipboardImpl getCurrentClipboard() {
		return currentClipboard;
	}

	public void setCurrentClipboard(ClipboardImpl clip) {
		this.currentClipboard = clip;
	}

	public void copy(CyNetworkView networkView, Set<CyNode> nodes, Set<CyEdge> edges) {
		copy(networkView, nodes, edges, false);
	}

	public void cut(CyNetworkView networkView, Set<CyNode> nodes, Set<CyEdge> edges) {
		copy(networkView, nodes, edges, true);
		networkView.getModel().removeEdges(edges);
		networkView.getModel().removeNodes(nodes);
		networkView.updateView();
	}

	public void copy(CyNetworkView networkView, Set<CyNode> nodes, Set<CyEdge> edges, boolean cut) {
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		final VisualLexicon lexicon = vmMgr.getAllVisualLexicon().iterator().next();
		currentClipboard = new ClipboardImpl(networkView, nodes, edges, cut, lexicon, serviceRegistrar);
	}

	public List<CyIdentifiable> paste(CyNetworkView targetView, double x, double y) {
		if (currentClipboard == null)
			return null;
		
		return currentClipboard.paste(targetView, x, y);
	}
}
