/**
 * The ClipboardManager provides a simple wrapper around a Clipboard.  This
 * allows us to manipulate the "current" clipboard.  In the future, we might
 * also want to provide for multiple clipboards....
 */
package org.cytoscape.editor.internal;

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
		return currentClipboard.paste(targetView, x, y);
	}

}
