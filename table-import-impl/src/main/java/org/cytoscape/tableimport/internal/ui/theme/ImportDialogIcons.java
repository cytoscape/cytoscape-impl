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
 * Icon set for Import Dialog GUI.
 */
public enum ImportDialogIcons {
	ID_ICON("images/ximian/stock_3d-light-on-16.png"),
	REMOTE_SOURCE_ICON("images/ximian/stock_internet-16.png"),
	LOCAL_SOURCE_ICON("images/ximian/stock_data-sources-modified-16.png");

	private String resourceLoc;

	private ImportDialogIcons(String resourceLocation) {
		this.resourceLoc = resourceLocation;
	}

	public ImageIcon getIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource(resourceLoc));
	}
}
