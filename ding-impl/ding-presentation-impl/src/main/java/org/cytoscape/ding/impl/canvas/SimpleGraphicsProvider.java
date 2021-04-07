package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;

public class SimpleGraphicsProvider implements GraphicsProvider {

	private final NetworkTransform transform;
	private final Graphics2D graphics;
	
	public SimpleGraphicsProvider(NetworkTransform transform, Graphics2D graphics) {
		this.transform = transform;
		this.graphics = graphics;
	}
	
	@Override
	public Graphics2D getGraphics(boolean clear) {
		return (Graphics2D) graphics.create();
	}

	@Override
	public NetworkTransform getTransform() {
		return transform;
	}
}
