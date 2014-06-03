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
	
	protected Point2D start;
	protected Point2D end;
	protected Rectangle2D rectangle;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LinearGradientLayer(final Point2D start,
							   final Point2D end,
							   final List<ControlPoint> controlPoints) {
		super(controlPoints);
		this.start = start;
		this.end = end;
		
		if (this.start == null)
			this.start = new Point2D.Float(0f, 0f);
		if (this.end == null)
			this.end = new Point2D.Float(1f, 0f);

		rectangle = new Rectangle(0, 0, 100, 100);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public Paint getPaint(final Rectangle2D bounds) {
		this.paint = new LinearGradientPaint(scale(start, bounds), scale(end, bounds), positions, colors);
		
		return this.paint;
	}

	@Override
	public Rectangle2D getBounds2D() {
		return rectangle;
	}

	@Override
	public CustomGraphicLayer transform(final AffineTransform xform) {
		final LinearGradientLayer newLayer = new LinearGradientLayer(start, end, controlPoints);
		newLayer.rectangle = xform.createTransformedShape(rectangle) .getBounds2D();
		
		return newLayer;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
}
