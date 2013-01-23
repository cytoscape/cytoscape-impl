package org.cytoscape.ding.impl.cyannotator.create;

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

import org.cytoscape.ding.impl.DGraphView;
import javax.swing.JFrame;
import java.awt.geom.Point2D;
import java.util.Map;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.LoadImageDialog;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;

public class ImageAnnotationFactory implements AnnotationFactory {
	private final CustomGraphicsManager customGraphicsManager;

	public ImageAnnotationFactory(CustomGraphicsManager customGraphicsManager) {
		this.customGraphicsManager = customGraphicsManager;
	}

	public JFrame createAnnotationFrame(DGraphView view, Point2D location) {
		return new LoadImageDialog(view, location, customGraphicsManager);
	}

	public Annotation createAnnotation(String type, CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		if ( type.equals(ImageAnnotationImpl.NAME) ) {
			Annotation a = new ImageAnnotationImpl(cyAnnotator, view,argMap,customGraphicsManager);
			a.update();
			return a;
		} else 
			return null;
	}
}
