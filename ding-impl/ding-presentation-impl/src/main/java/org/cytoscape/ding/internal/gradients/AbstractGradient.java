package org.cytoscape.ding.internal.gradients;

import java.util.Map;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class AbstractGradient<T extends CustomGraphicLayer> extends AbstractCustomGraphics2<T> {

	public static final String STOP_LIST = "stopList";
	
	protected AbstractGradient(final String displayName) {
		super(displayName);
	}
	
	protected AbstractGradient(final String displayName, final String input) {
		super(displayName, input);
	}
	
	protected AbstractGradient(final AbstractGradient<T> gradient) {
		this(gradient.getDisplayName());
		this.properties.putAll(gradient.getProperties());
	}
	
	protected AbstractGradient(final String displayName, final Map<String, Object> properties) {
		super(displayName, properties);
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	@Override
	public String getSerializableString() {
		return toSerializableString();
	}
}
