package org.cytoscape.ding.internal.gradients.linear;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.gradients.AbstractGradient;
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
	
	public LinearGradient(final String input) {
		super(DISPLAY_NAME, input);
	}
	
	public LinearGradient(final LinearGradient gradient) {
		super(gradient);
	}

	public LinearGradient(final Map<String, Object> properties) {
		super(DISPLAY_NAME, properties);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	@SuppressWarnings("unchecked")
	public List<LinearGradientLayer> getLayers(final CyNetworkView networkView,
			final View<? extends CyIdentifiable> grView) {
		final LinearGradientLayer layer = createLayer();
		
		return layer != null ? Collections.singletonList(layer) : Collections.emptyList();
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
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(ANGLE)) return Double.class;
		
		return super.getSettingType(key);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private LinearGradientLayer createLayer() {
		LinearGradientLayer layer = null;
		final Double angle = get(ANGLE, Double.class, 0.0);
		final List<Float> fractions = getList(GRADIENT_FRACTIONS, Float.class);
		final List<Color> colors = getList(GRADIENT_COLORS, Color.class);
		
		if (angle != null && fractions.size() > 1)
			layer = new LinearGradientLayer(angle, fractions, colors);
		
		return layer;
	}
	
	private void updateRendereredImage() {
		final LinearGradientLayer layer = createLayer();
		
		if (layer != null) {
			// Create a rectangle and fill it with our current paint
			final Rectangle rect = layer.getBounds2D().getBounds();
			renderedImg = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = renderedImg.createGraphics();
			g2d.setPaint(layer.getPaint(rect));
			g2d.fill(rect);
		} else {
			renderedImg = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		}
	}
}
