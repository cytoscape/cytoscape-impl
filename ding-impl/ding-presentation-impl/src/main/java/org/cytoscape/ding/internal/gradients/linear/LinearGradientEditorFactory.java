package org.cytoscape.ding.internal.gradients.linear;

import javax.swing.JComponent;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2EditorFactory;

public class LinearGradientEditorFactory implements CyCustomGraphics2EditorFactory<LinearGradientLayer> {

	@Override
	public JComponent createEditor(final CyCustomGraphics2<LinearGradientLayer> gradient) {
		return new LinearGradientEditor((LinearGradient)gradient);
	}

	@Override
	public Class<? extends CyCustomGraphics2<LinearGradientLayer>> getSupportedClass() {
		return LinearGradient.class;
	}
}
