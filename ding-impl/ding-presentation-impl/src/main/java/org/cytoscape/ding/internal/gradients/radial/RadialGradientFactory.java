package org.cytoscape.ding.internal.gradients.radial;

import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class RadialGradientFactory implements CyCustomGraphics2Factory<RadialGradientLayer> {
	
	@Override
	public CyCustomGraphics2<RadialGradientLayer> getInstance(final String input) {
		return new RadialGradient(input);
	}

	@Override
	public CyCustomGraphics2<RadialGradientLayer> getInstance(final CyCustomGraphics2<RadialGradientLayer> gradient) {
		return new RadialGradient((RadialGradient)gradient);
	}

	@Override
	public CyCustomGraphics2<RadialGradientLayer> getInstance(final Map<String, Object> properties) {
		return new RadialGradient(properties);
	}

	@Override
	public String getId() {
		return RadialGradient.FACTORY_ID;
	}

	@Override
	public String getDisplayName() {
		return "Radial";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return null;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<RadialGradientLayer>> getSupportedClass() {
		return RadialGradient.class;
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
