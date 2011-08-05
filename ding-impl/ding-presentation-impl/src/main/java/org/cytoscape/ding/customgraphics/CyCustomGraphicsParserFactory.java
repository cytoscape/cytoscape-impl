package org.cytoscape.ding.customgraphics;

public interface CyCustomGraphicsParserFactory {
	public CyCustomGraphicsParser getParser(final String customGraphicsClassName);

	public void registerParser(Class<? extends CyCustomGraphics> cgClass,
			CyCustomGraphicsParser parser);
}
