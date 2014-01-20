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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
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
		List<CyIdentifiable> pastedObjects = null;
		if (this.xformPt == null)
			pastedObjects = clipboard.paste(view, 0.0, 0.0);
		else
			pastedObjects = clipboard.paste(view, xformPt.getX(), xformPt.getY());
		
		// Apply visual style
		VisualStyle vs = vmm.getVisualStyle(view);
		for (CyIdentifiable element: pastedObjects) {
			View<? extends CyIdentifiable> elementView = null;
			if (element instanceof CyNode)
				elementView = view.getNodeView((CyNode)element);
			else if (element instanceof CyEdge)
				elementView = view.getEdgeView((CyEdge)element);
			else
				continue;

			vs.apply(view.getModel().getRow(element), elementView);
		}

		view.updateView();
	}
}
