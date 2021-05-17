package org.cytoscape.view.table.internal.impl.icon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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
public class ColorIcon extends VisualPropertyIcon<Color> {

	private static final float ARC_RATIO = 0.35f;
	private static final Stroke STROKE = new BasicStroke(0.5f);

	public ColorIcon(Color value, int width, int height, String name) {
		super(value, width, height, name);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		var g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(value);
		Float arc = width * ARC_RATIO;
		g2d.fillRoundRect(x, y, width, height, arc.intValue(), arc.intValue());
		
		g2d.setStroke(STROKE);
		g2d.setColor(c.getForeground());
		g2d.drawRoundRect(x, y, width, height, arc.intValue(), arc.intValue());
	}
}
