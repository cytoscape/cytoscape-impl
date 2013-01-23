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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;

import org.cytoscape.view.presentation.property.values.Bend;

public class EdgeBendIcon extends VisualPropertyIcon<Bend> {

	private static final long serialVersionUID = 3321774231185088226L;

	private static final Stroke EDGE_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
	private static final int FONT_SIZE = 32;
	private static final Font FONT = new Font("SansSerif", Font.BOLD, FONT_SIZE);
	private static final Color NUMBER_COLOR = new Color(100, 100, 100, 90);

	public EdgeBendIcon(Bend value, int width, int height, final String name) {
		super(value, width, height, name);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(color);
		g2d.setStroke(EDGE_STROKE);
		final int yPosition = (height + 20) / 2;

		// Turn AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Integer handles = value.getAllHandles().size();
		if (handles == 0) {
			// No Handles: Just draw straight line
			g2d.draw(new Line2D.Double(leftPad, yPosition, width * 1.5, yPosition));
		} else {
			final double newWidth = width * 1.5;
			final CubicCurve2D curvedLine = new CubicCurve2D.Double(leftPad, yPosition,
					newWidth/2, yPosition + height,
					newWidth*3/4, yPosition - height,
					newWidth, yPosition);
			g2d.draw(curvedLine);

			final Font original = g2d.getFont();

			if (value != null) {
				g2d.setColor(NUMBER_COLOR);
				g2d.setFont(FONT);
				final int cHeight = c.getHeight();
				g2d.drawString(handles.toString(), x+leftPad+15, y + (cHeight / 2) - 5);
			}

			// Set to original
			g2d.setFont(original);
		}
	}

}
