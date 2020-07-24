package org.cytoscape.ding.impl.cyannotator.tasks;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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
			
			var newList = new ArrayList<DingAnnotation>();
			
			// Duplicate annotations
			for (var a : annotations) {
				var copy = duplicate(a);
				annotator.addAnnotation(copy);
				newList.add(copy);
			}
			
			// Bring new annotations to front
			ViewUtils.reorder(newList, Shift.TO_FRONT, re);
			
			// Select new annotations only
			annotator.clearSelectedAnnotations();
			
			for (var copy : newList)
				copy.setSelected(true);
			
			annotator.postUndoEdit();
		}
	}

	private DingAnnotation duplicate(DingAnnotation a) {
		var argMap = a.getArgMap();
		var type = argMap.get(DingAnnotation.TYPE);
		
		if (type == null)
			type = a.getClass().getName();
		
		// Create annotation
		var copy = (DingAnnotation) annotationFactoryManager.createAnnotation(type, re.getViewModel(), argMap);
		
		// Move it a few pixels, so it does not hide the copied one
		copy.moveAnnotation(new Point2D.Double(a.getX() + SHIFT, a.getY() + SHIFT));
		
		return copy;
	}
}
