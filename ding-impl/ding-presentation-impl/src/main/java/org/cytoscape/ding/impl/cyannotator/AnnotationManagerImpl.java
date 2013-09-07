package org.cytoscape.ding.impl.cyannotator;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

import org.cytoscape.ding.impl.DGraphView;

/**
 * This class is essentially a wrapper around each network's CyAnnotator.
 */
public class AnnotationManagerImpl implements AnnotationManager {
	private final CyNetworkViewManager viewManager;

	public AnnotationManagerImpl(CyNetworkViewManager viewManager) {
		this.viewManager = viewManager;
	}

	/**********************************************************************************
	 *                   AnnotationManager implementation methods                     *
	 **********************************************************************************/
	@Override
	public void addAnnotation(Annotation annotation) {
		CyNetworkView view = annotation.getNetworkView();
		for (CyNetworkView networkView: viewManager.getNetworkViewSet()) {
			if (view.equals(networkView))
				((DGraphView)view).getCyAnnotator().addAnnotation(annotation);
		}
	}

	@Override
	public void removeAnnotation(Annotation annotation) {
		for (CyNetworkView view: viewManager.getNetworkViewSet())
			((DGraphView)view).getCyAnnotator().removeAnnotation(annotation);
	}

	@Override
	public List<Annotation> getAnnotations(CyNetworkView networkView) {
		for (CyNetworkView view: viewManager.getNetworkViewSet()) {
			if (view.equals(networkView))
				return ((DGraphView)view).getCyAnnotator().getAnnotations();
		}
		return null;
	}
}
