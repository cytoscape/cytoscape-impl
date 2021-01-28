package org.cytoscape.cg.internal.gradient;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.cytoscape.cg.model.AbstractCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class AbstractGradient<T extends CustomGraphicLayer> extends AbstractCustomGraphics2<T> {

	public static final String GRADIENT_FRACTIONS = "cy_gradientFractions";
	public static final String GRADIENT_COLORS = "cy_gradientColors";
	
	public static final float DEF_FIT_RATIO = 1.0f;
	
	protected AbstractGradient(String displayName) {
		super(displayName);
		fitRatio = DEF_FIT_RATIO;
	}
	
	protected AbstractGradient(String displayName, String input) {
		super(displayName, input);
		fitRatio = DEF_FIT_RATIO;
	}
	
	protected AbstractGradient(AbstractGradient<T> gradient) {
		this(gradient.getDisplayName());
		addProperties(gradient.getProperties());
	}
	
	protected AbstractGradient(String displayName, Map<String, Object> properties) {
		super(displayName, properties);
		fitRatio = DEF_FIT_RATIO;
	}
	
	@Override
	public String getSerializableString() {
		return toSerializableString();
	}
	
	@Override
	public Class<?> getSettingType(String key) {
		if (key.equalsIgnoreCase(GRADIENT_FRACTIONS)) return List.class;
		if (key.equalsIgnoreCase(GRADIENT_COLORS)) return List.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	public Class<?> getSettingElementType(String key) {
		if (key.equalsIgnoreCase(GRADIENT_FRACTIONS)) return Float.class;
		if (key.equalsIgnoreCase(GRADIENT_COLORS)) return Color.class;
		
		return super.getSettingElementType(key);
	}
}
