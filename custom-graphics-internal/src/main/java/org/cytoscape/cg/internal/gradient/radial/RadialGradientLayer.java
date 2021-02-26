package org.cytoscape.cg.internal.gradient.radial;

import java.awt.Color;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.cg.internal.gradient.AbstractGradientLayer;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class RadialGradientLayer extends AbstractGradientLayer {
	
	protected Point2D center;
	protected float radius;
	protected Rectangle2D rectangle = new Rectangle(0, 0, 100, 100);

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RadialGradientLayer(final Point2D center,
			   				   final float radius,
			   				   final List<Float> fractions,
			   				   final List<Color> colors) {
		super(fractions, colors);
		this.center = center;
		this.radius = radius;
		
		if (this.center == null)
			this.center = new Point2D.Float(0.5f, 0.5f);
		
		if (this.radius <= 0.0f)
			this.radius = 1.0f; // 100%
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public Paint getPaint(final Rectangle2D bounds) {
		// Assuming radius and center are of a unit circle, scale appropriately
		double w = bounds.getWidth();
		double h = bounds.getHeight();
		double cx = w * center.getX() + bounds.getX();
		double cy = h * center.getY() + bounds.getY();
		final Point2D newCenter = new Point2D.Double(cx, cy);
		
		double delta = newCenter.distance(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()));
		final double r =  Math.sqrt(w*w + h*h) / 2;
		final double newRadius = delta + r * radius;
		
		paint = new RadialGradientPaint(newCenter, (float)newRadius, fractionArray, colorArray);
		
		return paint;
	}

	@Override
	public Rectangle2D getBounds2D() {
		return rectangle;
	}

	@Override
	public CustomGraphicLayer transform(final AffineTransform xform) {
		final RadialGradientLayer newLayer = new RadialGradientLayer(center, radius, fractions, colors);
		newLayer.rectangle = xform.createTransformedShape(rectangle).getBounds2D();
		
		return newLayer;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
}
