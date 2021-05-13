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


/**
 * Icon for node shapes.
 */
public class NodeIcon extends VisualPropertyIcon<Shape> {
	
	private final static long serialVersionUID = 1202339876280466L;
	
	private static final Stroke BASIC_STROKE = new BasicStroke(2.0f);

	private Shape newShape;
	private Graphics2D g2d;

	
	public NodeIcon(final Shape shape, int width, int height, String name) {
		super(shape, width, height, name);
		adjustShape();
	}

	private void adjustShape() {
		final double shapeWidth = value.getBounds2D().getWidth();
		final double shapeHeight = value.getBounds2D().getHeight();

		final double xRatio = width / shapeWidth;
		final double yRatio = height / shapeHeight;

		final AffineTransform af = new AffineTransform();
		af.setToScale(xRatio, yRatio);
		newShape = af.createTransformedShape(value);
	}

	@Override
	public void paintIcon(final Component c, final Graphics g, int x, int y) {		
		g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.translate(x, y);
		g2d.setColor(c.getForeground());
		g2d.setStroke(BASIC_STROKE);
		g2d.draw(newShape);
		g2d.translate(-x, -y);
	}

	@Override
	public NodeIcon clone() {
		final NodeIcon cloned = new NodeIcon(value, width, height, name);
		return cloned;
	}
}
