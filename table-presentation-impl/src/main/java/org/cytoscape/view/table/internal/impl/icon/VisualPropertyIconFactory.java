package org.cytoscape.view.table.internal.impl.icon;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.cytoscape.cg.util.CustomGraphicsIcon;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

public class VisualPropertyIconFactory {

	public static <V> Icon createIcon(V value, int w, int h) {
		if (value == null)
			return null;

		Icon icon = null;

		if (value instanceof Color) {
			icon = new ColorIcon((Color) value, w, h, value.toString());
		} else if (value instanceof Font) {
			icon = new FontFaceIcon((Font) value, w, h, "");
		} else if (value instanceof CyCustomGraphics) {
			var name = ((CyCustomGraphics<?>) value).getDisplayName();
			icon = new CustomGraphicsIcon(((CyCustomGraphics<?>) value), w, h, name);
		} else {
			// If not found, use return value of toString() as icon.
			icon = new TextIcon(value, w, h, value.toString());
		}

		return icon;
	}
}
