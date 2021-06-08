package org.cytoscape.cg.internal.gradient.radial;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class RadialGradientFactory implements CyCustomGraphics2Factory<RadialGradientLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;

	public RadialGradientFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public CyCustomGraphics2<RadialGradientLayer> getInstance(String input) {
		return new RadialGradient(input);
	}

	@Override
	public CyCustomGraphics2<RadialGradientLayer> getInstance(CyCustomGraphics2<RadialGradientLayer> gradient) {
		return new RadialGradient((RadialGradient)gradient);
	}

	@Override
	public CyCustomGraphics2<RadialGradientLayer> getInstance(Map<String, Object> properties) {
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
	public JComponent createEditor(CyCustomGraphics2<RadialGradientLayer> gradient) {
		return new RadialGradientEditor((RadialGradient)gradient);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
