package org.cytoscape.internal.util;

import java.awt.Color;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public abstract class IconUtil {
	
	public static final String CY_FONT_NAME = "cytoscape-3";
	
	// Colors
	public static Color C1 = new Color(5, 62, 96); // Main color
	public static Color C2 = Color.WHITE; // Background color
	public static Color C3 = new Color(56, 120, 158); // Highlight color
	
	public static final Color[] COLORS_2A = new Color[] { C1, C2 };
	public static final Color[] COLORS_2B = new Color[] { C1, C3 };
	public static final Color[] COLORS_3 = new Color[] { C1, C2, C3 };
	
	public static final String CYTOSCAPE_LOGO = "b";
	
	public static final String NEW_FROM_SELECTED_1 = "H";
	public static final String NEW_FROM_SELECTED_2 = "I";
	public static final String NEW_FROM_SELECTED_3 = "J";
	
	public static final String SELECTION_MODE_ANNOTATIONS = "7";
	public static final String SELECTION_MODE_EDGES = "8";
	public static final String SELECTION_MODE_NODES = "9";
	public static final String SELECTION_MODE_NODE_LABELS = ":";
	
	public static final String TERMINAL = "%";
	
	public static final String PIN = "'";
	public static final String PIN_ALL = "(";
	public static final String UNPIN = ")";
	
	public static final String GD_HIGH = "$";
	public static final String GD_LOW = "&";
	
	public static final String FILE_EXPORT = "+";
	
	// Layers used to build a TextIcon
	public static final String[] LAYERED_NEW_FROM_SELECTED = new String[] { NEW_FROM_SELECTED_1, NEW_FROM_SELECTED_2, NEW_FROM_SELECTED_3 };
	
	private IconUtil() {
		// ...
	}
}
