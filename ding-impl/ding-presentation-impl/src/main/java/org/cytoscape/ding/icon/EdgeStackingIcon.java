package org.cytoscape.ding.icon;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;

import org.cytoscape.view.presentation.property.EdgeStackingVisualProperty;
import org.cytoscape.view.presentation.property.values.EdgeStacking;

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

@SuppressWarnings("serial")
public class EdgeStackingIcon extends VisualPropertyIcon<EdgeStacking> {

	private static final Stroke EDGE_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
	
	public EdgeStackingIcon(EdgeStacking value, int width, int height, String name) {
		super(value, width, height, name);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		var g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setColor(c != null ? c.getForeground() : color);
		g2d.setStroke(EDGE_STROKE);
		g2d.translate(x,  y);
		
		double x1 = 0.0;
		double x2 = x1 + width;
		double xc = width / 2.0; // center x
		double ym = height / 2.0; // middle y
		
		double vpad = Math.max(1.0, height / 6.0);
		
		if (value == EdgeStackingVisualProperty.HAYSTACK) {
			g2d.draw(new Line2D.Double(x1, ym - vpad, x2, ym));
			g2d.draw(new Line2D.Double(x1, ym, x2, ym + 1.5 * vpad));
			g2d.draw(new Line2D.Double(x1, ym + vpad, x2, ym - vpad));
		} else if (value == EdgeStackingVisualProperty.AUTO_BEND) {
			g2d.draw(new QuadCurve2D.Double(
					x1, ym - vpad,
					xc, 0,
					x2, ym - vpad
			));
			g2d.draw(new Line2D.Double(x1, ym, x2, ym));
			g2d.draw(new QuadCurve2D.Double(
					x1, ym + vpad,
					xc, height,
					x2, ym + vpad
			));
		}
		
		g2d.dispose();
	}
}
