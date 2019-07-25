package org.cytoscape.ding.impl;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface NetworkTransform {
	
	// node coordinates
	double getScaleFactor();
	
	double getCenterX();
	
	double getCenterY();
	
	default Point2D getCenter() {
		return new Point2D.Double(getCenterX(), getCenterY());
	}
	
	
	// window (image) coordinates
	int getWidth();
	
	int getHeight();
	
	
	// methods to transform between window (image) and node coordinates
	AffineTransform getAffineTransform();
	
	void xformImageToNodeCoords(double[] coords);
	
	void xformNodeToImageCoords(double[] coords);
	
	GeneralPath pathInNodeCoords(GeneralPath path);

	Rectangle2D.Float getNetworkVisibleAreaNodeCoords();
	
	default Rectangle2D getNodeCoordinates(Rectangle2D bounds) {
		double x1 = bounds.getX();
		double y1 = bounds.getY();
		double x2 = bounds.getX()+bounds.getWidth();
		double y2 = bounds.getY()+bounds.getHeight();
		
		double[] p1 = {x1, y1};
		xformImageToNodeCoords(p1);

		double[] p2 = {x2, y2};
		xformImageToNodeCoords(p2);

		return new Rectangle2D.Double(p1[0], p1[1], p2[0]-p1[0], p2[1]-p1[1]);
	}

	default Rectangle2D getImageCoordinates(Rectangle2D bounds) {
		double x1 = bounds.getX();
		double y1 = bounds.getY();
		double x2 = bounds.getX()+bounds.getWidth();
		double y2 = bounds.getY()+bounds.getHeight();
		
		double[] p1 = {x1, y1};
		xformNodeToImageCoords(p1);

		double[] p2 = {x2, y2};
		xformNodeToImageCoords(p2);

		return new Rectangle2D.Double(p1[0], p1[1], p2[0]-p1[0], p2[1]-p1[1]);
	}

	default Point2D getNodeCoordinates(int x, int y) {
		double[] p1 = {x, y};
		xformImageToNodeCoords(p1);
		return new Point2D.Double(p1[0], p1[1]);
	}
	
	default Point2D getNodeCoordinates(Point2D p) {
		return getNodeCoordinates((int)p.getX(), (int)p.getY());
	}

	default Point2D getImageCoordinates(double x, double y) {
		double[] p1 = {x, y};
		xformNodeToImageCoords(p1);
		return new Point2D.Double(p1[0], p1[1]);
	}
}
