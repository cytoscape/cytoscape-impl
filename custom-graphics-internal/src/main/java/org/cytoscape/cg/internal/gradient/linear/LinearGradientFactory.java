package org.cytoscape.cg.internal.gradient.linear;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public class LinearGradientFactory implements CyCustomGraphics2Factory<LinearGradientLayer> {
	
	private final CyServiceRegistrar serviceRegistrar;

	public LinearGradientFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public CyCustomGraphics2<LinearGradientLayer> getInstance(String input) {
		return new LinearGradient(input);
	}

	@Override
	public CyCustomGraphics2<LinearGradientLayer> getInstance(CyCustomGraphics2<LinearGradientLayer> gradient) {
		return new LinearGradient((LinearGradient)gradient);
	}

	@Override
	public CyCustomGraphics2<LinearGradientLayer> getInstance(Map<String, Object> properties) {
		return new LinearGradient(properties);
	}

	@Override
	public String getId() {
		return LinearGradient.FACTORY_ID;
	}

	@Override
	public String getDisplayName() {
		return "Linear";
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
	public JComponent createEditor(CyCustomGraphics2<LinearGradientLayer> gradient) {
		return new LinearGradientEditor((LinearGradient)gradient);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
