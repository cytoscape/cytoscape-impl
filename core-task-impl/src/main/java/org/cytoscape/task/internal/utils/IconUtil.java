package org.cytoscape.task.internal.utils;

import java.awt.Color;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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
	
	// Colors
	public static Color C1 = new Color(5, 62, 96); // Main color
	public static Color C2 = Color.WHITE; // Background color
	public static Color C3 = new Color(56, 120, 158); // Highlight color
	
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
	public static final String SHOW_ALL = "p";
	public static final String VENN_DIFFERENCE = "q";
	public static final String VENN_INTERSECTION = "r";
	public static final String VENN_UNION = "s";
	public static final String ZOOM_FIT = "t";
	public static final String ZOOM_IN = "u";
	public static final String ZOOM_OUT = "v";
	public static final String ZOOM_SELECTED = "w";
	
	// Icon Parts
	public static final String HELP_1 = "x";
	public static final String HELP_2 = "y";
	
	public static final String HIDE_SELECTED_1 = "z";
	public static final String HIDE_SELECTED_2 = "A";
	public static final String HIDE_SELECTED_3 = "B";
	
	public static final String IMPORT_NET_1 = "C";
	public static final String IMPORT_NET_2 = "D";
	
	public static final String IMPORT_TABLE_1 = "E";
	public static final String IMPORT_TABLE_2 = "F";
	public static final String IMPORT_TABLE_3 = "G";
	
	public static final String NEW_FROM_SELECTED_1 = "H";
	public static final String NEW_FROM_SELECTED_2 = "I";
	public static final String NEW_FROM_SELECTED_3 = "J";
	
	public static final String OPEN_FILE_1 = "K";
	public static final String OPEN_FILE_2 = "L";
	
	public static final String SAVE_1 = "M";
	public static final String SAVE_2 = "N";
	public static final String SAVE_3 = "O";
	
	public static final String SHOW_ALL_1 = "P";
	public static final String SHOW_ALL_2 = "Q";
	
	public static final String ZOOM_1 = "R";
	public static final String ZOOM_2 = "S";
	public static final String ZOOM_FIT_3 = "T";
	public static final String ZOOM_IN_3 = "U";
	public static final String ZOOM_OUT_3 = "V";
	public static final String ZOOM_SELECTED_3 = "W";
	
	// Annotations Icons
	public static final String ANNOTATION_1 = "X";
	public static final String ANNOTATION_2 = "Y";
	public static final String ANNOTATION_ARROW = "Z";
	public static final String ANNOTATION_BOUNDED_TEXT_1 = "0";
	public static final String ANNOTATION_BOUNDED_TEXT_2 = "1";
	public static final String ANNOTATION_IMAGE_1 = "2";
	public static final String ANNOTATION_IMAGE_2 = "3";
	public static final String ANNOTATION_SHAPE_1 = "4";
	public static final String ANNOTATION_SHAPE_2 = "5";
	public static final String ANNOTATION_TEXT = "6";
	
	public static final String SELECTION_MODE_ANNOTATIONS = "7";
	public static final String SELECTION_MODE_EDGES = "8";
	public static final String SELECTION_MODE_NODES = "9";
	
	// Generic Import/Export
	public static final String FILE_EXPORT = "+";
	public static final String FILE_IMPORT = ",";
	
	// Layers/Colors used to build a TextIcon
	// HELP
	public static final String[] LAYERED_HELP = new String[] { HELP_1, HELP_2 };
	// SHOW / HIDE
	public static final String[] LAYERED_SHOW_ALL = new String[] { SHOW_ALL_1, SHOW_ALL_2 };
	public static final String[] LAYERED_HIDE_SELECTED = new String[] { HIDE_SELECTED_1, HIDE_SELECTED_2, HIDE_SELECTED_3 };
	// IMPORT
	public static final String[] LAYERED_IMPORT_NET = new String[] { IMPORT_NET_1, IMPORT_NET_2 };
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
