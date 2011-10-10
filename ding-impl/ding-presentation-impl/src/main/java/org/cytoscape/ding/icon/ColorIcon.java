package org.cytoscape.ding.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class ColorIcon extends VisualPropertyIcon<Color> {

	private static final long serialVersionUID = 5636448639330547200L;
	
	private static final float ARC_RATIO = 0.35f;

	public ColorIcon(final Color value, final int width, final int height, final String name) {
		super(value, width, height, name);
	}

	@Override public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2d = (Graphics2D) g;
		
		// Turn AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Float arc = width * ARC_RATIO;
		g2d.setColor(value);
		g2d.fillRoundRect(x+leftPad, y, width, height, arc.intValue(), arc.intValue());
	}

}
