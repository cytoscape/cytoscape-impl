package org.cytoscape.ding.impl.cyannotator.tasks;

import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.ANNOTATION_ID;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.annotations.Annotation;
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

public class DuplicateAnnotationsTask extends AbstractTask {

	private static final double SHIFT = 15.0;
	
	private final DRenderingEngine re;
	private final Collection<DingAnnotation> annotations;
	private final AnnotationFactoryManager annotationFactoryManager;

	public DuplicateAnnotationsTask(
			DRenderingEngine re,
			Collection<DingAnnotation> annotations,
			AnnotationFactoryManager annotationFactoryManager
	) {
		this.re = re;
		this.annotations = annotations;
		this.annotationFactoryManager = annotationFactoryManager;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Duplicate Selected Annotations");
		tm.setProgress(-1);

		if (re != null && !annotations.isEmpty()) {
			var annotator = re.getCyAnnotator();
			annotator.markUndoEdit("Duplicate Selected Annotations");
			
			var oldNewMap = new HashMap<DingAnnotation, DingAnnotation>();
			var newArrows = new HashSet<ArrowAnnotationImpl>();
			
			// Duplicate annotations
			for (var a : annotations) {
				var copy = duplicate(a);
				annotator.addAnnotation(copy);
				oldNewMap.put(a, copy);
				
				if (copy instanceof ArrowAnnotationImpl)
					newArrows.add((ArrowAnnotationImpl) copy);
			}
			
			// Update target/source of new Arrows, because they are created with the original references
			for (var arrow : newArrows) {
				var src = oldNewMap.get(arrow.getSource());
				
				if (src != null && annotator.contains(src)/*it could be an arrow we just removed*/) {
					arrow.setSource(src);
					
					var tgt = arrow.getTarget();
					
					if (tgt instanceof DingAnnotation) // It could also be a CyNode!
						arrow.setTarget(oldNewMap.get(tgt));
				} else {
					// The source annotation is mandatory, but it was not duplicated,
					// so this arrow cannot be duplicated either and must be deleted
					arrow.removeAnnotation();
					oldNewMap.values().remove(arrow);
				}
			}
			
			// Bring new annotations to front
			ViewUtils.reorder(new ArrayList<>(oldNewMap.values()), Shift.TO_FRONT, re);
			
			// Select new annotations only
			annotator.clearSelectedAnnotations();
			
			for (var copy : oldNewMap.values())
				copy.setSelected(true);
			
			annotator.postUndoEdit();
		}
	}

	private DingAnnotation duplicate(DingAnnotation a) {
		var argMap = new HashMap<>(a.getArgMap());
		argMap.remove(ANNOTATION_ID);
		
		var type = argMap.get(DingAnnotation.TYPE);
		
		if (type == null)
			type = a.getClass().getName();
		
		// Create annotation
		var copy = (DingAnnotation) annotationFactoryManager.createAnnotation(type, re.getViewModel(), argMap);
		
		if (copy instanceof ArrowAnnotation) {
			// Force source/target to be same references as the ones from the original arrow,
			// because, for some reason, the copy seems to get different ones sometimes (???)
			((ArrowAnnotation) copy).setSource(((ArrowAnnotation) a).getSource());
			
			var tgt = ((ArrowAnnotation) a).getTarget();
			
			if (tgt instanceof Annotation)
				((ArrowAnnotation) copy).setTarget((Annotation) tgt);
			else if (tgt instanceof CyNode)
				((ArrowAnnotation) copy).setTarget((CyNode) tgt);
			else if (tgt instanceof Point2D)
				((ArrowAnnotation) copy).setTarget((Point2D) tgt);
		} else {
			// Move it a few pixels, so it does not hide the copied one
			copy.moveAnnotation(new Point2D.Double(a.getX() + SHIFT, a.getY() + SHIFT));
		}
		
		return copy;
	}
}
