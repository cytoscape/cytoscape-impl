package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

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

public class PasteTask extends AbstractNetworkViewTask {

	private final Point2D xformPt;
	private final ClipboardManagerImpl clipMgr;
	private final CyServiceRegistrar serviceRegistrar;

	public PasteTask(final CyNetworkView view, final Point2D xformPt, final ClipboardManagerImpl clipMgr,
			final CyServiceRegistrar serviceRegistrar) {
		super(view);
		this.xformPt = xformPt;
		this.clipMgr = clipMgr;
		this.serviceRegistrar = serviceRegistrar;
	}

	// TODO: add an isRead that is ready when we have something to paste

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Paste Task");
		final List<CyIdentifiable> pastedObjects;
		
		if (xformPt == null)
			pastedObjects = clipMgr.paste(view, 0.0, 0.0);
		else
			pastedObjects = clipMgr.paste(view, xformPt.getX(), xformPt.getY());

		if (pastedObjects == null) {
			tm.showMessage(TaskMonitor.Level.WARN, "Nothing to past");
			return;
		}

		final UndoSupport undoSupport = serviceRegistrar.getService(UndoSupport.class);
		undoSupport.postEdit(new PasteEdit(view, xformPt, clipMgr, pastedObjects, serviceRegistrar));
		
		// Apply visual style
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		VisualStyle vs = vmMgr.getVisualStyle(view);
		
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
		tm.setStatusMessage("Pasted "+pastedObjects.size()+" nodes and/or edges");
	}
}
