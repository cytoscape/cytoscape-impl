package org.cytoscape.view.vizmap.gui.internal.view.cellrenderer;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.Icon;
import javax.swing.UIManager;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

/**
 * CyColorCellRenderer. Provides both table/Llist cell renderers.
 */
public class CyColorCellRenderer extends DefaultCellRenderer {
	
	private final static long serialVersionUID = 1202339868706383L;
	
	@Override
	protected String convertToString(Object value) {
		// Do not return color as string.
		return null;
	}
	
	@Override
	protected Icon convertToIcon(Object value) {
		if (value == null)
			return null;

		if (value instanceof Number)
			value = new Color(((Number) value).intValue());

		return new PaintIcon((Paint) value);
	}

	public static class PaintIcon implements Icon {
		
		private final Paint color;
		private int width;
		private int height;

		public PaintIcon(Paint color) {
			this(color, 70, 10);
		}

		public PaintIcon(Paint color, int width, int height) {
			this.color = color;
			this.width = width;
			this.height = height;
		}

		public int getIconHeight() {
			return height;
		}

		public int getIconWidth() {
			return width;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g;
			Paint oldPaint = g2d.getPaint();

			if (c != null)
				width = c.getWidth() - 6;

			if (color != null) {
				g2d.setPaint(color);
				g.fillRect(3, y, getIconWidth(), getIconHeight());
			}

			g.setColor(UIManager.getColor("controlDkShadow"));
			g.drawRect(3, y, getIconWidth(), getIconHeight());

			g2d.setPaint(oldPaint);
		}
	}
}
