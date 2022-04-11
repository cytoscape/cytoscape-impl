package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;
import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class PasteTask extends AbstractNetworkViewTask {

	private final Point2D xformPt;
	private final ClipboardManagerImpl clipMgr;
	private final CyServiceRegistrar serviceRegistrar;

	public PasteTask(CyNetworkView view, Point2D xformPt, ClipboardManagerImpl clipMgr, CyServiceRegistrar serviceRegistrar) {
		super(view);
		this.xformPt = xformPt;
		this.clipMgr = clipMgr;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Paste Task");
		
		double x = xformPt == null ? 0.0 : xformPt.getX();
		double y = xformPt == null ? 0.0 : xformPt.getY();
		
		Collection<Object> pastedObjects = clipMgr.paste(view, x, y);
		
		if (pastedObjects == null) {
			tm.showMessage(TaskMonitor.Level.WARN, "Nothing to paste");
			return;
		}

		var undoSupport = serviceRegistrar.getService(UndoSupport.class);
		undoSupport.postEdit(new PasteEdit(view, xformPt, clipMgr, pastedObjects, serviceRegistrar));
		
		// Apply visual style
		var vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		var vs = vmMgr.getVisualStyle(view);


		for (var element: pastedObjects) {
			if (element instanceof CyNode node) {
				var elementView = view.getNodeView(node);
				vs.apply(view.getModel().getRow(node), elementView);
			} else if (element instanceof CyEdge edge) {
				var elementView = view.getEdgeView(edge);
				vs.apply(view.getModel().getRow(edge), elementView);
			}
		}

		view.updateView();
		tm.setStatusMessage("Pasted " + pastedObjects.size() + " nodes and/or edges");
	}
}
