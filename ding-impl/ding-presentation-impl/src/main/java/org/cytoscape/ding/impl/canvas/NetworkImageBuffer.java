package org.cytoscape.ding.impl.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


/**
 * A NetworkTransform that allows painting onto an image buffer.
 * This allows Canvases to paint in parallel because each canvas can have a separate
 * image buffer.
 */
public class NetworkImageBuffer implements ImageGraphicsProvider {
	
	private static final Color TRANSPARENT_COLOR = new Color(0,0,0,0);
	
	private NetworkTransform transform;
	private BufferedImage image;
	
	public NetworkImageBuffer(NetworkTransform transform) {
		this.transform = transform;
		transform.addTransformChangeListener(this::updateImage);
		updateImage();
	}
	
	private synchronized void updateImage() {
		if(image == null) {
			return;
		}
		if(transform.getPixelWidth() != image.getWidth(null) || transform.getPixelHeight() != image.getHeight(null)) {
			this.image = null;
		}
	}
	
	@Override
	public NetworkTransform getTransform() {
		return transform;
	}
	
	@Override
	public synchronized BufferedImage getImage() {
		if(image == null) {
			image  = new BufferedImage(transform.getPixelWidth(), transform.getPixelHeight(), BufferedImage.TYPE_INT_ARGB);
		}
		return image;
	}
	
	
	/*
	 * Returns the Graphics2D object directly from the image buffer, the AffineTransform has not 
	 * been applied yet. To draw in node coordinates make sure to call
	 * g.setTransform(networkImageBuffer.getAffineTransform()).
	 */
	public Graphics2D getGraphics(boolean clear) {
		image = getImage();
		var g = (Graphics2D) image.getGraphics();
		if(clear)
			clear(g);
		return g;
	}
	
	public void clear(Graphics2D g) {
		g.setBackground(TRANSPARENT_COLOR);
		g.clearRect(0, 0, image.getWidth(null), image.getHeight(null));
	}

}
