package org.cytoscape.ding.internal.gradients.radial;

import javax.swing.JComponent;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2EditorFactory;

public class RadialGradientEditorFactory implements CyCustomGraphics2EditorFactory<RadialGradientLayer> {

	@Override
	public JComponent createEditor(final CyCustomGraphics2<RadialGradientLayer> gradient) {
		return new RadialGradientEditor((RadialGradient)gradient);
	}

	@Override
	public Class<? extends CyCustomGraphics2<RadialGradientLayer>> getSupportedClass() {
		return RadialGradient.class;
	}
}
