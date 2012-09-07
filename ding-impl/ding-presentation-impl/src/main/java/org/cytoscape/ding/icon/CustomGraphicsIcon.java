package org.cytoscape.ding.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

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
		final Image original = this.getImage();
		final Image resized = original.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		g2d.drawImage(resized, leftPad, (c.getHeight() - height) / 2, width, height, c);
	}

}
