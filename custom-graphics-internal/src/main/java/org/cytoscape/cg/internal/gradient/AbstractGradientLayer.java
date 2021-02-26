package org.cytoscape.cg.internal.gradient;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.TreeMap;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class AbstractGradientLayer implements CustomGraphicLayer {
	
	protected final List<Float> fractions;
	protected final List<Color> colors;
	protected final float[] fractionArray;
	protected final Color[] colorArray;
	
	protected Paint paint;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public AbstractGradientLayer(List<Float> fractions, List<Color> colors) {
		this.fractions = fractions;
		this.colors = colors;
		// For now, let's use a TreeMap to sort and remove repeated positions,
		// since AWT Gradient Paints do not support duplicate fractions
		var map = new TreeMap<Float, Color>();
		
		for (int i = 0; i < fractions.size(); i++) {
			var frac = fractions.get(i);
			
			if (frac == null)
				continue;
			
			var color = colors.size() > i ? colors.get(i) : Color.WHITE;
			map.put(frac, color);
		}
		
		this.fractionArray = new float[map.size()];
		this.colorArray = new Color[map.size()];
		int i = 0;
		
		for (var entry : map.entrySet()) {
			this.fractionArray[i] = entry.getKey();
			this.colorArray[i] = entry.getValue() != null ? entry.getValue() : Color.WHITE;
			i++;
		}
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
//	@Override
//	public void draw(Graphics2D g, Shape shape, CyNetworkView networkView, View<? extends CyIdentifiable> view) {
//		final Paint paint = getPaint(shape.getBounds2D());
//		g.setPaint(paint);
//		g.fill(shape);
//	}
}
