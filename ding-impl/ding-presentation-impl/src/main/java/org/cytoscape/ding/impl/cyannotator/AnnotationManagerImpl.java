package org.cytoscape.ding.impl.cyannotator;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
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

/**
 * This class is essentially a wrapper around each network's CyAnnotator.
 */
public class AnnotationManagerImpl implements AnnotationManager {
	
	private final CyServiceRegistrar serviceRegistrar;

	public AnnotationManagerImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void addAnnotation(Annotation annotation) {
		addAnnotations(List.of(annotation));
	}

	@Override
	public void addAnnotations(Collection<? extends Annotation> annotations) {
		var annotationsByView = groupByViewAndCanvasAndFlatten(annotations, false);
		
		if (annotationsByView.isEmpty())
			return;
		
		var dingRenderer = serviceRegistrar.getService(DingRenderer.class);
		
		// checkCycle throws IllegalAnnotationStructureException
		// we don't want this thrown from inside the invokeOnEDTAndWait call because it will get wrapped
		// note all the groups have to be on the same canvas for this to work
		annotationsByView.forEach((view, annotationsByCanvas) -> {
			annotationsByCanvas.forEach((canvas, canvasAnnotations) -> {
				var re = dingRenderer.getRenderingEngine(view);
				re.getCyAnnotator().checkCycle(canvasAnnotations);
			});
		});
		
		invokeOnEDTAndWait(() -> {
			annotationsByView.forEach((view, annotationsByCanvas) -> {
				var re = dingRenderer.getRenderingEngine(view);
				
				// We have to make sure the foreground annotations are added before the background annotations.
				// Because group annotations must be on the foreground canvas, if we add annotations to the background
				// before the groups are added it can lead to bad things happening.
				var all = new ArrayList<DingAnnotation>();
				
				if (annotationsByCanvas.containsKey(CanvasID.FOREGROUND)) {
					all.addAll(annotationsByCanvas.get(CanvasID.FOREGROUND));
				}
				if (annotationsByCanvas.containsKey(CanvasID.BACKGROUND)) {
					all.addAll(annotationsByCanvas.get(CanvasID.BACKGROUND));
				}
				
				re.getCyAnnotator().addAnnotations(all);
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
	public void removeAnnotation(Annotation annotation) {
		removeAnnotations(List.of(annotation));
	}

	@Override
	public void removeAnnotations(Collection<? extends Annotation> annotations) {
		if (annotations == null | annotations.isEmpty())
			return;
		
		// throws IllegalAnnotationStructureException
		var annotationsByView = groupByViewAndCanvasAndFlatten(annotations, true);
		
		if (annotationsByView.isEmpty())
			return;
		
		var dingRenderer = serviceRegistrar.getService(DingRenderer.class);
		
		annotationsByView.forEach((view, annotationsByCanvas) -> {
			var re = dingRenderer.getRenderingEngine(view);
			
			annotationsByCanvas.forEach((canvasId, dingAnnotations) -> {
				// The following code is a batch version of Annotation.removeAnnotation()
				for (var a : dingAnnotations) {
					var parent = a.getGroupParent();
					
					if (parent != null)
						parent.removeMember(a);
				}

				var arrows = getArrows(dingAnnotations);
				
				re.getCyAnnotator().removeAnnotations(arrows);
				re.getCyAnnotator().removeAnnotations(dingAnnotations);
			});
			
		});
		
		// TODO
//		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
//		annotationsByView.values().forEach(list -> {
//			list.forEach(a -> eventHelper.addEventPayload(a.getNetworkView(), a, AnnotationsRemovedEvent.class));
//		});
	}
	
	@Override
	public List<Annotation> getAnnotations(CyNetworkView networkView) {
		var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(networkView);
		
		if (re != null)
			return new ArrayList<>(re.getCyAnnotator().getAnnotations()); // just to shut up the type checker
		
		return Collections.emptyList();
	}
	
	@Override
	public List<Annotation> getSelectedAnnotations(CyNetworkView networkView) {
		var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(networkView);
		var cyAnnotator = re != null ? re.getCyAnnotator() : null;
		
		if (cyAnnotator != null)
			return new ArrayList<>(cyAnnotator.getAnnotationSelection().getSelectedAnnotations());
		
		return Collections.emptyList();	
	}
	
	private Map<CyNetworkView, Map<CanvasID, List<DingAnnotation>>> groupByViewAndCanvasAndFlatten(
			Collection<? extends Annotation> annotations,
			boolean incudeExisting
	) {
		var map = new HashMap<CyNetworkView, Map<CanvasID, List<DingAnnotation>>>();

		groupByView(annotations).forEach((view, as) -> {
			var flattened = flattenAnnotations(view, as, incudeExisting);
			map.put(view, groupByCanvas(view, flattened));
		});

		return map;
	}
	
	private Map<CyNetworkView,List<DingAnnotation>> groupByView(Collection<? extends Annotation> annotations) {
		return annotations.stream()
			.filter(a -> a instanceof DingAnnotation)
			.map(a -> (DingAnnotation) a)
			.collect(groupingBy(da -> da.getNetworkView()));
	}

	private Map<CanvasID, List<DingAnnotation>> groupByCanvas(CyNetworkView view, Collection<DingAnnotation> dingAnnotations) {
		return dingAnnotations.stream().collect(groupingBy(da -> getCanvas(view, da)));
	}

	private Set<DingAnnotation> flattenAnnotations(CyNetworkView view, List<DingAnnotation> annotaitons, boolean incudeExisting) {
		var collector = new HashSet<DingAnnotation>();
		
		for (var a : annotaitons)
			flattenAnnotations(view, a, collector, incudeExisting);
		
		return collector;
	}

	private void flattenAnnotations(CyNetworkView view, DingAnnotation a, Set<DingAnnotation> collector, boolean includeExisting) {
		var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);

		if (!includeExisting && re.getCyAnnotator().contains(a))
			return;
		if (!collector.add(a))
			return;
			
		collector.add(a);
		
		if (a instanceof GroupAnnotation) {
			for (var member : ((GroupAnnotation) a).getMembers())
				flattenAnnotations(view, (DingAnnotation) member, collector, includeExisting);
		}
	}

	private static CanvasID getCanvas(CyNetworkView view, DingAnnotation annotation) {
		return annotation.getCanvas() == null ? CanvasID.FOREGROUND : annotation.getCanvas();
	}
	
	private static List<DingAnnotation> getArrows(Collection<DingAnnotation> annotations) {
		return annotations.stream().flatMap(a -> a.getArrows().stream()).map(a -> (DingAnnotation)a).collect(toList());
	}
}
