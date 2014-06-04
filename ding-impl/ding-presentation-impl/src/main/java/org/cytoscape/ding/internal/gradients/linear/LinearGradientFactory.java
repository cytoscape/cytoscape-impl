package org.cytoscape.ding.internal.gradients.linear;

import java.util.Map;

import org.cytoscape.view.presentation.gradients.CyGradient;
import org.cytoscape.view.presentation.gradients.CyGradientFactory;

public class LinearGradientFactory implements CyGradientFactory<LinearGradientLayer> {
	
	@Override
	public CyGradient<LinearGradientLayer> getInstance(final String input) {
		return new LinearGradient(input);
	}

	@Override
	public CyGradient<LinearGradientLayer> getInstance(final CyGradient<LinearGradientLayer> gradient) {
		return new LinearGradient((LinearGradient)gradient);
	}

	@Override
	public CyGradient<LinearGradientLayer> getInstance(final Map<String, Object> properties) {
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
	public Class<? extends CyGradient<LinearGradientLayer>> getSupportedClass() {
		return LinearGradient.class;
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
