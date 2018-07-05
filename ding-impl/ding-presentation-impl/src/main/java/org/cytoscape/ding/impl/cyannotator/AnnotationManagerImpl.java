package org.cytoscape.ding.impl.cyannotator;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.events.AnnotationsAddedEvent;
import org.cytoscape.view.presentation.events.AnnotationsRemovedEvent;

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
		if (!(annotation instanceof DingAnnotation))
			return;

		DingAnnotation dAnnotation = (DingAnnotation)annotation;
		CyNetworkView view = annotation.getNetworkView();

		invokeOnEDTAndWait(() -> {
			((DGraphView)view).getCyAnnotator().addAnnotation(annotation);
			if (dAnnotation.getCanvas() != null) {
				dAnnotation.getCanvas().add(dAnnotation.getComponent());
			} else {
				((DGraphView)view).getCyAnnotator().getForeGroundCanvas().add(dAnnotation.getComponent());
			}
		});
		
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.addEventPayload(view, annotation, AnnotationsAddedEvent.class);
	}
	 
	@Override
	public void addAnnotations(Collection<? extends Annotation> annotations) {
		Map<DGraphView,List<DingAnnotation>> annotationsByView = groupByView(annotations);
		if(annotationsByView.isEmpty())
			return;
		
		invokeOnEDTAndWait(() -> {
			annotationsByView.forEach((view, viewAnnotations) -> {
				Map<ArbitraryGraphicsCanvas,List<DingAnnotation>> annotationsByCanvas = groupByCanvas(view, viewAnnotations);
				view.getCyAnnotator().addAnnotations(viewAnnotations);
				annotationsByCanvas.forEach(ArbitraryGraphicsCanvas::addAnnotations);
			});
		});
		
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		annotationsByView.values().forEach(list -> {
			list.forEach(a -> {
				eventHelper.addEventPayload(a.getNetworkView(), a, AnnotationsAddedEvent.class);
			});
		});
	}
	
	@Override
	public void removeAnnotation(final Annotation annotation) {
		CyNetworkView view = annotation.getNetworkView();
		if (!(view instanceof DGraphView))
			return;
		
		invokeOnEDTAndWait(() -> {
			annotation.removeAnnotation();
			((DGraphView)view).getCyAnnotator().removeAnnotation(annotation);
		});
		
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.addEventPayload(view, annotation, AnnotationsRemovedEvent.class);
	}

	@Override
	public void removeAnnotations(Collection<? extends Annotation> annotations) {
		Map<DGraphView,List<DingAnnotation>> annotationsByView = groupByView(annotations);
		if(annotationsByView.isEmpty())
			return;
		
		invokeOnEDTAndWait(() -> {
			annotationsByView.forEach((view, viewAnnotations) -> {
				Map<ArbitraryGraphicsCanvas,List<DingAnnotation>> annotationsByCanvas = groupByCanvas(view, viewAnnotations);
				annotationsByCanvas.forEach((canvas, dingAnnotations) -> {

					// The following code is a batch version of Annotation.removeAnnotation()
					for(DingAnnotation a : dingAnnotations) {
						GroupAnnotation parent = a.getGroupParent();
						if(parent != null) {
							parent.removeMember(a);
						}
					}

					List<DingAnnotation> arrows = getArrows(dingAnnotations);
					canvas.removeAnnotations(arrows);
					canvas.removeAnnotations(dingAnnotations);
					canvas.repaint();
					
					view.getCyAnnotator().removeAnnotations(arrows);
					view.getCyAnnotator().removeAnnotations(dingAnnotations);
				});
			});
		});
		
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		annotationsByView.values().forEach(list -> {
			list.forEach(a -> eventHelper.addEventPayload(a.getNetworkView(), a, AnnotationsRemovedEvent.class));
		});
	}
	
	private static Map<DGraphView,List<DingAnnotation>> groupByView(Collection<? extends Annotation> annotations) {
		return annotations.stream()
			.filter(a -> a instanceof DingAnnotation)
			.map(a -> (DingAnnotation) a)
			.collect(groupingBy(da -> (DGraphView)da.getNetworkView()));
	}
	
	private static Map<ArbitraryGraphicsCanvas,List<DingAnnotation>> groupByCanvas(DGraphView view, List<DingAnnotation> dingAnnotations) {
		return dingAnnotations.stream().collect(groupingBy(da -> getCanvas(view, da)));
	}
	

	private static ArbitraryGraphicsCanvas getCanvas(DGraphView view, DingAnnotation annotation) {
		return annotation.getCanvas() == null ? view.getCyAnnotator().getForeGroundCanvas() : annotation.getCanvas();
	}
	
	private static List<DingAnnotation> getArrows(Collection<DingAnnotation> annotations) {
		return annotations.stream().flatMap(a -> a.getArrows().stream()).map(a -> (DingAnnotation)a).collect(toList());
	}
	
	@Override
	public List<Annotation> getAnnotations(final CyNetworkView networkView) {
		if (networkView instanceof DGraphView)
			return ((DGraphView)networkView).getCyAnnotator().getAnnotations();
		return null;
	}

}
