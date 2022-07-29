package org.cytoscape.ding.impl.canvas;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.ding.impl.TransformChangeListener;

public class NetworkTransform {
	
	// width and height of viewport (JComponent) in image coordinate space
	private int width;
	private int height;
	
	// x/y is center of network in node coordinate space
	private double x = 0;
	private double y = 0;
	
	// scale factor is same as zoom level
	private double scaleFactor = 1;
	private double dpiScaleFactor = 1.0;
	
	// This transform is used to convert from window (ie mouse or image) coordinates to node coordinates.
	private final AffineTransform windowXform = new AffineTransform();
	
	// This transform is used when painting, and it incorporates the dpiScaleFactor to render at higher resolution.
	private final AffineTransform paintXform = new AffineTransform();
	
	private final Rectangle2D.Float area = new Rectangle2D.Float();
	
	private final List<TransformChangeListener> transformChangeListeners = new CopyOnWriteArrayList<>();
	
	public NetworkTransform(int width, int height) {
		this.width = width;
		this.height = height;
		updateTransform();
	}
	
	public NetworkTransform(int width, int height, double x, double y, double scaleFactor) {
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.scaleFactor = scaleFactor;
		updateTransform();
	}
	
	public NetworkTransform(NetworkTransform t) {
		this.width = t.width;
		this.height = t.height;
		this.x = t.x;
		this.y = t.y;
		this.scaleFactor = t.scaleFactor;
		this.dpiScaleFactor = t.dpiScaleFactor;
		updateTransform();
	}
	
	public Snapshot snapshot() {
		return new Snapshot(this);
	}
	
	public static class Snapshot {
		public final double x;
		public final double y;
		public final double scaleFactor;
		
		private Snapshot(NetworkTransform nt) {
			this.x = nt.x;
			this.y = nt.y;
			this.scaleFactor = nt.scaleFactor;
		}

		@Override
		public int hashCode() {
			return Objects.hash(scaleFactor, x, y);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Snapshot))
				return false;
			Snapshot other = (Snapshot) obj;
			return scaleFactor == other.scaleFactor && x == other.x && y == other.y;
		}
	}
	
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getPixelWidth() {
		return (int)(width  * dpiScaleFactor);
	}
	
	public int getPixelHeight() {
		return (int)(height * dpiScaleFactor);
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
	
	public double getDpiScaleFactor() {
		return dpiScaleFactor;
	}
	
	public void setScaleFactor(double scaleFactor) {
		if(this.scaleFactor != scaleFactor) {
			this.scaleFactor = scaleFactor;
			updateTransform();
		}
	}
	
	public void setDPIScaleFactor(double dpiScaleFactor) {
		if(this.dpiScaleFactor != dpiScaleFactor) {
			this.dpiScaleFactor = dpiScaleFactor;
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
	
	public void setScaleFactorAndCenter(double scaleFactor, double x, double y) {
		if(this.x != x || this.y != y || this.scaleFactor != scaleFactor) {
			this.x = x;
			this.y = y;
			this.scaleFactor = scaleFactor;
			updateTransform(); // Fires one event
		}
	}
	
	private void updateTransform() {
		windowXform.setToTranslation(0.5d * width, 0.5d * height);
		windowXform.scale(scaleFactor, scaleFactor);
		windowXform.translate(-x, -y);
		
		paintXform.setToTranslation(0.5d * getPixelWidth(), 0.5d * getPixelHeight());
		paintXform.scale(scaleFactor, scaleFactor);
		paintXform.scale(dpiScaleFactor, dpiScaleFactor);
		paintXform.translate(-x, -y);
		
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
	
	public AffineTransform getPaintAffineTransform() {
		return paintXform;
	}
	
	public AffineTransform getWindowAffineTransform() {
		return windowXform;
		
	}
	public final void xformImageToNodeCoords(double[] coords) {
		try {
			windowXform.inverseTransform(coords, 0, coords, 0, 1);
		} catch (java.awt.geom.NoninvertibleTransformException e) {
			throw new RuntimeException("noninvertible matrix - cannot happen");
		}
	}
	
	public final void xformNodeToImageCoords(double[] coords) {
		windowXform.transform(coords, 0, coords, 0, 1);
	}
	
	public GeneralPath pathInNodeCoords(GeneralPath path) {
		try {
			GeneralPath transformedPath = new GeneralPath(path);
			transformedPath.transform(windowXform.createInverse());
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
	
	public Point getImageCoordinates(Point2D p) {
		return getImageCoordinates(p.getX(), p.getY());
	}
	
	public float getNodeDistance(int imageUnits) {
		return (float)(imageUnits / scaleFactor);
	}
}
