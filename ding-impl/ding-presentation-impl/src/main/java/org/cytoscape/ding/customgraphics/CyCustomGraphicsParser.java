package org.cytoscape.ding.customgraphics;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

public interface CyCustomGraphicsParser {
	public CyCustomGraphics getInstance(final String entry);

	public Class<? extends CyCustomGraphics> getTargetClass();
}
