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
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import org.cytoscape.graph.render.stateful.PaintFactory;

public class LinearGradientPaintFactory extends GradientPaintFactory {
	private Point2D start;
	private Point2D end;
	
	public LinearGradientPaintFactory(List<Color> colors, List<Float> stops) {
		super(colors, stops);
		this.start = new Point2D.Float(0f,0f);
		this.end = new Point2D.Float(1f,0f);
	}

	public LinearGradientPaintFactory(List<Color> colors, List<Float> stops, Point2D start, Point2D end) {
		super(colors, stops);
		this.start = start;
		this.end = end;
	}
	
	public Paint getPaint(Rectangle2D bound) {
		this.paint = new LinearGradientPaint(scale(start, bound), scale(end, bound), stopArray, colorArray);

		return paint;
	}
}
