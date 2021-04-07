package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;
import java.awt.Image;

public class NullGraphicsProvider implements ImageGraphicsProvider {

	public static final NullGraphicsProvider INSTANCE = new NullGraphicsProvider();
	
	@Override
	public Graphics2D getGraphics(boolean clear) {
		return null;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public NetworkTransform getTransform() {
		return null;
	}

}
