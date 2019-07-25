package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;


/**
 * An image buffer that also stores the transform between the image coordinates and the network coordinats.
 *
 */
public class NetworkImageBuffer implements NetworkTransform {
	
	private static final Color TRANSPARENT_COLOR = new Color(0,0,0,0);
	
	private int width;
	private int height;
	
	private double x = 0;
	private double y = 0;
	private double scaleFactor = 1;
	
	private Image image;
	
	private final AffineTransform xform = new AffineTransform();
	private final Rectangle2D.Float area = new Rectangle2D.Float();
	
	
	public NetworkImageBuffer() {
		this(1, 1);
	}
	
	public NetworkImageBuffer(int width, int height) {
		this.width = width;
		this.height = height;
		updateTransform();
		updateImage();
	}
	
	private void updateTransform() {
		xform.setToTranslation(0.5d * width, 0.5d * height);
		xform.scale(scaleFactor, scaleFactor);
		xform.translate(-x, -y);
		
		// Define the visible window in node coordinate space.
		float xMin = (float) (x - ((0.5d * width)  / scaleFactor));
		float yMin = (float) (y - ((0.5d * height) / scaleFactor));
		float xMax = (float) (x + ((0.5d * width)  / scaleFactor)); 
		float yMax = (float) (y + ((0.5d * height) / scaleFactor));
		area.setRect(xMin, yMin, xMax - xMin, yMax - yMin);
	}
	
	private void updateImage() {
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	
	public Image getImage() {
		return image;
	}
	
	/**
	 * Returns the Graphics2D object directly from the image buffer, the AffineTransform has not 
	 * been applied yet. To draw in node coordinates make sure to call
	 * g.setTransform(networkImageBuffer.getAffineTransform()).
	 */
	public Graphics2D getGraphics() {
		return (Graphics2D) image.getGraphics();
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public double getCenterX() {
		return x;
	}
	
	@Override
	public double getCenterY() {
		return y;
	}
	
	@Override
	public double getScaleFactor() {
		return scaleFactor;
	}
	
	public void setScaleFactor(double scaleFactor) {
		if(this.scaleFactor != scaleFactor) {
			this.scaleFactor = scaleFactor;
			updateTransform();
		}
	}
	
	public void setCenter(double x, double y) {
		if(this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			updateTransform();
		}
	}
	
	public void setViewport(int width, int height) {
		if(this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			updateTransform();
			updateImage();
		}
	}
	
	@Override
	public Rectangle2D.Float getNetworkVisibleAreaNodeCoords() {
		return area;
	}
	
	@Override
	public AffineTransform getAffineTransform() {
		return xform;
	}
	
	@Override
	public final void xformImageToNodeCoords(double[] coords) {
		try {
			xform.inverseTransform(coords, 0, coords, 0, 1);
		} catch (java.awt.geom.NoninvertibleTransformException e) {
			throw new RuntimeException("noninvertible matrix - cannot happen");
		}
	}
	
	@Override
	public final void xformNodeToImageCoords(double[] coords) {
		xform.transform(coords, 0, coords, 0, 1);
	}
	
	@Override
	public GeneralPath pathInNodeCoords(GeneralPath path) {
		try {
			GeneralPath transformedPath = new GeneralPath(path);
			transformedPath.transform(xform.createInverse());
			return transformedPath;
		} catch (NoninvertibleTransformException e) {
			return null;
		}
	}
	
	public void fill(Color color) {
		Graphics2D g = getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, width, height);
	}

	public void clear() {
		Graphics2D g = getGraphics();
		g.setBackground(TRANSPARENT_COLOR);
		g.clearRect(0, 0, width, height);
	}
}
