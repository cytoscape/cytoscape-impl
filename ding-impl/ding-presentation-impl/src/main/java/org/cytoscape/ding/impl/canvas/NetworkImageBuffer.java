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
public class NetworkImageBuffer implements ImageGraphicsProvider {
	
	private static final Color TRANSPARENT_COLOR = new Color(0,0,0,0);
	
	private NetworkTransform transform;
	private boolean enabled = true;
	private Image image;
	
	public NetworkImageBuffer(NetworkTransform transform) {
		this.transform = transform;
		transform.addTransformChangeListener(this::updateImage);
		updateImage();
	}
	
	private void updateImage() {
		if(!enabled) {
			this.image = null;
			return;
		}
		
		if(image == null || (transform.getWidth() != image.getWidth(null) || transform.getHeight() != image.getHeight(null))) {
			this.image = new BufferedImage(transform.getWidth(), transform.getHeight(), BufferedImage.TYPE_INT_ARGB);
		}
	}
	
	@Override
	public NetworkTransform getTransform() {
		return transform;
	}
	
	@Override
	public Image getImage() {
		return image;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if(this.enabled == enabled)
			return;
		
		this.enabled = enabled;
		
		if(enabled)
			updateImage();
		else
			this.image = null;
	}
	/**
	 * Returns the Graphics2D object directly from the image buffer, the AffineTransform has not 
	 * been applied yet. To draw in node coordinates make sure to call
	 * g.setTransform(networkImageBuffer.getAffineTransform()).
	 */
	public Graphics2D getGraphics() {
		if(!enabled || image == null)
			return null;
		var g = (Graphics2D) image.getGraphics();
		clear(g);
		return g;
	}
	
	private void clear(Graphics2D g) {
		g.setBackground(TRANSPARENT_COLOR);
		g.clearRect(0, 0, image.getWidth(null), image.getHeight(null));
	}

}
