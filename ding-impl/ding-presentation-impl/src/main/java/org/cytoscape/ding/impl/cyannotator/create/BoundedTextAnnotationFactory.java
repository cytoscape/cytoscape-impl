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

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.annotations.BoundedTextAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.BoundedTextAnnotationDialog;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;

public class BoundedTextAnnotationFactory extends AbstractDingAnnotationFactory<BoundedTextAnnotation> {

	public BoundedTextAnnotationFactory(final CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
	}
	
	@Override
	public JDialog createAnnotationDialog(DGraphView view, Point2D location) {
		return new BoundedTextAnnotationDialog(view, location, getActiveWindow());
	}

	@Override
	public BoundedTextAnnotation createAnnotation(Class<? extends BoundedTextAnnotation> type, CyNetworkView view,
			Map<String, String> argMap) {
		if (!(view instanceof DGraphView))
			return null;

		DGraphView dView = (DGraphView) view;
		
		if (type.equals(BoundedTextAnnotation.class)) {
			final BoundedTextAnnotationImpl a = new BoundedTextAnnotationImpl(dView, argMap, getActiveWindow());
			a.update();
			
			return (BoundedTextAnnotation) a;
		} else {
			return null;
		}
	}
}
