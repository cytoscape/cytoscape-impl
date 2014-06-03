package org.cytoscape.ding.internal.gradients.linear;

import javax.swing.JComponent;

import org.cytoscape.view.presentation.gradients.CyGradient;
import org.cytoscape.view.presentation.gradients.CyGradientEditorFactory;

public class LinearGradientEditorFactory implements CyGradientEditorFactory<LinearGradientLayer> {

	@Override
	public JComponent createEditor(final CyGradient<LinearGradientLayer> gradient) {
		return new LinearGradientEditor((LinearGradient)gradient);
	}

	@Override
	public Class<? extends CyGradient<LinearGradientLayer>> getSupportedClass() {
		return LinearGradient.class;
	}
}
