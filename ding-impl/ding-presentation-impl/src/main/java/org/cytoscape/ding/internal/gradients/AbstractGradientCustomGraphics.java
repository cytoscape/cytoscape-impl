package org.cytoscape.ding.internal.gradients;

import java.util.Map;

import org.cytoscape.ding.internal.charts.AbstractEnhancedCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.gradients.CyGradient;

public abstract class AbstractGradientCustomGraphics<T extends CustomGraphicLayer>
		extends AbstractEnhancedCustomGraphics<T> implements CyGradient<T> {

	public static final String STOP_LIST = "stoplist";
	
	protected AbstractGradientCustomGraphics(final String displayName) {
		super(displayName);
	}
	
	protected AbstractGradientCustomGraphics(final String displayName, final String input) {
		super(displayName, input);
	}
	
	protected AbstractGradientCustomGraphics(final AbstractGradientCustomGraphics<T> gradient) {
		this(gradient.getDisplayName());
		this.properties.putAll(gradient.getProperties());
	}
	
	protected AbstractGradientCustomGraphics(final String displayName, final Map<String, Object> properties) {
		super(displayName, properties);
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}
}
