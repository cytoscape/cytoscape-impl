package org.cytoscape.cg.internal.util;

import javax.swing.Icon;

import org.cytoscape.cg.util.CustomGraphicsIcon;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * Static factory for icons.
 */
public final class VisualPropertyIconFactory {	
	
	public static <V> Icon createIcon(V value, int w, int h) {
		if (value == null)
			return null;
		
		Icon icon = null;
		
		if (value instanceof CyCustomGraphics) {
			var name = ((CyCustomGraphics<?>) value).getDisplayName();
			icon = new CustomGraphicsIcon(((CyCustomGraphics<?>) value), w, h, name);
		} else {
			// If not found, use return value of toString() as icon.
			icon = new TextIcon(value, w, h, value.toString());
		}
		
		return icon;
	}
	
	private VisualPropertyIconFactory() {
		// Restrict instantiation
	}
}
