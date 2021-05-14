package org.cytoscape.ding.impl.cyannotator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

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
 * Stores style attributes (e.g. color, line width, shape) from one annotation so it can be pasted to other annotations.
 */
public class AnnotationClipboard {
	
	private Map<String, String> styleMap;

	public void copyStyle(DingAnnotation annotation) {
		var argMap = annotation.getArgMap();
		
		if (argMap != null) {
			styleMap = new HashMap<>(argMap);
			styleMap.remove(Annotation.ZOOM);
			styleMap.remove(Annotation.CANVAS);
			styleMap.remove(Annotation.X);
			styleMap.remove(Annotation.Y);
			styleMap.remove(Annotation.Z);
			styleMap.remove(Annotation.NAME);
			
			styleMap.remove(ShapeAnnotation.HEIGHT);
			styleMap.remove(ShapeAnnotation.WIDTH);
			
			styleMap.remove(ImageAnnotation.URL);
			
			styleMap.remove(GroupAnnotation.MEMBERS);
		}
	}

	public void pasteStyle(Collection<DingAnnotation> annotations) {
		if (styleMap != null) {
			for (var a : annotations)
				a.setStyle(styleMap);
		}
	}
	
	public boolean isEmpty() {
		return styleMap == null || styleMap.isEmpty();
	}
}
