package org.cytoscape.ding.internal.gradients.radial;

import java.util.Map;

import org.cytoscape.view.presentation.gradients.CyGradient;
import org.cytoscape.view.presentation.gradients.CyGradientFactory;

public class RadialGradientFactory implements CyGradientFactory<RadialGradientLayer> {
	
	@Override
	public CyGradient<RadialGradientLayer> getInstance(final String input) {
		return new RadialGradient(input);
	}

	@Override
	public CyGradient<RadialGradientLayer> getInstance(final CyGradient<RadialGradientLayer> gradient) {
		return new RadialGradient((RadialGradient)gradient);
	}

	@Override
	public CyGradient<RadialGradientLayer> getInstance(final Map<String, Object> properties) {
		return new RadialGradient(properties);
	}

	@Override
	public String getId() {
		return RadialGradient.FACTORY_ID;
	}

	@Override
	public String getDisplayName() {
		return "Radial Gradient";
	}
	
	@Override
	public Class<? extends CyGradient<RadialGradientLayer>> getSupportedClass() {
		return RadialGradient.class;
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
