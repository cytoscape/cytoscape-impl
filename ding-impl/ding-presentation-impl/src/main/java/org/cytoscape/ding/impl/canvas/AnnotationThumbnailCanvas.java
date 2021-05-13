package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.view.presentation.annotations.Annotation;

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

public class AnnotationThumbnailCanvas<GP extends GraphicsProvider> extends DingCanvas<GP> {
	
	private Collection<Annotation> annotations;
	
	public AnnotationThumbnailCanvas(GP graphics, Collection<Annotation> annotations) {
		super(graphics);
		this.annotations = annotations;
	}

	@Override
	public String getCanvasDebugName() {
		return "Annotations";
	}

	@Override
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
		Graphics2D g = graphicsProvider.getGraphics(true);
		if(g == null)
			return;
		
		var transform = graphicsProvider.getTransform();
		g.transform(transform.getPaintAffineTransform());
		
		Rectangle2D visibleArea = transform.getNetworkVisibleAreaNodeCoords();
		
		for(var ann : annotations) {
			DingAnnotation a = (DingAnnotation) ann;
			if(visibleArea.intersects(a.getBounds()) && a.getGroupParent() == null) {
			 	a.paint(g, false);
			}
		}
		
		g.dispose();
	}
	
}
