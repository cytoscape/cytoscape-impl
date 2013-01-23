package org.cytoscape.ding.customgraphics.paint;

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

import java.awt.Color;
import java.awt.RadialGradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import org.cytoscape.graph.render.stateful.PaintFactory;

public class RadialGradientPaintFactory extends GradientPaintFactory {
	private Point2D center;
	private float size;
	
	public RadialGradientPaintFactory(List<Color> colors, List<Float> stops) {
		super(colors, stops);
		this.center = new Point2D.Float(0.5f, 0.5f);
		this.size = .5f;
	}

	public RadialGradientPaintFactory(List<Color> colors, List<Float> stops, Point2D center, float size) {
		super(colors, stops);
		this.center = center;
		this.size = size;
	}
	

	public Paint getPaint(Rectangle2D bound) {
		double diameter = Math.min(bound.getWidth(), bound.getHeight());

		paint = new RadialGradientPaint(scale(center, bound), (float)(size*diameter), stopArray, colorArray);
		
		return paint;
	}
}
