package org.cytoscape.ding.icon;

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

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
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

		double x1 = x;
		double y1 = y + height / 2.0;
		double x2 = x1 + width;
		double y2 = y1;
		
		// If shape is not defined, treat as no-arrow.
		if (value != null) {
			final AffineTransform af = new AffineTransform();
			g2d.setStroke(new BasicStroke(2.0f));
	
			Shape shape = value;
			final double minx = shape.getBounds2D().getMinX();
			final double miny = shape.getBounds2D().getMinY();
			
			// Adjust position if it is NOT in first quadrant.
			if (minx < 0) {
				af.setToTranslation(Math.abs(minx), 0);
				shape = af.createTransformedShape(shape);
			}
	
			if (miny < 0) {
				af.setToTranslation(0, Math.abs(miny));
				shape = af.createTransformedShape(shape);
			}
	
			final double shapeWidth = shape.getBounds2D().getWidth();
			final double shapeHeight = shape.getBounds2D().getHeight() * 2;
	
			final double originalXYRatio = shapeWidth / shapeHeight;
			final double xRatio = (width / 3) / shapeWidth;
			final double yRatio = height / shapeHeight;
			af.setToScale(xRatio * originalXYRatio, yRatio);
			shape = af.createTransformedShape(shape);
	
			Rectangle2D bound = shape.getBounds2D();
			af.setToTranslation(x2 - bound.getWidth(), y2 - bound.getHeight() / 2);
			shape = af.createTransformedShape(shape);
			
			g2d.fill(shape);
			
			x2 = shape.getBounds2D().getCenterX() - 2;
		}
		
		g2d.setStroke(EDGE_STROKE);
		g2d.draw(new Line2D.Double(x1, y1, x2, y2));
	}
}
