package org.cytoscape.ding;

import java.awt.Image;

import org.cytoscape.ding.customgraphics.AbstractDCustomGraphics;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;

public final class DummyCustomGraphics extends AbstractDCustomGraphics {
	
	public DummyCustomGraphics(Long id, String displayName) {
		super(id, displayName);
	}

	@Override
	public Image getRenderedImage() {
		return NullCustomGraphics.DEF_IMAGE;
	}
}