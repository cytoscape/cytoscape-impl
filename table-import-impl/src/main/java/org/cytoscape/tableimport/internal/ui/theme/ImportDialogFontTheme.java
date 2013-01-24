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

import java.awt.Font;
public enum ImportDialogFontTheme {
	TITLE_FONT(new Font("Sans-serif", Font.BOLD, 18)),
	SELECTED_COL_FONT(new Font("Sans-serif", Font.BOLD, 14)),
	SELECTED_FONT(new Font("Sans-serif", Font.BOLD, 14)),
	UNSELECTED_FONT(new Font("Sans-serif", Font.PLAIN, 14)),
	KEY_FONT(new Font("Sans-Serif", Font.BOLD, 14)),
	LABEL_FONT(new Font("Sans-serif", Font.BOLD, 14)),
	LABEL_ITALIC_FONT(new Font("Sans-serif", 3, 14)),
	ITEM_FONT(new Font("Sans-serif", Font.BOLD, 12)),
	ITEM_FONT_LARGE(new Font("Sans-serif", Font.BOLD, 14));

	private Font font;

	private ImportDialogFontTheme(Font font) {
		this.font = font;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Font getFont() {
		return font;
	}
}
