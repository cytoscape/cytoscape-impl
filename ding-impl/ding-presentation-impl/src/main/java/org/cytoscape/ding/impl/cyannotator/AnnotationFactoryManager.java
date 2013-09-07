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
import java.util.ArrayList;
import java.util.Map;

import org.cytoscape.view.model.CyNetworkView; 
import org.cytoscape.ding.impl.DGraphView;

import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.BoundedTextAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotationImpl;

public class AnnotationFactoryManager {

	List<AnnotationFactory> annotationFactories;

	public AnnotationFactoryManager() {
		annotationFactories = new ArrayList<AnnotationFactory>();
	}

	// This method is used to create annotations when we're reading the serialization from a saved
	// session.  Note that we need to do some funky things with the type to support backwares compatibility
	public Annotation createAnnotation(String type, CyNetworkView view, Map<String,String> argMap) {
		Class clazz = null;
		try{
			clazz = Class.forName(type);
			if (Annotation.class.isAssignableFrom(clazz))
				return createAnnotation(clazz, view, argMap);
			return null;
		} catch (Exception e) {
			clazz = null;
		}

		if (type.equals("ARROW"))
			return createAnnotation(ArrowAnnotation.class, view, argMap);

		if (type.equals("SHAPE"))
			return createAnnotation(ShapeAnnotation.class, view, argMap);

		if (type.equals("TEXT"))
			return createAnnotation(TextAnnotation.class, view, argMap);

		if (type.equals("BOUNDEDTEXT"))
			return createAnnotation(BoundedTextAnnotation.class, view, argMap);

		if (type.equals("IMAGE"))
			return createAnnotation(ImageAnnotation.class, view, argMap);

		return null;
	}

	public Annotation createAnnotation(Class type, CyNetworkView view, Map<String,String> argMap) {
		Annotation annotation = null;
		for (AnnotationFactory factory: annotationFactories) {
			annotation =  factory.createAnnotation(type, view, argMap);
			if (annotation != null)
				break;
		}

		return annotation;
	}

	public void addAnnotationFactory(AnnotationFactory factory, Map props) {
		if ( factory != null )
			annotationFactories.add(factory);
	}

	public void removeAnnotationFactory(AnnotationFactory factory, Map props) {
		if ( factory != null )
			annotationFactories.remove(factory);
	}
}
