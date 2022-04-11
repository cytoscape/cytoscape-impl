package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.undo.AbstractCyEdit;

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

public class PasteEdit extends AbstractCyEdit {
	
	private final CyNetworkView view;
	private final Point2D xformPt;
	private final ClipboardImpl clipboard;
	private final CyServiceRegistrar registrar;
	
	private Collection<Object> pastedObjects;

	
	public PasteEdit(
			CyNetworkView view,
			Point2D xformPt,
			ClipboardManagerImpl clipMgr,
			Collection<Object> pastedObjects,
			CyServiceRegistrar registrar
	) {
		super("Paste");
		this.view = view;
		this.xformPt = xformPt;
		this.clipboard = clipMgr.getCurrentClipboard();
		this.pastedObjects = pastedObjects;
		this.registrar = registrar;
	}

	@Override
	public void undo() {
		List<CyNode> nodeList = new ArrayList<>();
		List<CyEdge> edgeList = new ArrayList<>();
		List<Annotation> annotations = new ArrayList<>();
		
		for (var object: pastedObjects) {
			if (object instanceof CyEdge edge)
				edgeList.add(edge);
			else if (object instanceof CyNode node)
				nodeList.add(node);
			else if (object instanceof Annotation ann)
				annotations.add(ann);
		}

		// Remove edges first
		view.getModel().removeEdges(edgeList);
		view.getModel().removeNodes(nodeList);
		
		var annotatioManager = registrar.getService(AnnotationManager.class);
		annotatioManager.removeAnnotations(annotations);
		
		view.updateView();
	}

	@Override
	public void redo() {
		double x = xformPt == null ? 0.0 : xformPt.getX();
		double y = xformPt == null ? 0.0 : xformPt.getY();
		
		// This returns a NEW set of pasted objects, needs to replace the old set.
		this.pastedObjects = clipboard.paste(view, x, y);
		
		// Apply visual style
		var vmMgr = registrar.getService(VisualMappingManager.class);
		var annotationManager = registrar.getService(AnnotationManager.class);
		
		VisualStyle vs = vmMgr.getVisualStyle(view);
		
		for (var element: pastedObjects) {
			if (element instanceof CyNode node) {
				var elementView = view.getNodeView(node);
				vs.apply(view.getModel().getRow(node), elementView);
			} else if (element instanceof CyEdge edge) {
				var elementView = view.getEdgeView(edge);
				vs.apply(view.getModel().getRow(edge), elementView);
			} else if (element instanceof Annotation ann) {
				annotationManager.addAnnotation(ann);
			}
		}

		view.updateView();
	}
}
