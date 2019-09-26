package org.cytoscape.ding.impl.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;


/**
 * A NetworkTransform that allows painting onto an image buffer.
 * This allows Canvases to paint in parallel because each canvas can have a separate
 * image buffer.
 */
public class NetworkImageBuffer extends NetworkTransform {
	
	private static final Color TRANSPARENT_COLOR = new Color(0,0,0,0);
	
	private Image image;
	
	public NetworkImageBuffer(int width, int height) {
		super(width, height);
		updateImage();
	}
	
	public NetworkImageBuffer(NetworkTransform t) {
		super(t);
		updateImage();
	}
	
	private void updateImage() {
		this.image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
	}
	
	public Image getImage() {
		return image;
	}
	
	@Override
	public void setViewport(int width, int height) {
		if(getWidth() != width || getHeight() != height) {
			super.setViewport(width, height);
			updateImage();
		}
	}
	
	/**
	 * Returns the Graphics2D object directly from the image buffer, the AffineTransform has not 
	 * been applied yet. To draw in node coordinates make sure to call
	 * g.setTransform(networkImageBuffer.getAffineTransform()).
	 */
	public Graphics2D getGraphics() {
		var g = (Graphics2D) image.getGraphics();
		clear(g);
		return g;
	}
	
	private void clear(Graphics2D g) {
		g.setBackground(TRANSPARENT_COLOR);
		g.clearRect(0, 0, getWidth(), getHeight());
	}
}
