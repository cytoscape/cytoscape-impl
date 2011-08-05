package org.cytoscape.ding.icon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

public class ColorIcon extends VisualPropertyIcon<Color> {

	private static final long serialVersionUID = 5636448639330547200L;
	
	private static final Stroke BORDER = new BasicStroke(1.0f);

	public ColorIcon(final Color value, final int width, final int height, final String name) {
		super(value, width, height, name);
	}

	@Override public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2d = (Graphics2D) g;
		
		// Turn AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(value);
		g2d.fillRect(x+leftPad, y+bottomPad, width, height);
		g2d.setColor(color);
		g2d.setStroke(BORDER);
		g2d.drawRect(x+leftPad, y+bottomPad, width, height);

	}

}
