package org.cytoscape.view.table.internal.impl.icon;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.cytoscape.cg.util.CustomGraphicsIcon;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
