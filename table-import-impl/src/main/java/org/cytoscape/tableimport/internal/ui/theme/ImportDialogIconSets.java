
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
	INTERACTION_ICON("images/ximian/stock_interaction.png"),
	SPREADSHEET_ICON_LARGE("images/ximian/stock_new-spreadsheet-48.png"),
	REMOTE_SOURCE_ICON("images/ximian/stock_internet-16.png"),
	REMOTE_SOURCE_ICON_LARGE("images/ximian/stock_internet-32.png"),
	LOCAL_SOURCE_ICON("images/ximian/stock_data-sources-modified-16.png"),
	SPREADSHEET_ICON("images/ximian/stock_new-spreadsheet.png"),
	TEXT_FILE_ICON("images/ximian/stock_new-text-32.png"),
	RIGHT_ARROW_ICON("images/ximian/stock_right-16.png"),
	CAUTION_ICON("images/ximian/stock_dialog-warning-32.png"),
	CHECKED_ICON("images/ximian/stock_3d-apply-16.png"),
	UNCHECKED_ICON("images/ximian/stock_close-16.png");

	private String resourceLoc;

	private ImportDialogIconSets(String resourceLocation) {
		this.resourceLoc = resourceLocation;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public ImageIcon getIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource(resourceLoc));
	}
}
