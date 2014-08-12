package org.cytoscape.ding.internal.gradients.linear;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class LinearGradientFactory implements CyCustomGraphics2Factory<LinearGradientLayer> {
	
	@Override
	public CyCustomGraphics2<LinearGradientLayer> getInstance(final String input) {
		return new LinearGradient(input);
	}

	@Override
	public CyCustomGraphics2<LinearGradientLayer> getInstance(final CyCustomGraphics2<LinearGradientLayer> gradient) {
		return new LinearGradient((LinearGradient)gradient);
	}

	@Override
	public CyCustomGraphics2<LinearGradientLayer> getInstance(final Map<String, Object> properties) {
		return new LinearGradient(properties);
	}

	@Override
	public String getId() {
		return LinearGradient.FACTORY_ID;
	}

	@Override
	public String getDisplayName() {
		return "Linear Gradient";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return null;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<LinearGradientLayer>> getSupportedClass() {
		return LinearGradient.class;
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
