package org.cytoscape.cg.model;

import java.awt.Image;

import org.cytoscape.cg.internal.util.IconUtil;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * Null object for Custom Graphics. This is used to reset custom graphics on node views.
 */
public class NullCustomGraphics extends AbstractDCustomGraphics<CustomGraphicLayer> {
	
	public static Image DEF_IMAGE = IconUtil.emptyIcon(24, 24).getImage();
	
	// Human readable name of this null object.
	private static final String NAME = "[ Remove Graphics ]";
	
	static final CyCustomGraphics<CustomGraphicLayer> NULL = new NullCustomGraphics();

	public static CyCustomGraphics<CustomGraphicLayer> getNullObject() {
		return NULL;
	}

	private NullCustomGraphics() {
		super(0l, NAME);
	}

	@Override
	public String toString() {
		return "None";
	}

	@Override
	public Image getRenderedImage() {
		return DEF_IMAGE;
	}

	@Override
	public String toSerializableString() {
		return makeSerializableString(displayName);
	}
}
