package org.cytoscape.ding.impl;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface NetworkTransform {
	
	
	public AffineTransform getAffineTransform();
	
	public void xformImageToNodeCoords(double[] coords);
	
	public void xformNodeToImageCoords(double[] coords);
	
	public GeneralPath pathInNodeCoords(GeneralPath path);

	public Rectangle2D.Float getNetworkVisibleAreaInNodeCoords();
	

	default public Rectangle2D getNodeCoordinates(Rectangle2D bounds) {
		double x1 = bounds.getX();
		double y1 = bounds.getY();
		double x2 = bounds.getX()+bounds.getWidth();
		double y2 = bounds.getY()+bounds.getHeight();
		
		double[] nextLocn1 = {x1, y1};
		xformImageToNodeCoords(nextLocn1);

		double[] nextLocn2 = {x2, y2};
		xformImageToNodeCoords(nextLocn2);

		return new Rectangle2D.Double(nextLocn1[0], nextLocn1[1], nextLocn2[0]-nextLocn1[0], nextLocn2[1]-nextLocn1[1]);
	}

	default public Rectangle2D getImageCoordinates(Rectangle2D bounds) {
		double x1 = bounds.getX();
		double y1 = bounds.getY();
		double x2 = bounds.getX()+bounds.getWidth();
		double y2 = bounds.getY()+bounds.getHeight();
		
		double[] nextLocn1 = {x1, y1};
		xformNodeToImageCoords(nextLocn1);

		double[] nextLocn2 = {x2, y2};
		xformNodeToImageCoords(nextLocn2);

		return new Rectangle2D.Double(nextLocn1[0], nextLocn1[1], nextLocn2[0]-nextLocn1[0], nextLocn2[1]-nextLocn1[1]);
	}

	default public Point2D getNodeCoordinates(int x, int y) {
		double[] nextLocn = {x, y};
		xformImageToNodeCoords(nextLocn);
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}
	
	default public Point2D getNodeCoordinates(Point2D p) {
		return getNodeCoordinates((int)p.getX(), (int)p.getY());
	}

	default public Point2D getImageCoordinates(double x, double y) {
		double[] nextLocn = {x, y};
		xformNodeToImageCoords(nextLocn);
		return new Point2D.Double(nextLocn[0], nextLocn[1]);
	}
}
