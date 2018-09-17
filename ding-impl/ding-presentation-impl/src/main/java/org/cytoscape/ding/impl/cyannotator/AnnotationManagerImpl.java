package org.cytoscape.ding.impl.cyannotator;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DGraphView.Canvas;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

	@Override
	public void addAnnotation(final Annotation annotation) {
		addAnnotations(Arrays.asList(annotation));
	}
	
	
	@Override
	public void addAnnotations(Collection<? extends Annotation> annotations) {
		Map<DGraphView,Map<Canvas,List<DingAnnotation>>> annotationsByView = groupByViewAndCanvasAndFlatten(annotations, false);
		if (annotationsByView.isEmpty())
			return;
		
		// checkCycle throws IllegalAnnotationStructureException
		// we don't want this thrown from inside the invokeOnEDTAndWait call because it will get wrapped
		// note all the groups have to be on the same canvas for this to work
		annotationsByView.forEach((view, annotationsByCanvas) -> {
			annotationsByCanvas.forEach((canvas, canvasAnnotations) -> {
				view.getCyAnnotator().checkCycle(canvasAnnotations);
			});
		});
		
		invokeOnEDTAndWait(() -> {
			annotationsByView.forEach((view, annotationsByCanvas) -> {
				// We have to make sure the foreground annotations are added before the background annotations.
				// Because group annotations must be on the foreground canvas, if we add annotations to the background
				// before the groups are added it can lead to bad things happening.
				List<DingAnnotation> all = new ArrayList<>();
				
				if(annotationsByCanvas.containsKey(Canvas.FOREGROUND_CANVAS)) {
					List<DingAnnotation> foregroundAnnotations = annotationsByCanvas.get(Canvas.FOREGROUND_CANVAS);
					((ArbitraryGraphicsCanvas)view.getCanvas(Canvas.FOREGROUND_CANVAS)).addAnnotations(foregroundAnnotations);
					all.addAll(foregroundAnnotations);
				}
				if(annotationsByCanvas.containsKey(Canvas.BACKGROUND_CANVAS)) {
					List<DingAnnotation> backgroundAnnotations = annotationsByCanvas.get(Canvas.BACKGROUND_CANVAS);
					((ArbitraryGraphicsCanvas)view.getCanvas(Canvas.BACKGROUND_CANVAS)).addAnnotations(backgroundAnnotations);
					all.addAll(backgroundAnnotations);
				}
				
				view.getCyAnnotator().addAnnotations(all);
			});
		});
		
		// TODO
//		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
//		annotationsByView.values().forEach(list -> {
//			list.forEach(a -> {
//				eventHelper.addEventPayload(a.getNetworkView(), a, AnnotationsAddedEvent.class);
//			});
//		});
	}
	
	@Override
	public void removeAnnotation(final Annotation annotation) {
		removeAnnotations(Arrays.asList(annotation));
	}
	

	@Override
	public void removeAnnotations(Collection<? extends Annotation> annotations) {
		// throws IllegalAnnotationStructureException
		Map<DGraphView,Map<Canvas,List<DingAnnotation>>> annotationsByView = groupByViewAndCanvasAndFlatten(annotations, true);
		if (annotationsByView.isEmpty())
			return;
		
		invokeOnEDTAndWait(() -> {
			annotationsByView.forEach((view, annotationsByCanvas) -> {
				annotationsByCanvas.forEach((canvasId, dingAnnotations) -> {
					// The following code is a batch version of Annotation.removeAnnotation()
					for (DingAnnotation a : dingAnnotations) {
						GroupAnnotation parent = a.getGroupParent();
						if (parent != null)
							parent.removeMember(a);
					}

					List<DingAnnotation> arrows = getArrows(dingAnnotations);
					
					ArbitraryGraphicsCanvas canvas = (ArbitraryGraphicsCanvas)view.getCanvas(canvasId);
					canvas.removeAnnotations(arrows);
					canvas.removeAnnotations(dingAnnotations);
					canvas.repaint();

					view.getCyAnnotator().removeAnnotations(arrows);
					view.getCyAnnotator().removeAnnotations(dingAnnotations);
				});
			});
		});
		
		// TODO
//		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
//		annotationsByView.values().forEach(list -> {
//			list.forEach(a -> eventHelper.addEventPayload(a.getNetworkView(), a, AnnotationsRemovedEvent.class));
//		});
	}
	
	
	private static Map<DGraphView,Map<Canvas,List<DingAnnotation>>> groupByViewAndCanvasAndFlatten(Collection<? extends Annotation> annotations, boolean incudeExisting) {
		Map<DGraphView,Map<Canvas,List<DingAnnotation>>> map = new HashMap<>();
		
		groupByView(annotations).forEach((view, as) -> {
			Set<DingAnnotation> flattened = flattenAnnotations(view, as, incudeExisting);
			map.put(view, groupByCanvas(view, flattened));
		});
		
		return map;
	}
	
	
	private static Map<DGraphView,List<DingAnnotation>> groupByView(Collection<? extends Annotation> annotations) {
		return annotations.stream()
			.filter(a -> a instanceof DingAnnotation)
			.map(a -> (DingAnnotation) a)
			.collect(groupingBy(da -> (DGraphView)da.getNetworkView()));
	}
	
	private static Map<Canvas,List<DingAnnotation>> groupByCanvas(DGraphView view, Collection<DingAnnotation> dingAnnotations) {
		return dingAnnotations.stream().collect(groupingBy(da -> getCanvas(view, da)));
	}
	
	private static Set<DingAnnotation> flattenAnnotations(DGraphView view, List<DingAnnotation> annotaitons, boolean incudeExisting) {
		Set<DingAnnotation> collector = new HashSet<>();
		for(DingAnnotation a : annotaitons) {
			flattenAnnotations(view, a, collector, incudeExisting);
		}
		return collector;
	}
	
	private static void flattenAnnotations(DGraphView view, DingAnnotation a, Set<DingAnnotation> collector, boolean includeExisting) {
		if(!includeExisting && view.getCyAnnotator().contains(a))
			return;
		if(!collector.add(a))
			return;
			
		collector.add(a);
		if(a instanceof GroupAnnotation) {
			for(Annotation member : ((GroupAnnotation)a).getMembers()) {
				flattenAnnotations(view, (DingAnnotation)member, collector, includeExisting);
			}
		}
	}
	

	private static Canvas getCanvas(DGraphView view, DingAnnotation annotation) {
		return annotation.getCanvas() == null ? Canvas.FOREGROUND_CANVAS : annotation.getCanvas().getCanvasId();
	}
	
	private static List<DingAnnotation> getArrows(Collection<DingAnnotation> annotations) {
		return annotations.stream().flatMap(a -> a.getArrows().stream()).map(a -> (DingAnnotation)a).collect(toList());
	}
	
	@Override
	public List<Annotation> getAnnotations(final CyNetworkView networkView) {
		if (networkView instanceof DGraphView)
			return ((DGraphView) networkView).getCyAnnotator().getAnnotations();
		
		return Collections.emptyList();
	}
}
