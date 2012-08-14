package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;

public class PasteEdit extends AbstractCyEdit {
	private final CyNetworkView view;
	private final VisualMappingManager vmm;
	private final Point2D xformPt;
	private final ClipboardManagerImpl clipMgr;
	private final ClipboardImpl clipboard;
	private final List<CyIdentifiable> pastedObjects;

	public PasteEdit(VisualMappingManager vmm, CyNetworkView view, Point2D xformPt, 
	                 ClipboardManagerImpl clipMgr, List<CyIdentifiable> pastedObjects) {
		super("Paste");
		this.view = view;
		this.vmm = vmm;
		this.xformPt = xformPt;
		this.clipMgr = clipMgr;
		this.clipboard = clipMgr.getCurrentClipboard();
		this.pastedObjects = pastedObjects;
	}

	public void undo() {
		List<CyNode> nodeList = new ArrayList<CyNode>();
		List<CyEdge> edgeList = new ArrayList<CyEdge>();
		for (CyIdentifiable object: pastedObjects) {
			// Remove edges first
			if (object instanceof CyEdge)
				edgeList.add((CyEdge)object);
			else if (object instanceof CyNode)
				nodeList.add((CyNode)object);
		}

		view.getModel().removeEdges(edgeList);
		view.getModel().removeNodes(nodeList);
		view.updateView();
	}

	public void redo() {
		if (this.xformPt == null)
			clipboard.paste(view, 0.0, 0.0);
		else
			clipboard.paste(view, xformPt.getX(), xformPt.getY());
		
		// Apply visual style
		VisualStyle vs = vmm.getVisualStyle(view);
		vs.apply(view);
		view.updateView();
	}
}
