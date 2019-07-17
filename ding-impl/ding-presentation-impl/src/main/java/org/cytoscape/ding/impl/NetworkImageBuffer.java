package org.cytoscape.ding.impl;

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
	
	public Graphics2D getGraphics() {
		return (Graphics2D) image.getGraphics();
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getScaleFactor() {
		return scaleFactor;
	}
	
	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
		updateTransform();
	}
	
	public void setCenter(double x, double y)	 {
		this.x = x;
		this.y = y;
		updateTransform();
	}
	
	public void setViewport(int width, int height) {
		this.width = width;
		this.height = height;
		updateTransform();
		updateImage();
	}
	
	@Override
	public Rectangle2D.Float getNetworkVisibleArea() {
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

	@Override
	public String toString() {
		return "NetworkImageBuffer [width=" + width + ", height=" + height + ", x=" + x + ", y=" + y + ", scaleFactor="
				+ scaleFactor + ", xform=" + xform + ", area=" + area + "]";
	}
	
	
	
}
