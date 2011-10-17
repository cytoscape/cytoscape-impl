package org.cytoscape.view.vizmap.gui.internal;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.RichVisualLexicon;

public final class PropertySheetUtil {
	
	private static final Set<VisualProperty<?>> BASIC_PROPS = new HashSet<VisualProperty<?>>();
	
	private static boolean mode = false;
	
	// Preset Basic Properties
	static {
		BASIC_PROPS.add(RichVisualLexicon.NODE_FILL_COLOR);
		BASIC_PROPS.add(RichVisualLexicon.NODE_SHAPE);
		BASIC_PROPS.add(RichVisualLexicon.NODE_WIDTH);
		BASIC_PROPS.add(RichVisualLexicon.NODE_HEIGHT);
		BASIC_PROPS.add(RichVisualLexicon.NODE_LABEL);
		BASIC_PROPS.add(RichVisualLexicon.NODE_BORDER_PAINT);
		BASIC_PROPS.add(RichVisualLexicon.NODE_BORDER_WIDTH);
		
		BASIC_PROPS.add(RichVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		BASIC_PROPS.add(RichVisualLexicon.EDGE_WIDTH);
		BASIC_PROPS.add(RichVisualLexicon.EDGE_LABEL);
		BASIC_PROPS.add(RichVisualLexicon.EDGE_LINE_TYPE);
	}
	
	public static boolean isBasic(final VisualProperty<?> vp) {
		if(BASIC_PROPS.contains(vp))
			return true;
		else
			return false;
	}
	
	public static boolean isAdvancedMode() {
		return mode;
	}
	
	public static void setMode(boolean advanced) {
		mode = advanced;
	}

}
