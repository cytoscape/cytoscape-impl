package org.cytoscape.ding.internal.gradients.linear;

import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.ding.internal.gradients.AbstractGradientLayer;
import org.cytoscape.ding.internal.gradients.ControlPoint;
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
	
//	/**
//	 * Used to visually test it.
//	 */
//	@Override
//	public void draw(Graphics2D g, final Rectangle2D area, final Shape shape) {
//		super.draw(g, area, shape);
//		
//		double ax;
//		double ay;
//		g.setStroke(new BasicStroke(0.5f));
//		
//		double d = Math.max(area.getWidth(), area.getHeight());
//		
//		// Gradient line when angle is 0 degree
//		Line2D gl = new Line2D.Double(area.getCenterX() - d, area.getCenterY(), area.getCenterX() + d, area.getCenterY());
//		gl = MathUtil.rotate(gl, -angle, area.getCenterX(), area.getCenterY());
//		
//		g.setColor(Color.blue);
//		g.draw(gl);
//		
//		int q = MathUtil.getQuadrant(angle);
//		
//		// Creates two perpendicular lines (to the gradient line)
//		Line2D pl1 = new Line2D.Double(area.getMinX(), area.getCenterY() - d, area.getMinX(), area.getCenterY() + d);
//		ax = area.getMinX();
//		ay = q == 1 || q == 3 ? area.getMaxY() : area.getMinY();
//		pl1 = MathUtil.rotate(pl1, -angle, ax, ay);
//		
//		g.setColor(Color.green);
//		g.draw(pl1);
//		
//		Line2D pl2 = new Line2D.Double(area.getMaxX(), area.getCenterY() - d, area.getMaxX(), area.getCenterY() + d);
//		ax = area.getMaxX();
//		ay = q == 1 || q == 3 ? area.getMinY() : area.getMaxY();
//		pl2 = MathUtil.rotate(pl2, -angle, ax, ay);
//		
//		g.setColor(Color.red);
//		g.draw(pl2);
//	}
	
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
		
		double x1 = bounds.getMinX();
		double y1 = bounds.getMinY();
		double x2 = bounds.getMaxX();
		double y2 = bounds.getMaxY();
		double cx = bounds.getCenterX();
		double cy = bounds.getCenterY();
		
		if (angle == 0.0) {
			// Just because it's faster to calculate it like this with these angles
			start = new Point2D.Double(x1, cy);
			end = new Point2D.Double(x2, cy);
		} else if (angle == 90.0) {
			start = new Point2D.Double(cx, y2);
			end = new Point2D.Double(cx, y1);
		} else if (angle == 180.0) {
			start = new Point2D.Double(x2, cy);
			end = new Point2D.Double(x1, cy);
		} else if (angle == 270.0) {
			start = new Point2D.Double(cx, y1);
			end = new Point2D.Double(cx, y2);
		} else {
			// To understand what it does here, see:
			//   https://developer.mozilla.org/en-US/docs/Web/CSS/linear-gradient
			//   http://hugogiraudel.com/blog/css-gradients
			//   http://codepen.io/thebabydino/pen/qgoBL
			
			double ax;
			double ay;
			double d = Math.max(bounds.getWidth(), bounds.getHeight());
			
			// Gradient line when angle is 0 degree and the rotate it
			Line2D gl = new Line2D.Double(cx - d, cy, cx + d, cy);
			gl = MathUtil.rotate(gl, -angle, cx, cy);
			
			int q = MathUtil.getQuadrant(angle);
			
			// Creates two perpendicular lines (to the gradient line) and rotate them accordingly
			Line2D pl1 = new Line2D.Double(x1, cy - d, x1, cy + d);
			ax = x1;
			ay = q == 1 || q == 3 ? y2 : y1;
			pl1 = MathUtil.rotate(pl1, -angle, ax, ay);
			
			Line2D pl2 = new Line2D.Double(x2, cy - d, x2, cy + d);
			ax = x2;
			ay = q == 1 || q == 3 ? y1 : y2;
			pl2 = MathUtil.rotate(pl2, -angle, ax, ay);
			
			// Find the intersection points between the gradient line and the perpendicular lines
			start = MathUtil.getIntersectionPoint(gl, (q == 1 || q == 4 ? pl1 : pl2));
			end = MathUtil.getIntersectionPoint(gl, (q == 1 || q == 4 ? pl2 : pl1));
		}
		
		return new Line2D.Double(start, end);
	}
}
