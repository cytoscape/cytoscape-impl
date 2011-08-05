package org.cytoscape.ding.icon;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class TextIcon extends VisualPropertyIcon<Object> {

	private static final long serialVersionUID = -4217147694751380332L;
	
	private static final Font font = new Font("SansSerif", Font.BOLD, 18);

	public TextIcon(final Object value, final int width, final int height,
			final String name) {
		super(value, width, height, name);
		this.leftPad = 15;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2d = (Graphics2D) g;

		// Turn AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		final Font original = g2d.getFont();
		if (value != null) {
			g2d.setColor(color);
			g2d.setFont(font);
			g2d.drawString(value.toString(), leftPad, height/2+7);
		}
		
		g2d.setFont(original);

	}

}
