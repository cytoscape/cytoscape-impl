package org.cytoscape.ding.internal.gradients.radial;

import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.gradients.GradientLayer;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class RadialGradientLayer extends GradientLayer {
	
	protected Point2D center;
	protected float radius;
	protected Rectangle2D rectangle;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RadialGradientLayer(final Point2D center,
							   final float radius,
							   final List<ControlPoint> controlPoints) {
		super(controlPoints);
		this.center = center;
		this.radius = radius;
		
		if (this.center == null)
			this.center = new Point2D.Float(0.5f, 0.5f);
		if (this.radius == 0.0f)
			this.radius = 1.0f;

		rectangle = new Rectangle(0, 0, 1, 1);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public Paint getPaint(final Rectangle2D bounds) {
		// Assuming radius and center are of a unit circle, scale appropriately
		double xCenter = bounds.getWidth() * center.getX() + bounds.getX();
		double yCenter = bounds.getHeight() * center.getY() + bounds.getY();
		final Point2D newCenter = new Point2D.Float((float)xCenter, (float)yCenter);
		final double newRadius = radius * Math.min(bounds.getWidth(), bounds.getHeight());
		
		paint = new RadialGradientPaint(newCenter, (float)newRadius, positions, colors);
		
		return paint;
	}

	@Override
	public Rectangle2D getBounds2D() {
		return rectangle;
	}

	@Override
	public CustomGraphicLayer transform(final AffineTransform xform) {
		final RadialGradientLayer newLayer = new RadialGradientLayer(center, radius, controlPoints);
		newLayer.rectangle = xform.createTransformedShape(rectangle).getBounds2D();
		
		return newLayer;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
}
