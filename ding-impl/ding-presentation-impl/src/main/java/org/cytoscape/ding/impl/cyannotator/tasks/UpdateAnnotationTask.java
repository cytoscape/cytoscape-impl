package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

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

public class UpdateAnnotationTask extends AbstractTask implements ObservableTask {

	@Tunable(context = "nogui", required = true, description = "The UUID or name of the annotation to be updated")
	public String uuidOrName;

	// Standard annotation values
	@ContainsTunables
	public StandardAnnotationTunables standardTunables;

	// Text annotation values
	@ContainsTunables
	public TextAnnotationTunables textTunables;

	// Shape annotation values
	@ContainsTunables
	public ShapeAnnotationTunables shapeTunables;

	// Image annotation values
	@ContainsTunables
	public ImageAnnotationTunables imageTunables;
	
	private Annotation updatedAnnotation;
	
	private final Class<? extends Annotation> type;
	private final CyServiceRegistrar serviceRegistrar;
	
	public UpdateAnnotationTask(Class<? extends Annotation> type, CyServiceRegistrar serviceRegistrar) {
		this.type = type;
		this.serviceRegistrar = serviceRegistrar;

		if (type.equals(ImageAnnotation.class)) {
			standardTunables = new StandardAnnotationTunables();
			shapeTunables = new ShapeAnnotationTunables();
			imageTunables = new ImageAnnotationTunables();
		} else if (type.equals(BoundedTextAnnotation.class)) {
			standardTunables = new StandardAnnotationTunables();
			textTunables = new TextAnnotationTunables();
			shapeTunables = new ShapeAnnotationTunables();
		} else if (type.equals(TextAnnotation.class)) {
			standardTunables = new StandardAnnotationTunables();
			textTunables = new TextAnnotationTunables();
		} else if (type.equals(ShapeAnnotation.class)) {
			standardTunables = new StandardAnnotationTunables();
			shapeTunables = new ShapeAnnotationTunables();
		} else if (type.equals(ArrowAnnotation.class)) {
			// Ignore...
		} else if (type.equals(GroupAnnotation.class)) {
			standardTunables = new StandardAnnotationTunables();
		}
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Update Annotation");

		// Get the UUID
		UUID aUUID = null;
		String name = null;
		
		try {
			aUUID = UUID.fromString(uuidOrName);
		} catch (IllegalArgumentException e) {
			name = uuidOrName;
		}
		
		var viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		var annotationManager = serviceRegistrar.getService(AnnotationManager.class);
		
		Annotation annotation = null;
		CyNetworkView view = null;
		
		if (aUUID != null) {
			// Iterate over the set of all annotations, looking for the one with the same UUID
			SEARCH:
			for (var nv : viewManager.getNetworkViewSet()) {
				for (var a : annotationManager.getAnnotations(nv)) {
					if (a.getUUID().equals(aUUID)) {
						annotation = a;
						view = nv;
						break SEARCH;
					}
				}
			}
		} else if (name != null) {
			// The UUID was not informed, so try to find the annotation by its name (look into the current view first)
			var currView = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			// If there's no current view, we have to search in all views until one annotation is found
			var viewSet = currView != null ? Collections.singleton(currView) : viewManager.getNetworkViewSet();
			
			SEARCH:
			for (var nv : viewSet) {
				for (var a : annotationManager.getAnnotations(nv)) {
					if (name.equals(a.getName())) {
						annotation = a;
						view = nv;
						break SEARCH;
					}
				}
			}
		}
		
		if (annotation != null) {
			try {
				updateAnnotation(tm, annotation, view);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			updatedAnnotation = annotation;
		} else {
			tm.setStatusMessage("Can't find any annotation with UUID or name '" + uuidOrName + "'");
		}
	}

	private void updateAnnotation(TaskMonitor tm, Annotation annotation, CyNetworkView view) {
		if (annotation instanceof ImageAnnotation) {
			standardTunables.update(tm, annotation);
			shapeTunables.update(tm, annotation);
			imageTunables.update(tm, annotation);
		} else if (annotation instanceof BoundedTextAnnotation) {
			standardTunables.update(tm, annotation);
			shapeTunables.update(tm, annotation);
			textTunables.update(tm, annotation);
		} else if (annotation instanceof TextAnnotation) {
			standardTunables.update(tm, annotation);
			textTunables.update(tm, annotation);
		} else if (annotation instanceof ShapeAnnotation) {
			standardTunables.update(tm, annotation);
			shapeTunables.update(tm, annotation);
		} else if (annotation instanceof GroupAnnotation) {
			standardTunables.update(tm, annotation);
		}
		
		if (standardTunables.canvas != null && view != null) {
			// need to rebuild the tree AFTER changing the canvas
			var dingRenderer = serviceRegistrar.getService(DingRenderer.class);
			var re = dingRenderer.getRenderingEngine(view);
			
			var cyAnnotator = re.getCyAnnotator();
			var tree = cyAnnotator.getAnnotationTree();
			tree.resetZOrder();
			
			cyAnnotator.fireAnnotationsReordered();
		}
		
		annotation.update();
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class, String.class, Annotation.class);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(Annotation.class))
			return (R) updatedAnnotation;

		if (type.equals(String.class)) {
			final String result;
			
			if (updatedAnnotation == null)
				result = "Nothing added";
			else
				result = "Updated annotation " + updatedAnnotation.toString();
			
			return (R) result;
		}

		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (updatedAnnotation == null)
					return "{}";
				
				return AnnotationJsonConverter.toJson((DingAnnotation) updatedAnnotation);
			};
			
			return (R) res;
		}
		
		return null;
	}
}
