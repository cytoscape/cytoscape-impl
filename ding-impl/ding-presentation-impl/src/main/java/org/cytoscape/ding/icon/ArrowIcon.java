package org.cytoscape.ding.icon;

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;


/**
 * Icon for arrow shape.
 */
public class ArrowIcon extends VisualPropertyIcon<Shape> {
	private final static long serialVersionUID = 1202339877462891L;
	
	private static final int EDGE_WIDTH = 4;
	private static final Stroke EDGE_STROKE = new BasicStroke(EDGE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
	protected Graphics2D g2d;


	public ArrowIcon(final Shape shape, int width, int height, String name) {
		super(shape, width, height, name);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(c.getForeground());

		int x1 = x + (c.getWidth() - width) / 2;
		int y1 = c.getHeight()/2;
		int x2 = x1 + width;
		int y2 = y1;
		
		// If shape is not defined, treat as no-arrow.
		if (value != null) {
			final AffineTransform af = new AffineTransform();
			g2d.setStroke(new BasicStroke(2.0f));
	
			Shape newShape = value;
			final double minx = newShape.getBounds2D().getMinX();
			final double miny = newShape.getBounds2D().getMinY();
			
			// Adjust position if it is NOT in first quadrant.
			if (minx < 0) {
				af.setToTranslation(Math.abs(minx), 0);
				newShape = af.createTransformedShape(newShape);
			}
	
			if (miny < 0) {
				af.setToTranslation(0, Math.abs(miny));
				newShape = af.createTransformedShape(newShape);
			}
	
			final double shapeWidth = newShape.getBounds2D().getWidth();
			final double shapeHeight = newShape.getBounds2D().getHeight()*2;
	
			final double originalXYRatio = shapeWidth / shapeHeight;
	
			final double xRatio = (width / 3) / shapeWidth;
			final double yRatio = height / shapeHeight;
			af.setToScale(xRatio * originalXYRatio, yRatio);
			newShape = af.createTransformedShape(newShape);
	
			Rectangle2D bound = newShape.getBounds2D();
			af.setToTranslation(width - bound.getCenterX(), y + (bound.getHeight())/2);
			newShape = af.createTransformedShape(newShape);
			
			g2d.fill(newShape);
			
			x2 = (int) (newShape.getBounds2D().getCenterX()) - 2;
		}
		
		g2d.setStroke(EDGE_STROKE);
		g2d.drawLine(x1, y1, x2, y2);
	}
}
