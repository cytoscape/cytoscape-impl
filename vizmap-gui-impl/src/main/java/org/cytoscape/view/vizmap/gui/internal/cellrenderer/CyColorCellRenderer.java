package org.cytoscape.view.vizmap.gui.internal.cellrenderer;

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
 * 
 * @since Cytoscape 2.5
 */
public class CyColorCellRenderer extends DefaultCellRenderer {
	private final static long serialVersionUID = 1202339868706383L;

	
	public static String toHex(Color color) {
		String red = Integer.toHexString(color.getRed());
		String green = Integer.toHexString(color.getGreen());
		String blue = Integer.toHexString(color.getBlue());

		if (red.length() == 1)
			red = "0" + red;

		if (green.length() == 1)
			green = "0" + green;

		if (blue.length() == 1)
			blue = "0" + blue;

		return ("#" + red + green + blue).toUpperCase();
	}

	protected String convertToString(Object value) {
		// Do not return color as string.
		return null;
	}

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
