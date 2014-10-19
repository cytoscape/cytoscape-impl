package org.cytoscape.ding.internal.gradients;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class AbstractGradientLayer implements CustomGraphicLayer {
	
	protected final float[] fractions;
	protected final Color[] colors;
	
	protected Rectangle2D bounds = new Rectangle2D.Double(0, 0, 100, 100);
	protected Paint paint;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public AbstractGradientLayer(final float[] fractions, final Color[] colors) {
		// For now, let's use a TreeMap to sort and remove repeated positions,
		// since AWT Gradient Paints do not support duplicate fractions
		final TreeMap<Float, Color> map = new TreeMap<>();
		
		for (int i = 0; i < fractions.length; i++) {
			final float frac = fractions[i];
			final Color color = colors.length > i ? colors[i] : Color.WHITE;
			map.put(frac, color);
		}
		
		this.fractions = new float[map.size()];
		this.colors = new Color[map.size()];
		int i = 0;
		
		for (final Entry<Float, Color> entry : map.entrySet()) {
			this.fractions[i] = entry.getKey();
			this.colors[i] = entry.getValue() != null ? entry.getValue() : Color.WHITE;
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
