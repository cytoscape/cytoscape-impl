package org.cytoscape.ding.customgraphics;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

public interface CyCustomGraphicsParserFactory {
	public CyCustomGraphicsParser getParser(final String customGraphicsClassName);

	public void registerParser(Class<? extends CyCustomGraphics> cgClass,
			CyCustomGraphicsParser parser);
}
