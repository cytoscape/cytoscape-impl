package org.cytoscape.editor.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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
 * The ClipboardManager provides a simple wrapper around a Clipboard.
 * This allows us to manipulate the "current" clipboard.
 * In the future, we might also want to provide for multiple clipboards....
 */
public final class ClipboardManagerImpl {
	
	private ClipboardImpl currentClipboard;
	private List<AnnotationFactory<? extends Annotation>> annotationFactories = new ArrayList<>();
	
	private final CyServiceRegistrar serviceRegistrar;

	public ClipboardManagerImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	public boolean clipboardHasData() {
		return currentClipboard == null ? false : currentClipboard.clipboardHasData();
	}

	public ClipboardImpl getCurrentClipboard() {
		return currentClipboard;
	}

	public void setCurrentClipboard(ClipboardImpl clip) {
		this.currentClipboard = clip;
	}

	public void copy(
			CyNetworkView networkView,
			Collection<CyNode> nodes,
			Collection<CyEdge> edges,
			Collection<Annotation> annotations
	) {
		copy(networkView, nodes, edges, annotations, false);
	}

	public void copy(
			CyNetworkView networkView,
			Collection<CyNode> nodes,
			Collection<CyEdge> edges,
			Collection<Annotation> annotations,
			boolean cut
	) {
		var vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		var lexicon = vmMgr.getAllVisualLexicon().iterator().next();
		currentClipboard = new ClipboardImpl(networkView, nodes, edges, annotations, cut, lexicon, annotationFactories,
				serviceRegistrar);
	}

	public void cut(
			CyNetworkView networkView,
			Collection<CyNode> nodes,
			Collection<CyEdge> edges,
			Collection<Annotation> annotations
	) {
		copy(networkView, nodes, edges, annotations, true);
		networkView.getModel().removeEdges(edges);
		networkView.getModel().removeNodes(nodes);
		serviceRegistrar.getService(AnnotationManager.class).removeAnnotations(annotations);
		
		networkView.updateView();
	}

	public Collection<CyIdentifiable> paste(CyNetworkView targetView, double x, double y) {
		if (currentClipboard == null)
			return null;

		return currentClipboard.paste(targetView, x, y);
	}
	
	public void addAnnotationFactory(AnnotationFactory<?> factory, Map<?, ?> props) {
		if (factory != null)
			annotationFactories.add(factory);
	}

	public void removeAnnotationFactory(AnnotationFactory<?> factory, Map<?, ?> props) {
		if (factory != null)
			annotationFactories.remove(factory);
	}
}
