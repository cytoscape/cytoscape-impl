package org.cytoscape.ding.impl.cyannotator.create;

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

import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.LoadImageDialog;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;

public class ImageAnnotationFactory extends AbstractDingAnnotationFactory<ImageAnnotation> {
	
	public ImageAnnotationFactory(final CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
	}

	@Override
	public JDialog createAnnotationDialog(DGraphView view, Point2D location) {
		final CustomGraphicsManager customGraphicsManager = serviceRegistrar.getService(CustomGraphicsManager.class);
		
		return new LoadImageDialog(view, location, customGraphicsManager, getActiveWindow());
	}

	@Override
	public ImageAnnotation createAnnotation(Class<? extends ImageAnnotation> clazz, CyNetworkView view,
			Map<String, String> argMap) {
		if (!(view instanceof DGraphView))
			return null;

		DGraphView dView = (DGraphView) view;

		if (ImageAnnotation.class.equals(clazz)) {
			final CustomGraphicsManager customGraphicsManager = serviceRegistrar
					.getService(CustomGraphicsManager.class);
			final ImageAnnotationImpl a = new ImageAnnotationImpl(dView.getCyAnnotator(), dView, argMap,
					customGraphicsManager, getActiveWindow());
			a.update();

			return (ImageAnnotation) a;
		} else {
			return null;
		}
	}
}
