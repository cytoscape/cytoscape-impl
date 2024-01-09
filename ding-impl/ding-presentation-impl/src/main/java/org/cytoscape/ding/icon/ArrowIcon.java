package org.cytoscape.ding.icon;

import static org.cytoscape.ding.DArrowShape.ARROW;
import static org.cytoscape.ding.DArrowShape.ARROW_SHORT;
import static org.cytoscape.ding.DArrowShape.CIRCLE;
import static org.cytoscape.ding.DArrowShape.HALF_BOTTOM;
import static org.cytoscape.ding.DArrowShape.HALF_CIRCLE;
import static org.cytoscape.ding.DArrowShape.HALF_TOP;
import static org.cytoscape.ding.DArrowShape.OPEN_CIRCLE;
import static org.cytoscape.ding.DArrowShape.OPEN_HALF_CIRCLE;
import static org.cytoscape.ding.DArrowShape.OPEN_SQUARE;
import static org.cytoscape.ding.DArrowShape.SQUARE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.UIManager;

import org.cytoscape.ding.DArrowShape;

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

/**
 * Icon for arrow shape.
 */
public class ArrowIcon extends VisualPropertyIcon<DArrowShape> {
	
	private final static long serialVersionUID = 1202339877462891L;
	
	private static final int MIN_EDGE_LENGTH = 4;
	private static final int MIN_EDGE_WIDTH = 1;
	private static final int MIN_ARROW_BORDER_WIDTH = 2;
	
	public ArrowIcon(DArrowShape dArrowShape, int width, int height) {
		super(dArrowShape, width, height, dArrowShape.getDisplayName());
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		var g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double w = width;
		double h = height;
		double x1 = x;
		double y1 = y + h / 2.0;
		double x2 = x1 + w;
		double y2 = y1;
		
		float edgeWidth = Math.max(height / 10f, MIN_EDGE_WIDTH);
		var fg = getForeground(c);
		
		if (value == null) {
			// If shape is not defined, treat as no-arrow.
			drawEdge(edgeWidth, fg, g2d, x1, y1, x2, y2);
		} else {
			var arrowShape = value.getPresentationShape();
			var shape = value.getShape();
			var bounds = shape.getBounds2D();
			var af = new AffineTransform();
			
			// Adjust position if it is NOT in first quadrant.
			double minx = bounds.getMinX();
			double miny = bounds.getMinY();
			
			if (minx < 0) {
				af.setToTranslation(Math.abs(minx), 0);
				shape = af.createTransformedShape(shape);
			}
			if (miny < 0) {
				af.setToTranslation(0, Math.abs(miny));
				shape = af.createTransformedShape(shape);
			}
			
			bounds = shape.getBounds2D();
			
			double sw = bounds.getWidth(); // Shape width
			double sh = bounds.getHeight() * 2; // Shape height
	
			double maxArrowHeight = maxArrowHeight(bounds);
			double maxArrowWidth = maxArrowWidth(bounds);
			
			if (isSquaredShape())
				maxArrowHeight = maxArrowWidth = Math.min(maxArrowHeight, maxArrowWidth);
			
			double originalXYRatio = sw / sh;
			double xRatio = maxArrowWidth / sw;
			double yRatio = maxArrowHeight / sh;
			af.setToScale(xRatio * originalXYRatio, yRatio);
			shape = af.createTransformedShape(shape);
	
			bounds = shape.getBounds2D();
			
			af.setToTranslation(x2 - bounds.getWidth(), yTranslation(y2, bounds));
			shape = af.createTransformedShape(shape);
			
			bounds = shape.getBounds2D();
			
			x2 = xIntersection(bounds);
			drawEdge(edgeWidth, fg, g2d, x1, y1, x2, y2);
			drawArrow(shape, arrowShape.isFilled(), edgeWidth, fg, c.getBackground(), g2d);
		}
	}

	private void drawEdge(float edgeWidth, Color fg, Graphics2D g2d, double x1, double y1, double x2, double y2) {
		fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 128);
		
		g2d.setColor(fg);
		g2d.setStroke(new BasicStroke(edgeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
		g2d.draw(new Line2D.Double(x1, y1, x2, y2));
	}
	
	private void drawArrow(Shape shape, boolean filled, float edgeWidth, Color fg, Color bg, Graphics2D g2d) {
		if (filled) {
			g2d.setColor(fg);
			g2d.fill(shape);
		} else {
			float borderWidth = Math.max(edgeWidth / 2f, MIN_ARROW_BORDER_WIDTH);
			
			g2d.setColor(bg);
			g2d.fill(shape);
			g2d.setColor(fg);
			g2d.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
			g2d.draw(shape);
		}
	}
	
	/**
	 * @return true if the arrow shape must have the same values for its width and its height.
	 */
	private boolean isSquaredShape() {
		return value == SQUARE || value == OPEN_SQUARE ||
				value == CIRCLE || value == OPEN_CIRCLE ||
				value == HALF_CIRCLE || value == OPEN_HALF_CIRCLE;
	}
	
	private double maxArrowWidth(Rectangle2D bounds) {
		double w = width;
		double ratio = width / height;
		
		if (ratio <= 1.1)
			w = Math.max(height, w);
		else if (ratio >= 3.0)
			w /= 3;
		else if (ratio >= 2.0)
			w /= 2;
		
		w -= MIN_EDGE_LENGTH;
		
		return w;
	}
	
	private double maxArrowHeight(Rectangle2D bounds) {
		double h = height;
		double ratio = width / height;
		
		if (ratio <= 1.1)
			h = Math.min(height, width);
		
		return h;
	}
	
	private double yTranslation(double y2, Rectangle2D bounds) {
		double ty = y2 - bounds.getHeight() / 2;
		
		if (value == HALF_TOP)
			return ty - bounds.getHeight() / 2;
		
		if (value == HALF_BOTTOM)
			return ty + bounds.getHeight() / 2;
		
		return ty;
	}
	
	/**
	 * Adjusts the x2 value so the edge intersects the arrow properly, depending on the arrow type.
	 */
	private double xIntersection(Rectangle2D bounds) {
		final double x;
		
		if (value == ARROW || value == ARROW_SHORT)
			x = bounds.getMinX();
		else if (value == HALF_TOP || value == HALF_BOTTOM)
			x = bounds.getCenterX() + (bounds.getMaxX() - bounds.getCenterX()) / 2;
		else
			x = bounds.getCenterX() - 2;
		
		return x;
	}
	
	private static Color getForeground(Component c) {
		var fg = c.getForeground();
		
		if (fg == null)
			fg = UIManager.getColor("Label.foreground");
		
		return fg;
	}
}
