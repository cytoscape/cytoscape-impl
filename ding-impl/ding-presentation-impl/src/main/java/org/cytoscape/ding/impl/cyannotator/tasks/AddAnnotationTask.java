package org.cytoscape.ding.impl.cyannotator.tasks;

import static java.util.Collections.emptyMap;
import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;

import java.awt.Point;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ArrowAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ImageAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.ui.AnnotationMediator;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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

public class AddAnnotationTask extends AbstractTask {

	private final DRenderingEngine re;
	private final Point location;
	private final AnnotationFactory annotationFactory;
	private final AnnotationMediator annotationMediator;

	public AddAnnotationTask(
			DRenderingEngine re,
			Point location,
			AnnotationFactory<?> annotationFactory,
			AnnotationMediator annotationMediator
	) {
		this.re = re;
		this.location = location != null ? location : re.getComponentCenter();
		this.annotationFactory = annotationFactory;
		this.annotationMediator = annotationMediator;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Add Annotation");
		
		if (re != null && annotationFactory instanceof AbstractDingAnnotationFactory) {
			invokeOnEDT(() -> {
				var cyAnnotator = re.getCyAnnotator();
				cyAnnotator.markUndoEdit("Create " + annotationFactory.getName() + " Annotation");
				
				var view = re.getViewModel();
				var source = re.getPicker().getAnnotationAt(location); // For ArrowAnnotations only!
				
				final Annotation annotation;
				
				if (annotationFactory instanceof ImageAnnotationFactory) {
					var dialog = ((ImageAnnotationFactory) annotationFactory).createLoadImageDialog(view, location);
					dialog.setVisible(true);
					annotation = dialog.getAnnotation();
					
					if (annotation == null)
						return;
				} else {
					if (annotationFactory instanceof ArrowAnnotationFactory
							&& (source == null || source instanceof ArrowAnnotation))
						return; // e.g. cannot create an arrow when the user did not click another annotation first!
					
					annotation = annotationFactory.createAnnotation(annotationFactory.getType(), view, emptyMap());
					tm.setStatusMessage("Created annotation: " + annotation.getName());
				}
				
				var editor = ((AbstractDingAnnotationFactory) annotationFactory).getEditor();
				
				// No need to set the new annotation to the editor now,
				// so just ask the editor to apply the previous styles
				if (editor != null)
					editor.apply(annotation);
				
				if (annotation instanceof ArrowAnnotationImpl) {
					// Arrow...
					((ArrowAnnotationImpl) annotation).setSource(source);
					
					cyAnnotator.addAnnotation(annotation);
					
					// NOTE: Do not select arrow annotation now! It should be done only after the arrow's target is set.
					
					cyAnnotator.positionArrow((ArrowAnnotationImpl) annotation);
					annotation.update();
				} else {
					// Other annotations types...
					if (annotation instanceof DingAnnotation) {
						var annotationLocation = re.getTransform().getNodeCoordinates(location);
						((DingAnnotation) annotation).setLocation(annotationLocation.getX(), annotationLocation.getY());
						annotation.update();
					}
					
					cyAnnotator.addAnnotation(annotation);
					
					if (annotation instanceof DingAnnotation) {
						// Select only the new annotation
						cyAnnotator.clearSelectedAnnotations();
						ViewUtils.selectAnnotation(re, (DingAnnotation) annotation);
					}
				}
				
				cyAnnotator.postUndoEdit();
				
				// The annotation can now be edited by the user, now that it has been created
				annotationMediator.editAnnotation(annotation);
			});
		}
	}
}
