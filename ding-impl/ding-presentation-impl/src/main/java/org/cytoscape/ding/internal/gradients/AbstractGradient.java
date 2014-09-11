package org.cytoscape.ding.internal.gradients;

import java.util.List;
import java.util.Map;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2;
import org.cytoscape.ding.customgraphics.json.ControlPointJsonDeserializer;
import org.cytoscape.ding.internal.charts.ControlPoint;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

import com.fasterxml.jackson.databind.module.SimpleModule;

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
	public String getSerializableString() {
		return toSerializableString();
	}
	
	@Override
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(STOP_LIST)) return List.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	public Class<?> getSettingListType(final String key) {
		if (key.equalsIgnoreCase(STOP_LIST)) return ControlPoint.class;
		
		return super.getSettingListType(key);
	}
	
	@Override
	protected void addJsonDeserializers(final SimpleModule module) {
		super.addJsonDeserializers(module);
		module.addDeserializer(ControlPoint.class, new ControlPointJsonDeserializer());
	}
}
