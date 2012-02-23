package org.cytoscape.view.vizmap.gui.internal.util;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public final class VisualPropertyFilter {

	private static final Set<VisualProperty<?>> INCOMPATIBLE_VP_SET = new HashSet<VisualProperty<?>>();

	// Visual Properties which are not compatible with current rendering engine.
	static {
		INCOMPATIBLE_VP_SET.add(BasicVisualLexicon.NODE_DEPTH);
		INCOMPATIBLE_VP_SET.add(BasicVisualLexicon.NODE_Z_LOCATION);
	}

	
	public static Boolean isCompatible(final VisualProperty<?> vp) {
		if (INCOMPATIBLE_VP_SET.contains(vp))
			return false;
		else
			return true;
	}
}
