/**
 * The ClipboardManager provides a simple wrapper around a Clipboard.  This
 * allows us to manipulate the "current" clipboard.  In the future, we might
 * also want to provide for multiple clipboards....
 */
package org.cytoscape.editor.internal;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

public final class ClipboardManagerImpl {
	private ClipboardImpl currentClipboard;

	public boolean clipboardHasData() {
		if (currentClipboard == null)
			return false;
		return currentClipboard.clipboardHasData();
	}

	public ClipboardImpl getCurrentClipboard() { return currentClipboard; }
	public void setCurrentClipboard(ClipboardImpl clip) {
		this.currentClipboard = clip;
	}

	public void copy(CyNetworkView networkView, List<CyNode> nodes, List<CyEdge> edges) {
		currentClipboard = new ClipboardImpl(networkView, nodes, edges);
	}

	public void cut(CyNetworkView networkView, List<CyNode> nodes, List<CyEdge> edges) {
		copy(networkView, nodes, edges);
		networkView.getModel().removeEdges(edges);
		networkView.getModel().removeNodes(nodes);
	}

	public List<CyIdentifiable> paste(CyNetworkView targetView, double x, double y) {
		if (currentClipboard == null) return null;
		return currentClipboard.paste(targetView, x, y);
	}

}
