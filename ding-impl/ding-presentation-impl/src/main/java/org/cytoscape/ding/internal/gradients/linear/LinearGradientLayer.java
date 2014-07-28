package org.cytoscape.ding.internal.gradients.linear;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
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
	public void draw(Graphics2D g, final Rectangle2D area, final Shape shape) {
		final boolean squared = area.getWidth() == area.getHeight();
		
		if (angle % 90 == 0 || squared) {
			super.draw(g, area, shape);
		} else {
			double ax = area.getCenterX();
			double ay = area.getCenterY();
			final AffineTransform tx = new AffineTransform();
			tx.rotate(Math.toRadians(-angle), ax, ay);
			
			Shape txArea = tx.createTransformedShape(area);
			
	//		if (txArea.getBounds().getHeight() > area.getHeight()) {
	//			final AffineTransform tx2 = new AffineTransform();
	//			tx2.translate(txArea.getBounds().getX() - txArea.getBounds().getWidth() / 2, txArea.getBounds().getX() - txArea.getBounds().getHeight() / 2);
	//			tx2.scale(1,  area.getHeight() / txArea.getBounds().getHeight());
	//			txArea = tx2.createTransformedShape(txArea);
	//		}
			
			g.setColor(Color.blue);
			g.draw(txArea);
			
			g.transform(tx);
			
			try {
				tx.invert();
			} catch (NoninvertibleTransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			super.draw(g, area, tx.createTransformedShape(shape));
			g.transform(tx);
			
			
	//		final PathIterator pi = line.getPathIterator(at);
			
	//		g.setColor(Color.blue);
	//		g.drawOval((int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight());
	//		
			Line2D line = getGradientAxis(area, angle);
			
			g.setColor(Color.red);
			g.drawOval((int)line.getX1(), (int)line.getY1(), 1, 1);
			g.setColor(Color.green);
			g.drawOval((int)line.getX2(), (int)line.getY2(), 1, 1);
		}
	}
	
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
		final boolean squared = bounds.getWidth() == bounds.getHeight();
		
		if (angle == 0.0) {
			// Just because it's easy, fast and more precise to do it like this
			start = new Point2D.Double(bounds.getX(), bounds.getCenterY());
			end = new Point2D.Double(bounds.getMaxX(), bounds.getCenterY());
		} else if (angle == 90.0) {
			start = new Point2D.Double(bounds.getCenterX(), bounds.getMaxY());
			end = new Point2D.Double(bounds.getCenterX(), bounds.getY());
		} else if (angle == 180.0) {
			start = new Point2D.Double(bounds.getMaxX(), bounds.getCenterY());
			end = new Point2D.Double(bounds.getX(), bounds.getCenterY());
		} else if (angle == 270.0) {
			start = new Point2D.Double(bounds.getCenterX(), bounds.getY());
			end = new Point2D.Double(bounds.getCenterX(), bounds.getMaxY());
		} else if (squared) {
			if (angle == 45.0) {
				start = new Point2D.Double(bounds.getX(), bounds.getMaxY());
				end = new Point2D.Double(bounds.getMaxX(), bounds.getY());
			} else if (angle == 135.0) {
				start = new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
				end = new Point2D.Double(bounds.getX(), bounds.getY());
			} else if (angle == 225.0) {
				start = new Point2D.Double(bounds.getMaxX(), bounds.getY());
				end = new Point2D.Double(bounds.getX(), bounds.getMaxY());
			} else if (angle == 315.0) {
				start = new Point2D.Double(bounds.getX(), bounds.getY());
				end = new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
			} else {
				// TODO Not perfect...
				final double size = Math.max(bounds.getWidth(), bounds.getHeight());
				start = new Point2D.Double(bounds.getX() - size, bounds.getCenterY());
				end = new Point2D.Double(bounds.getMaxX() + size, bounds.getCenterY());
				
				final Line2D line = adjustPoints(start, end, angle, bounds);
				start = line.getP1();
				end = line.getP2();
			}
		} else {
			// TODO only works between 46-89 degrees!!!
			double a = Math.abs(angle % 45);
			
//			if (a < 90.0 && a > 0.0) {
				double w = bounds.getWidth();
				double h = bounds.getHeight();
				double f = MathUtil.invLinearInterp(a, 45.0, 0.0);
				double iw = MathUtil.linearInterp(f, h, w);
				double dw = w - iw;
				start = new Point2D.Double(bounds.getX() + dw/2, bounds.getCenterY());
				end = new Point2D.Double(bounds.getMaxX() - dw/2, bounds.getCenterY());
//			}
		}
		
		// TODO: Using ellipse formula: Not so good--change rendered angle
//		double rad1 = Math.toRadians(-180 - angle);
//		double rad2 = Math.toRadians(-360 - angle);
//		
//		double w2 = bounds.getWidth() / 2;
//		double h2 = bounds.getHeight() / 2;
//		double x1 = bounds.getX() + w2 + w2 * Math.cos(rad1);
//		double y1 = bounds.getY() + h2 + h2 * Math.sin(rad1);
//		double x2 = bounds.getX() + w2 + w2 * Math.cos(rad2);
//		double y2 = bounds.getY() + h2 + h2 * Math.sin(rad2);
//		start = new Point2D.Double(x1, y1);
//		end = new Point2D.Double(x2, y2);
		
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
