package org.cytoscape.view.table.internal.impl.icon;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;


public class VisualPropertyIconFactory {	
	
	public static <V> Icon createIcon(V value, int w, int h) {
		if(value == null)
			return null;
		
		Icon icon = null;
		
		if(value instanceof Color) {
			icon = new ColorIcon((Color) value, w, h, value.toString());
		} else if(value instanceof Font) {
			icon = new FontFaceIcon((Font) value, w, h, "");
		} else {
			// If not found, use return value of toString() as icon.
			icon = new TextIcon(value, w, h, value.toString());
		}
		
		return icon;
	}
}