package org.cytoscape.ding.internal.gradients.linear;

import java.util.Map;

import org.cytoscape.view.presentation.gradients.CyGradient;
import org.cytoscape.view.presentation.gradients.CyGradientFactory;

public class LinearGradientFactory implements CyGradientFactory<LinearGradientLayer> {
	
	@Override
	public CyGradient<LinearGradientLayer> getInstance(String input) {
		return new LinearGradient(input);
	}

	@Override
	public CyGradient<LinearGradientLayer> getInstance(CyGradient<LinearGradientLayer> gradient) {
		// TODO Auto-generated method stub
		return new LinearGradient("");
	}

	@Override
	public CyGradient<LinearGradientLayer> getInstance(Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return new LinearGradient("");
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
