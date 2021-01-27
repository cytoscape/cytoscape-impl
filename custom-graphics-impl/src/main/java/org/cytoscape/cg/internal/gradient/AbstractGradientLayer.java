package org.cytoscape.cg.internal.gradient;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class AbstractGradientLayer implements CustomGraphicLayer {
	
	protected final List<Float> fractions;
	protected final List<Color> colors;
	protected final float[] fractionArray;
	protected final Color[] colorArray;
	
	protected Paint paint;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public AbstractGradientLayer(final List<Float> fractions, final List<Color> colors) {
		this.fractions = fractions;
		this.colors = colors;
		// For now, let's use a TreeMap to sort and remove repeated positions,
		// since AWT Gradient Paints do not support duplicate fractions
		final TreeMap<Float, Color> map = new TreeMap<>();
		
		for (int i = 0; i < fractions.size(); i++) {
			final Float frac = fractions.get(i);
			
			if (frac == null)
				continue;
			
			final Color color = colors.size() > i ? colors.get(i) : Color.WHITE;
			map.put(frac, color);
		}
		
		this.fractionArray = new float[map.size()];
		this.colorArray = new Color[map.size()];
		int i = 0;
		
		for (final Entry<Float, Color> entry : map.entrySet()) {
			this.fractionArray[i] = entry.getKey();
			this.colorArray[i] = entry.getValue() != null ? entry.getValue() : Color.WHITE;
			i++;
		}
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
//	@Override
//	public void draw(final Graphics2D g, final Shape shape, final CyNetworkView networkView,
//			final View<? extends CyIdentifiable> view) {
//		final Paint paint = getPaint(shape.getBounds2D());
//		g.setPaint(paint);
//		g.fill(shape);
//	}
}
