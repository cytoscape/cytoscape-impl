package org.cytoscape.ding.internal.gradients;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class AbstractGradientLayer implements CustomGraphicLayer {
	
	protected final Map<Float, Color> controlPoints;
	protected final Color[] colors;
	protected final float[] positions;

	protected Rectangle2D bounds;
	protected Paint paint;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public AbstractGradientLayer(final Map<Float, Color> controlPoints) {
		this.controlPoints = controlPoints != null ? controlPoints : new TreeMap<Float, Color>();
		this.bounds = new Rectangle2D.Double(0, 0, 100, 100);
		
		colors = new Color[controlPoints.size()];
		positions = new float[controlPoints.size()];
		int i = 0;
		
		for (final Entry<Float, Color> entry : controlPoints.entrySet()) {
			final Float position = entry.getKey();
			final Color color = entry.getValue();
			colors[i] = color != null ? color : Color.WHITE;
			positions[i] = position != null ? position : (i / (float)controlPoints.size());
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
