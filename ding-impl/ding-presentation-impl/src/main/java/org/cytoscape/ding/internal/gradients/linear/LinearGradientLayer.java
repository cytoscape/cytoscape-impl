package org.cytoscape.ding.internal.gradients.linear;

import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.gradients.GradientLayer;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class LinearGradientLayer extends GradientLayer {
	
	protected double angle;
	protected Rectangle2D rectangle;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LinearGradientLayer(final double angle,
							   final List<ControlPoint> controlPoints) {
		super(controlPoints);
		this.angle = angle;
		rectangle = new Rectangle(0, 0, 100, 100);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public Paint getPaint(final Rectangle2D bounds) {
		double r = Math.max(bounds.getWidth(), bounds.getHeight()) / 2;
		double x1 = bounds.getX() + r * Math.cos(angle);
		double y1 = bounds.getY() + r * Math.sin(angle);
		double x2 = bounds.getX() + r * Math.cos(angle + 180);
		double y2 = bounds.getY() + r * Math.sin(angle + 180);
		final Point2D start = new Point2D.Double(x1, y1);
		final Point2D end = new Point2D.Double(x2, y2);
		System.out.println(start + " :: " + end);
		
		this.paint = new LinearGradientPaint(start, end, positions, colors);
		
		return this.paint;
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
}
