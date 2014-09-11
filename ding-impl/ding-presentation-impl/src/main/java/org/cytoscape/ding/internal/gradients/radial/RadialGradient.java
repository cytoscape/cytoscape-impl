package org.cytoscape.ding.internal.gradients.radial;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.gradients.AbstractGradient;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

public class RadialGradient extends AbstractGradient<RadialGradientLayer> {

	public static final String FACTORY_ID = "org.cytoscape.RadialGradient";
	public static final String DISPLAY_NAME = "Radial Gradient";
	
	public static final String CENTER = "center";
	public static final String RADIUS = "radius";
	
	private BufferedImage renderedImg;
	private volatile boolean dirty = true;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RadialGradient(final String input) {
		super(DISPLAY_NAME, input);
	}
	
	public RadialGradient(final RadialGradient gradient) {
		super(gradient);
	}

	public RadialGradient(final Map<String, Object> properties) {
		super(DISPLAY_NAME, properties);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	@SuppressWarnings("unchecked")
	public List<RadialGradientLayer> getLayers(final CyNetworkView networkView,
			final View<? extends CyIdentifiable> grView) {
		final RadialGradientLayer layer = createLayer();
		
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
		
		if (CENTER.equalsIgnoreCase(key) || RADIUS.equalsIgnoreCase(key) || STOP_LIST.equalsIgnoreCase(key))
			dirty = true;
	}
	
	@Override
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(CENTER)) return Point2D.class;
		if (key.equalsIgnoreCase(RADIUS)) return Float.class;
		
		return super.getSettingType(key);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private RadialGradientLayer createLayer() {
		RadialGradientLayer layer = null;
		final float radius = get(RADIUS, Float.class, 1.0f);
		final Point2D center = get(CENTER, Point2D.class, new Point2D.Float(radius/2, radius/2));
		final List<ControlPoint> controlPoints = getList(STOP_LIST, ControlPoint.class);
		
		if (center != null && controlPoints.size() > 1)
			layer = new RadialGradientLayer(center, radius, controlPoints);
		
		return layer;
	}
	
	private void updateRendereredImage() {
		final RadialGradientLayer layer = createLayer();
		
		if (layer != null) {
			// Create a rectangle and fill it with our current paint
			Rectangle rect = layer.getBounds2D().getBounds();
			rect = new Rectangle(rect.x, rect.y, 100, 100);
			final Shape shape = new Ellipse2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
			renderedImg = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = renderedImg.createGraphics();
			g2d.setPaint(layer.getPaint(rect));
			g2d.fill(shape);
		} else {
			renderedImg = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		}
	}
}
