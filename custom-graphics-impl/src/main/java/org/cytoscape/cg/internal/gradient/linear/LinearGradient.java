package org.cytoscape.cg.internal.gradient.linear;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cytoscape.cg.internal.gradient.AbstractGradient;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

public class LinearGradient extends AbstractGradient<LinearGradientLayer> {

	public static final String FACTORY_ID = "org.cytoscape.LinearGradient";
	public static final String DISPLAY_NAME = "Linear Gradient";
	
	public static final String ANGLE = "cy_angle";
	
	private BufferedImage renderedImg;
	private volatile boolean dirty = true;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LinearGradient(String input) {
		super(DISPLAY_NAME, input);
	}
	
	public LinearGradient(LinearGradient gradient) {
		super(gradient);
	}

	public LinearGradient(Map<String, Object> properties) {
		super(DISPLAY_NAME, properties);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	@SuppressWarnings("unchecked")
	public List<LinearGradientLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> grView) {
		var layer = createLayer();
		
		return layer != null ? Collections.singletonList(layer) : Collections.EMPTY_LIST;
	}
	
	@Override
	public synchronized Image getRenderedImage() {
		if (dirty) {
			updateRendereredImage();
			dirty = false;
		}
		
		return renderedImg;
	}

	@Override
	public String getId() {
		return FACTORY_ID;
	}
	
	@Override
	public synchronized void set(String key, Object value) {
		super.set(key, value);
		
		if (ANGLE.equalsIgnoreCase(key) ||
				GRADIENT_FRACTIONS.equalsIgnoreCase(key) || GRADIENT_COLORS.equalsIgnoreCase(key))
			dirty = true;
	}
	
	@Override
	public Class<?> getSettingType(String key) {
		if (key.equalsIgnoreCase(ANGLE)) return Double.class;
		
		return super.getSettingType(key);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private LinearGradientLayer createLayer() {
		LinearGradientLayer layer = null;
		var angle = get(ANGLE, Double.class, 0.0);
		var fractions = getList(GRADIENT_FRACTIONS, Float.class);
		var colors = getList(GRADIENT_COLORS, Color.class);
		
		if (angle != null && fractions.size() > 1)
			layer = new LinearGradientLayer(angle, fractions, colors);
		
		return layer;
	}
	
	private void updateRendereredImage() {
		var layer = createLayer();
		
		if (layer != null) {
			// Create a rectangle and fill it with our current paint
			var rect = layer.getBounds2D().getBounds();
			renderedImg = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
			var g2d = renderedImg.createGraphics();
			g2d.setPaint(layer.getPaint(rect));
			g2d.fill(rect);
		} else {
			renderedImg = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		}
	}
}
