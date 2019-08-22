package org.cytoscape.ding.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.work.DiscreteProgressMonitor;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class AnnotationCanvas extends DingCanvas {
	
	private final DingAnnotation.CanvasID canvasID;
	private final DRenderingEngine re;
	
	public AnnotationCanvas(DingAnnotation.CanvasID canvasID, DRenderingEngine re, int width, int height) {
		super(width, height);
		this.re = re;
		this.canvasID = canvasID;
	}

	public DingAnnotation.CanvasID getCanvasID() {
		return canvasID;
	}

	@Override
	public Image paintImage(ProgressMonitor pm, RenderDetailFlags flags) {
		// only paint if we have an image to paint on
		// get image graphics
		if(pm.isCancelled())
			return null;
		
		image.clear();
		Graphics2D g = image.getGraphics();
		g.setTransform(image.getAffineTransform());
		
		Rectangle2D.Float visibleArea = image.getNetworkVisibleAreaNodeCoords();
		List<DingAnnotation> annotations = re.getCyAnnotator().getAnnotations(canvasID);
		
		DiscreteProgressMonitor dpm = pm.toDiscrete(annotations.size());
		
		for (DingAnnotation a : annotations) {
			if(dpm.isCancelled()) {
				return null;
			}
			if(visibleArea.intersects(a.getBounds())) {
				a.paint(g);
			}
			dpm.increment();
		}
		
		g.dispose();
		return image.getImage();
	}
	
}
