package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.ding.impl.TransformChangeListener;

public abstract class NetworkTransform {
	
	private int width;
	private int height;
	
	private double x = 0;
	private double y = 0;
	private double scaleFactor = 1;
	
	private final AffineTransform xform = new AffineTransform();
	private final Rectangle2D.Float area = new Rectangle2D.Float();
	
	private final List<TransformChangeListener> transformChangeListeners = new CopyOnWriteArrayList<>();
	
	public NetworkTransform(int width, int height) {
		this.width = width;
		this.height = height;
		updateTransform();
	}
	
	public NetworkTransform(NetworkTransform t) {
		this.width = t.width;
		this.height = t.height;
		this.x = t.x;
		this.y = t.y;
		this.scaleFactor = t.scaleFactor;
		updateTransform();
	}
	
	public abstract Graphics2D getGraphics();
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public double getCenterX() {
		return x;
	}
	
	public double getCenterY() {
		return y;
	}
	
	public Point2D getCenter() {
		return new Point2D.Double(x, y);
	}
	
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
		}
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
		
		fireTransformChanged();
	}

	
	public void addTransformChangeListener(TransformChangeListener l) {
		transformChangeListeners.add(l);
	}
	
	public void removeTransformChangeListener(TransformChangeListener l) {
		transformChangeListeners.remove(l);
	}
	
	private void fireTransformChanged() {
		for(var l : transformChangeListeners) {
			l.transformChanged();
		}
	}
	
	public Rectangle2D.Float getNetworkVisibleAreaNodeCoords() {
		return area;
	}
	
	public AffineTransform getAffineTransform() {
		return xform;
	}
	
	public final void xformImageToNodeCoords(double[] coords) {
		try {
			xform.inverseTransform(coords, 0, coords, 0, 1);
		} catch (java.awt.geom.NoninvertibleTransformException e) {
			throw new RuntimeException("noninvertible matrix - cannot happen");
		}
	}
	
	public final void xformNodeToImageCoords(double[] coords) {
		xform.transform(coords, 0, coords, 0, 1);
	}
	
	public GeneralPath pathInNodeCoords(GeneralPath path) {
		try {
			GeneralPath transformedPath = new GeneralPath(path);
			transformedPath.transform(xform.createInverse());
			return transformedPath;
		} catch (NoninvertibleTransformException e) {
			return null;
		}
	}	
	
	public Rectangle2D getNodeCoordinates(Rectangle2D bounds) {
		double x1 = bounds.getX();
		double y1 = bounds.getY();
		double[] p1 = {x1, y1};
		xformImageToNodeCoords(p1);

		double x2 = bounds.getX()+bounds.getWidth();
		double y2 = bounds.getY()+bounds.getHeight();
		double[] p2 = {x2, y2};
		xformImageToNodeCoords(p2);

		return new Rectangle2D.Double(p1[0], p1[1], p2[0]-p1[0], p2[1]-p1[1]);
	}

	public Rectangle getImageCoordinates(Rectangle2D bounds) {
		double x1 = bounds.getX();
		double y1 = bounds.getY();
		double[] p1 = {x1, y1};
		xformNodeToImageCoords(p1);

		double x2 = bounds.getX()+bounds.getWidth();
		double y2 = bounds.getY()+bounds.getHeight();
		double[] p2 = {x2, y2};
		xformNodeToImageCoords(p2);

		return new Rectangle((int)p1[0], (int)p1[1], (int)(p2[0]-p1[0]), (int)(p2[1]-p1[1]));
	}

	public Point2D getNodeCoordinates(double x, double y) {
		double[] p1 = {x, y};
		xformImageToNodeCoords(p1);
		return new Point2D.Double(p1[0], p1[1]);
	}
	
	public Point2D getNodeCoordinates(Point2D p) {
		return getNodeCoordinates(p.getX(), p.getY());
	}

	public Point getImageCoordinates(double x, double y) {
		double[] p1 = {x, y};
		xformNodeToImageCoords(p1);
		return new Point((int)p1[0], (int)p1[1]);
	}
}
