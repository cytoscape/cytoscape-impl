package org.cytoscape.ding.impl.canvas;

import java.awt.Color;
import java.awt.Graphics2D;

public interface GraphicsProvider {

	NetworkTransform getTransform();
	
	Graphics2D getGraphics();
	
	default void fill(Color color) {
		NetworkTransform t = getTransform();
		Graphics2D g = getGraphics();
		if(g != null) {
			if(color == null) {
				color = new Color(0,0,0,0); // transparent
			}
			g.setColor(color);
			g.fillRect(0, 0, t.getPixelWidth(), t.getPixelHeight());
		}
	}
	
}
