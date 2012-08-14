package org.cytoscape.editor.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.AbstractCyEdit;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.util.List;

public class CutEdit extends AbstractCyEdit {
	private final CyNetworkView view;
	private final ClipboardManagerImpl mgr;
	private final ClipboardImpl clipboard;

	public CutEdit(ClipboardManagerImpl clipMgr, CyNetworkView view) { 
		super("Cut");
		this.clipboard = clipMgr.getCurrentClipboard();
		this.view = view;
		this.mgr = clipMgr;
	}

	public void redo() {
		mgr.cut(view, clipboard.getNodes(), clipboard.getEdges());
	}

	public void undo() {
		mgr.setCurrentClipboard(clipboard);
		mgr.paste(view, clipboard.getCenterX(), clipboard.getCenterY());
		view.updateView();
	}
}
