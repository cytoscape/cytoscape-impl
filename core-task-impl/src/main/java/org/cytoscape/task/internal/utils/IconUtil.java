package org.cytoscape.task.internal.utils;

import java.awt.Color;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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
	
	// Colors
	public static Color C1 = new Color(5, 62, 96); // Main color
	public static Color C2 = Color.WHITE; // Background color
	public static Color C3 = new Color(56, 119, 158); // Highlight color
	
	public static final Color[] COLORS_2A = new Color[] { C1, C2 };
	public static final Color[] COLORS_2B = new Color[] { C1, C3 };
	public static final Color[] COLORS_3 = new Color[] { C1, C2, C3 };
	
	// Monochrome Icons
	public static final String APPLY_LAYOUT = "a";
	public static final String CYTOSCAPE_LOGO = "b";
	public static final String EXPORT_IMG = "c";
	public static final String EXPORT_NET = "d";
	public static final String EXPORT_TABLE = "e";
	public static final String FIRST_NEIGHBORS = "f";
	public static final String HELP = "g";
	public static final String HIDE_SELECTED = "h";
	public static final String IMPORT_NET = "i";
	public static final String IMPORT_NET_DB = "j";
	public static final String IMPORT_TABLE = "k";
	public static final String NEW_EMPTY = "l";
	public static final String NEW_FROM_SELECTED = "m";
	public static final String OPEN_FILE = "n";
	public static final String SAVE_SESSION = "o";
	public static final String SELECTION_MODE_ANNOTATIONS = "p";
	public static final String SELECTION_MODE_EDGES = "q";
	public static final String SELECTION_MODE_NODES = "r";
	public static final String SHOW_ALL = "s";
	public static final String VENN_DIFFERENCE = "t";
	public static final String VENN_INTERSECTION = "u";
	public static final String VENN_UNION = "v";
	public static final String ZOOM_FIT = "w";
	public static final String ZOOM_IN = "x";
	public static final String ZOOM_OUT = "y";
	public static final String ZOOM_SELECTED = "z";
	
	// Icon Parts
	public static final String ANNOTATION = "A";
	public static final String EDGES = "B";
	
	public static final String HELP_1 = "C";
	public static final String HELP_2 = "D";
	
	public static final String HIDE_SELECTED_1 = "E";
	public static final String HIDE_SELECTED_2 = "F";
	public static final String HIDE_SELECTED_3 = "G";
	
	public static final String IMPORT_NET_1 = "H";
	public static final String IMPORT_NET_2 = "I";
	
	public static final String IMPORT_TABLE_1 = "J";
	public static final String IMPORT_TABLE_2 = "K";
	public static final String IMPORT_TABLE_3 = "L";
	
	public static final String NEW_FROM_SELECTED_1 = "M";
	public static final String NEW_FROM_SELECTED_2 = "N";
	public static final String NEW_FROM_SELECTED_3 = "O";
	
	public static final String NODES = "P";
	
	public static final String OPEN_FILE_1 = "Q";
	public static final String OPEN_FILE_2 = "R";
	
	public static final String SAVE_1 = "S";
	public static final String SAVE_2 = "T";
	public static final String SAVE_3 = "U";
	
	public static final String SELECTION = "V";
	
	public static final String SHOW_ALL_1 = "W";
	public static final String SHOW_ALL_2 = "X";
	
	public static final String ZOOM_1 = "Y";
	public static final String ZOOM_2 = "Z";
	public static final String ZOOM_FIT_3 = "0";
	public static final String ZOOM_IN_3 = "1";
	public static final String ZOOM_OUT_3 = "2";
	public static final String ZOOM_SELECTED_3 = "3";
	
	// Layers/Colors used to build a TextIcon
	// HELP
	public static final String[] LAYERED_HELP = new String[] { HELP_1, HELP_2 };
	// SHOW / HIDE
	public static final String[] LAYERED_SHOW_ALL = new String[] { SHOW_ALL_1, SHOW_ALL_2 };
	public static final String[] LAYERED_HIDE_SELECTED = new String[] { HIDE_SELECTED_1, HIDE_SELECTED_2, HIDE_SELECTED_3 };
	// IMPORT
	public static final String[] LAYERED_IMPORT_NET = new String[] { IMPORT_NET_1, IMPORT_NET_2 };
	public static final String[] LAYERED_IMPORT_TABLE = new String[] { IMPORT_TABLE_1, IMPORT_TABLE_2, IMPORT_TABLE_3 };
	// OPEN / SAVE / NEW
	public static final String[] LAYERED_OPEN_FILE = new String[] { OPEN_FILE_1, OPEN_FILE_2 };
	public static final String[] LAYERED_SAVE = new String[] { SAVE_1, SAVE_2, SAVE_3 };
	public static final String[] LAYERED_NEW_FROM_SELECTED = new String[] { NEW_FROM_SELECTED_1, NEW_FROM_SELECTED_2, NEW_FROM_SELECTED_3 };
	// ZOOM
	public static final String[] LAYERED_ZOOM_FIT = new String[] { ZOOM_1, ZOOM_2, ZOOM_FIT_3 };
	public static final String[] LAYERED_ZOOM_IN = new String[] { ZOOM_1, ZOOM_2, ZOOM_IN_3 };
	public static final String[] LAYERED_ZOOM_OUT = new String[] { ZOOM_1, ZOOM_2, ZOOM_OUT_3 };
	public static final String[] LAYERED_ZOOM_SEL = new String[] { ZOOM_1, ZOOM_2, ZOOM_SELECTED_3 };
	
	private IconUtil() {
		// ...
	}
}
