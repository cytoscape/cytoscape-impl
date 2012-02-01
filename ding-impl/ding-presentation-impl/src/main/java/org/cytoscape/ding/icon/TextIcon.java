package org.cytoscape.ding.icon;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class TextIcon extends VisualPropertyIcon<Object> {

	private static final long serialVersionUID = -4217147694751380332L;

	private static final int FONT_SIZE_DEFAULT = 20;
	private static final int FONT_SIZE_SMALL = 16;

	private static final int MAX_TEXT_LEN = 5;

	private static final Font FONT = new Font("SansSerif", Font.BOLD, FONT_SIZE_DEFAULT);
	private static final Font FONT_SMALL = new Font("SansSerif", Font.BOLD, FONT_SIZE_SMALL);

	public TextIcon(final Object value, final int width, final int height, final String name) {
		super(value, width, height, name);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2d = (Graphics2D) g;

		// Turn AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Font original = g2d.getFont();

		if (value != null) {
			String text = value.toString();
			final int textLen = text.length();

			g2d.translate(leftPad, (c.getHeight()) / 2);
			
			g2d.setColor(color);
			if (textLen > MAX_TEXT_LEN) {
				text = text.substring(0, 5) + "...";
				g2d.setFont(FONT_SMALL);
			} else
				g2d.setFont(FONT);

			g2d.drawString(text, 0, 0);
			g2d.translate(-leftPad, -((c.getHeight()) / 2));
		}

		g2d.setFont(original);
	}

}
