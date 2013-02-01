package org.cytoscape.tableimport.internal.ui.theme;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
