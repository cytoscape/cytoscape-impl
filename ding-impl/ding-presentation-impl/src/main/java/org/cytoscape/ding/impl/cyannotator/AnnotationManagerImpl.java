package org.cytoscape.ding.impl.cyannotator;

import java.util.List;

import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

/**
 * This class is essentially a wrapper around each network's CyAnnotator.
 */
public class AnnotationManagerImpl implements AnnotationManager {
	
	private final CyServiceRegistrar serviceRegistrar;

	public AnnotationManagerImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	/**********************************************************************************
	 *                   AnnotationManager implementation methods                     *
	 **********************************************************************************/
	@Override
	public void addAnnotation(final Annotation annotation) {
		if (annotation == null || !(annotation instanceof DingAnnotation))
			return;

		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(() -> {
					addAnnotation(annotation);
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}

		DingAnnotation dAnnotation = (DingAnnotation)annotation;
		CyNetworkView view = annotation.getNetworkView();
		CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		for (CyNetworkView networkView: viewManager.getNetworkViewSet()) {
			if (view.equals(networkView)) {
				((DGraphView)view).getCyAnnotator().addAnnotation(annotation);
				
				if (dAnnotation.getCanvas() != null)
					dAnnotation.getCanvas().add(dAnnotation.getComponent());
				else
					((DGraphView)view).getCyAnnotator().getForeGroundCanvas().add(dAnnotation.getComponent());
			}
		}
	}

	@Override
	public void removeAnnotation(final Annotation annotation) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(() -> {
					removeAnnotation(annotation);
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}
		
		CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		for (CyNetworkView view: viewManager.getNetworkViewSet())
			((DGraphView)view).getCyAnnotator().removeAnnotation(annotation);
	}

	@Override
	public List<Annotation> getAnnotations(final CyNetworkView networkView) {
		CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		for (CyNetworkView view: viewManager.getNetworkViewSet()) {
			if (view.equals(networkView))
				return ((DGraphView)view).getCyAnnotator().getAnnotations();
		}
		
		return null;
	}
}
