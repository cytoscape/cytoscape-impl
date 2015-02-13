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

import javax.swing.ImageIcon;

/**
 * Iconset for Import Dialog GUI.<br>
 *
 * <p>
 *  By sharing these enums among GUIs, we can define Theme for the dialog.
 * </p>
 *
 * @author kono
 *
 */
public enum ImportDialogIconSets {
	STRING_ICON("images/ximian/stock_font-16.png"),
	INTEGER_ICON("images/ximian/stock_sort-row-ascending-16.png"),
	FLOAT_ICON("images/ximian/stock_format-scientific-16.png"),
	INT_ICON("images/ximian/stock_sort-row-ascending-16.png"),
	LIST_ICON("images/ximian/stock_navigator-list-box-toggle-16.png"),
	BOOLEAN_ICON("images/ximian/stock_form-radio-16.png"),
	ID_ICON("images/ximian/stock_3d-light-on-16.png"),
	SPREADSHEET_ICON_LARGE("images/ximian/stock_new-spreadsheet-48.png"),
	REMOTE_SOURCE_ICON("images/ximian/stock_internet-16.png"),
	LOCAL_SOURCE_ICON("images/ximian/stock_data-sources-modified-16.png");

	private String resourceLoc;

	private ImportDialogIconSets(String resourceLocation) {
		this.resourceLoc = resourceLocation;
	}

	public ImageIcon getIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource(resourceLoc));
	}
}
