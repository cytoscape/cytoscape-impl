package org.cytoscape.view.vizmap.gui.internal.util;

import java.awt.Color;
import java.awt.Font;

import org.cytoscape.view.presentation.property.values.VisualPropertyValue;

public final class VisualPropertyUtil {

	public static String getDisplayString(final Object value) {
		String s = null;
		
		if (value instanceof VisualPropertyValue) {
			s = ((VisualPropertyValue)value).getDisplayName();
		} else if (value instanceof Font) {
			s  = ((Font)value).getFontName();
		} else if (value instanceof Color) {
			final Color c  = (Color)value;
			int r = c.getRed();
			int g = c.getGreen();
			int b = c.getBlue();
			s = String.format("R:%s G:%s B:%s - #%02x%02x%02x", r, g, b, r, g, b).toUpperCase();
		} else if (value != null) {
			s = value.toString();
		}
		
		return s;
	}
	
	private VisualPropertyUtil() {
		// restrict instantiation
	}
}
