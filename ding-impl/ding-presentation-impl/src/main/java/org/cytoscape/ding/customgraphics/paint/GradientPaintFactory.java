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

public abstract class GradientPaintFactory implements PaintFactory {
	protected Color[] colorArray;
	protected float[] stopArray;
	
	protected Paint paint;

	public GradientPaintFactory(List<Color>colorList, List<Float>stopList) {
		colorArray = new Color[colorList.size()];
		stopArray = new float[colorList.size()];
		for (int index = 0; index < colorArray.length; index++) {
			colorArray[index] = colorList.get(index);
			stopArray[index] = stopList.get(index).floatValue();
		}
	}

	protected Point2D scale(Point2D point, Rectangle2D bound) {
		double xvalue = point.getX() * bound.getWidth() + bound.getX();
		double yvalue = point.getY() * bound.getHeight() + bound.getY();
		return new Point2D.Float((float)xvalue, (float)yvalue);
	}

}
