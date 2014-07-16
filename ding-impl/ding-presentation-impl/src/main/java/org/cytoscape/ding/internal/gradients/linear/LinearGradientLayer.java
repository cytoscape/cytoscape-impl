package org.cytoscape.ding.internal.gradients.linear;

import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.gradients.AbstractGradientLayer;
import org.cytoscape.ding.internal.util.MathUtil;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class LinearGradientLayer extends AbstractGradientLayer {
	
	protected double angle;
	protected Rectangle2D rectangle;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LinearGradientLayer(final double angle,
							   final List<ControlPoint> controlPoints) {
		super(controlPoints);
		this.angle = MathUtil.normalizeAngle(angle);
		rectangle = new Rectangle(0, 0, 100, 100);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public Paint getPaint(final Rectangle2D bounds) {
		final Line2D line = getGradientAxis(bounds, angle);
		paint = new LinearGradientPaint(line.getP1(), line.getP2(), positions, colors);
		
		return paint;
	}

	@Override
	public Rectangle2D getBounds2D() {
		return rectangle;
	}

	@Override
	public CustomGraphicLayer transform(final AffineTransform xform) {
		final LinearGradientLayer newLayer = new LinearGradientLayer(angle, controlPoints);
		newLayer.rectangle = xform.createTransformedShape(rectangle) .getBounds2D();
		
		return newLayer;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private static Line2D getGradientAxis(final Rectangle2D bounds, final double angle) {
		Point2D start = null;
		Point2D end = null;
		
		if (angle == 0.0) {
			// Just because it's easy, fast and more precise to do it like this
			start = new Point2D.Double(bounds.getX(), bounds.getCenterY());
			end = new Point2D.Double(bounds.getMaxX(), bounds.getCenterY());
		} else if (angle == 45.0) {
			start = new Point2D.Double(bounds.getX(), bounds.getMaxY());
			end = new Point2D.Double(bounds.getMaxX(), bounds.getY());
		} else if (angle == 90.0) {
			start = new Point2D.Double(bounds.getCenterX(), bounds.getMaxY());
			end = new Point2D.Double(bounds.getCenterX(), bounds.getY());
		} else if (angle == 135.0) {
			start = new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
			end = new Point2D.Double(bounds.getX(), bounds.getY());
		} else if (angle == 180.0) {
			start = new Point2D.Double(bounds.getMaxX(), bounds.getCenterY());
			end = new Point2D.Double(bounds.getX(), bounds.getCenterY());
		} else if (angle == 225.0) {
			start = new Point2D.Double(bounds.getMaxX(), bounds.getY());
			end = new Point2D.Double(bounds.getX(), bounds.getMaxY());
		} else if (angle == 270.0) {
			start = new Point2D.Double(bounds.getCenterX(), bounds.getY());
			end = new Point2D.Double(bounds.getCenterX(), bounds.getMaxY());
		} else if (angle == 315.0) {
			start = new Point2D.Double(bounds.getX(), bounds.getY());
			end = new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
		} else {
			// TODO The other angles need more work
			final double size = Math.max(bounds.getWidth(), bounds.getHeight());
			start = new Point2D.Double(bounds.getX() - size, bounds.getCenterY());
			end = new Point2D.Double(bounds.getMaxX() + size, bounds.getCenterY());
			
			final Line2D line = adjustPoints(start, end, angle, bounds);
			start = line.getP1();
			end = line.getP2();
		}
		
		return new Line2D.Double(start, end);
	}
	
	private static Line2D adjustPoints(Point2D start, Point2D end, double angle, final Rectangle2D bounds) {
		// Rotate the gradient line
		final double ax = start.getX() + (end.getX() - start.getX()) / 2;
		final double ay = start.getY() + (end.getY() - start.getY()) / 2;
		
		final AffineTransform at = new AffineTransform();
		at.rotate(Math.toRadians(-angle), ax, ay);
		final Line2D line = new Line2D.Double(start, end);
		final PathIterator pi = line.getPathIterator(at);
		int count = 0;
		
		while (!pi.isDone() && count < 2) {
			final double[] coords = new double[6];
			pi.currentSegment(coords);
			
			if (count == 0)
				start = new Point2D.Double(coords[0], coords[1]);
			else
				end = new Point2D.Double(coords[0], coords[1]);
			
			pi.next();
			count++;
		}
		
		// Check intersection with the node bounds to adjust start/end points
		Point2D ip1 = null;
		Point2D ip2 = null;
		
		final Point2D[] points = MathUtil.getIntersectionPoints(new Line2D.Double(start, end), bounds);
		
		if (points != null) {
			Point2D newStart = null, newEnd = null;
			
			for (final Point2D p : points) {
				if (p != null) {
					if (ip1 == null)
						ip1 = p;
					else
						ip2 = p;
				}
			}
			
			if (ip1 != null) {
				double d1 = ip1.distance(start);
				double d2 = ip1.distance(end);
				
				if (d1 < d2)
					newStart = ip1;
				else
					newEnd = ip1;
			}
			
			if (ip2 != null) {
				double d1 = ip2.distance(start);
				double d2 = ip2.distance(end);
				
				if (d1 < d2)
					newStart = ip2;
				else
					newEnd = ip2;
			}
			
			if (newStart != null)
				start = newStart;
			if (newEnd != null)
				end = newEnd;
		}
		
		return new Line2D.Double(start, end);
	}
}
