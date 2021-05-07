package org.cytoscape.ding.impl.cyannotator.utils;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import javax.swing.Icon;

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

public class ShapeIcon implements Icon {

	private final Shape shape;
	private final int width;
	private final int height;

	public ShapeIcon(Shape shape, int width, int height) {
		this.shape = shape;
		this.width = width;
		this.height = height;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		var g2 = (Graphics2D) g.create();
		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		
		g2.setPaint(c.getForeground());
		g2.setStroke(new BasicStroke(1));
		
		var sb = shape.getBounds();
		g2.translate(x + (width - sb.getWidth()) / 2.0, y + (height - sb.getHeight()) / 2.0);
		g2.draw(shape);
		
		g2.dispose();
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
}
