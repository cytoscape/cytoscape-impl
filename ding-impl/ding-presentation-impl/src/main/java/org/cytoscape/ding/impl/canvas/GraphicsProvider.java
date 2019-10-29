package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;

public interface GraphicsProvider {

	public NetworkTransform getTransform();
	
	public Graphics2D getGraphics();
	
}
