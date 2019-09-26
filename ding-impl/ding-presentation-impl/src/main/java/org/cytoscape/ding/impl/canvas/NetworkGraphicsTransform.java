package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;


/**
 * A NetworkTransform that supports drawing onto an arbitrary Graphics object.
 * 
 * This supports drawing in vector graphics for image and PDF export, but does
 * not support drawing each canvas in parallel.
 */
public class NetworkGraphicsTransform extends NetworkTransform {

	private final Graphics2D graphics;
	
	public NetworkGraphicsTransform(Graphics2D graphics, int width, int height) {
		super(width, height);
		this.graphics = graphics;
	}

	public NetworkGraphicsTransform(Graphics2D graphics, NetworkTransform t) {
		super(t);
		this.graphics = graphics;
	}
	
	@Override
	public Graphics2D getGraphics() {
		return (Graphics2D) graphics.create();
	}
}
