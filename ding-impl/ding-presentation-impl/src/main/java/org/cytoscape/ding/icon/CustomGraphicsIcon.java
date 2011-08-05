package org.cytoscape.ding.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.cytoscape.ding.customgraphics.CyCustomGraphics;

public class CustomGraphicsIcon extends VisualPropertyIcon<CyCustomGraphics<?>> {

	private static final long serialVersionUID = -216647303312376087L;
	
	
	public CustomGraphicsIcon(CyCustomGraphics<?> value, int width, int height,
			String name) {
		super(value, width, height, name);
		this.setImage(value.getRenderedImage());
	}
	
	@Override public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2d = (Graphics2D) g;

		// AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(0, bottomPad);
		g2d.drawImage(getImage(), 0, (c.getHeight() - height) / 2, width, height, c);
		g2d.translate(0, -bottomPad);
	}

}
