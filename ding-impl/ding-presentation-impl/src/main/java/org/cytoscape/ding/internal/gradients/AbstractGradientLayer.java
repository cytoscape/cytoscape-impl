package org.cytoscape.ding.internal.gradients;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class AbstractGradientLayer implements CustomGraphicLayer {
	
	protected final List<ControlPoint> controlPoints;
	protected final Color[] colors;
	protected final float[] positions;

	protected Rectangle2D bounds;
	protected Paint paint;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public AbstractGradientLayer(final List<ControlPoint> controlPoints) {
		this.controlPoints = controlPoints != null ? controlPoints : new ArrayList<ControlPoint>();
		this.bounds = new Rectangle2D.Double(0, 0, 100, 100);
		
		colors = new Color[controlPoints.size()];
		positions = new float[controlPoints.size()];
		int i = 0;
		
		for (ControlPoint cp : controlPoints) {
			colors[i] = cp.color;
			positions[i] = cp.position;
			i++;
		}
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public Rectangle2D getBounds2D() {
		return bounds;
	}

	@Override
	public CustomGraphicLayer transform(final AffineTransform xform) {
		final Shape s = xform.createTransformedShape(bounds);
		bounds = s.getBounds2D();
		
		return this;
	}
}
