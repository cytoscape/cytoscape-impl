package org.cytoscape.cg.model;

import java.awt.Image;

@SuppressWarnings("rawtypes")
public final class DummyCustomGraphics extends AbstractDCustomGraphics {
	
	public DummyCustomGraphics(Long id, String displayName) {
		super(id, displayName);
	}

	@Override
	public Image getRenderedImage() {
		return NullCustomGraphics.DEF_IMAGE;
	}

	@Override
	public String toString() {
		return "DummyCustomGraphics ("+id+") "+displayName;
	}

	@Override
	public String toSerializableString() {
		return makeSerializableString(displayName);
	}
}
