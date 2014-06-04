package org.cytoscape.ding.internal.gradients.linear;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.ding.internal.gradients.AbstractGradientCustomGraphics;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

public class LinearGradient extends AbstractGradientCustomGraphics<LinearGradientLayer> {

	public static final String FACTORY_ID = "org.cytoscape.LinearGradient";
	public static final String DISPLAY_NAME = "Linear Gradient";
	
	public static final String START = "start";
	public static final String END = "end";
	public static final String STOP_LIST = "stoplist";
	
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
		
		if (START.equalsIgnoreCase(key) || END.equalsIgnoreCase(key) || STOP_LIST.equalsIgnoreCase(key))
			dirty = true;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(START)) return Point2D.class;
		if (key.equalsIgnoreCase(END)) return Point2D.class;
		if (key.equalsIgnoreCase(STOP_LIST)) return List.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	protected Class<?> getSettingListType(final String key) {
		if (key.equalsIgnoreCase(STOP_LIST)) return ControlPoint.class;
		
		return super.getSettingListType(key);
	}
	
	private LinearGradientLayer createLayer() {
		LinearGradientLayer layer = null;
		final Point2D start = get(START, Point2D.class, new Point2D.Float(0.0f, 0.0f));
		final Point2D end = get(END, Point2D.class, new Point2D.Float(1.0f, 0.0f));
		final List<ControlPoint> controlPoints = getList(STOP_LIST, ControlPoint.class);
		
		if (start != null && end != null && controlPoints.size() > 1)
			layer = new LinearGradientLayer(start, end, controlPoints);
		
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
