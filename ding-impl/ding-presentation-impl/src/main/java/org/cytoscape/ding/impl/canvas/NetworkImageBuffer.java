package org.cytoscape.ding.impl.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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
		if(transform.getWidth() != image.getWidth(null) || transform.getHeight() != image.getHeight(null)) {
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
			image  = new BufferedImage(transform.getWidth(), transform.getHeight(), BufferedImage.TYPE_INT_ARGB);
		}
		return image;
	}
	
	
	public synchronized void bufferTransform(NetworkTransform.Snapshot ts, ImageGraphicsProvider slowEdgeCanvas) {
		if(image == null)
			return;
		
		clear((Graphics2D)image.getGraphics());
		
		// MKTODO this will only work on pan, how to make zoom work also????
		double[] coords = new double[2];
		
		coords[0] = ts.x;
		coords[1] = ts.y;
		transform.xformNodeToImageCoords(coords);
		double oldX = coords[0];
		double oldY = coords[1];
		
		coords[0] = transform.getCenterX();
		coords[1] = transform.getCenterY();
		transform.xformNodeToImageCoords(coords);
		double newX = coords[0];
		double newY = coords[1];
		
		var dx = oldX - newX;
		var dy = oldY - newY;
		
		AffineTransform t = new AffineTransform();
		t.translate(dx, dy);
		AffineTransformOp op = new AffineTransformOp(t, AffineTransformOp.TYPE_BILINEAR);
		op.filter((BufferedImage)slowEdgeCanvas.getImage(), image);
	}
	
	
	/*
	 * Returns the Graphics2D object directly from the image buffer, the AffineTransform has not 
	 * been applied yet. To draw in node coordinates make sure to call
	 * g.setTransform(networkImageBuffer.getAffineTransform()).
	 */
	public Graphics2D getGraphics() {
		image = getImage();
		var g = (Graphics2D) image.getGraphics();
		clear(g);
		return g;
	}
	
	private void clear(Graphics2D g) {
		g.setBackground(TRANSPARENT_COLOR);
		g.clearRect(0, 0, image.getWidth(null), image.getHeight(null));
	}

}
