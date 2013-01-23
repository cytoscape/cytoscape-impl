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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Icon renderer for font face.
 *
 */
public class FontFaceIcon extends VisualPropertyIcon<Font> {

	private static final long serialVersionUID = 4629615986711780878L;

	private static final String TEXT = "A";
	
	private static final Color FONT_COLOR = Color.DARK_GRAY;

	private final Font font;

	public FontFaceIcon(Font value, int width, int height, String name) {
		super(value, width, height, name);
		font = new Font(value.getFamily(), value.getStyle(), height);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2d = (Graphics2D) g;
		final Font originalFont = g2d.getFont();

		g2d.setColor(FONT_COLOR);
		// AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setFont(font);
		g2d.drawString(TEXT, x+leftPad+5, y+(c.getHeight()/2));

		g2d.setFont(originalFont);
	}

}
