
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.tableimport.internal.ui.theme;

import java.awt.Color;

/**
 * Color theme for Import Dialogs.<br>
 *
 * @since Cytoscape 2.4
 * @version 1.0
 * @author kono
 *
 */
public enum ImportDialogColorTheme {
	LABEL_COLOR(Color.black),
	KEY_ATTR_COLOR(Color.red),
	PRIMARY_KEY_COLOR(new Color(51, 51, 255)),
	ONTOLOGY_COLOR(new Color(0, 255, 255)),
	ALIAS_COLOR(new Color(51, 204, 0)),
	SPECIES_COLOR(new Color(182, 36, 212)),
	ATTRIBUTE_NAME_COLOR(new Color(102, 102, 255)),
	NOT_SELECTED_COL_COLOR(new Color(240, 240, 240)),
	SELECTED_COLOR(Color.BLACK),
	UNSELECTED_COLOR(Color.GRAY),

	//	HEADER_BACKGROUND_COLOR(new Color(165, 200, 254)),
	HEADER_BACKGROUND_COLOR(Color.WHITE),
	HEADER_UNSELECTED_BACKGROUND_COLOR(new Color(240, 240, 240)), NOT_LOADED_COLOR(Color.RED), 
	LOADED_COLOR(Color.GREEN),SOURCE_COLOR(new Color(204, 0, 204)), 
	INTERACTION_COLOR(new Color(255, 0, 51)),TARGET_COLOR(new Color(255, 102, 0)), 
	EDGE_ATTR_COLOR(Color.BLUE);
	private Color color;

	private ImportDialogColorTheme(Color color) {
		this.color = color;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Color getColor() {
		return color;
	}
}
