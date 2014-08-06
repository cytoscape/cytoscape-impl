package org.cytoscape.ding.internal.gradients.radial;

import javax.swing.JComponent;

import org.cytoscape.view.presentation.gradients.CyGradient;
import org.cytoscape.view.presentation.gradients.CyGradientEditorFactory;

public class RadialGradientEditorFactory implements CyGradientEditorFactory<RadialGradientLayer> {

	@Override
	public JComponent createEditor(final CyGradient<RadialGradientLayer> gradient) {
		return new RadialGradientEditor((RadialGradient)gradient);
	}

	@Override
	public Class<? extends CyGradient<RadialGradientLayer>> getSupportedClass() {
		return RadialGradient.class;
	}
}
