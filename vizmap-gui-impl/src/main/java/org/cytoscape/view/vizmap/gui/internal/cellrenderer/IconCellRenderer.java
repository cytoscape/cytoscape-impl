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
package org.cytoscape.view.vizmap.gui.internal.cellrenderer;

import java.awt.Component;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTable;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;


/**
 * Renderer for cells with icon.
 *
 * Icon size is fixed, so caller of this class is responsible
 * for passing proper icons.
 */
public class IconCellRenderer<T> extends DefaultCellRenderer {
	/**
	 *
	 */
	private static final long serialVersionUID = -616290814339403108L;
	private Map<?extends T, Icon> icons;

	/**
	 * Creates a new IconCellRenderer object.
	 *
	 * @param icons  DOCUMENT ME!
	 */
	public IconCellRenderer(Map<?extends T, Icon> icons) {
		this.icons = icons;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param table DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param isSelected DOCUMENT ME!
	 * @param hasFocus DOCUMENT ME!
	 * @param row DOCUMENT ME!
	 * @param column DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	                                               boolean hasFocus, int row, int column) {
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}

		if (value != null) {
			final Icon valueIcon = icons.get(value);

			if (valueIcon != null)
				this.setIcon(valueIcon);

			this.setIconTextGap(10);
			this.setText(value.toString());
		} else {
			this.setIcon(null);
			this.setText(null);
		}

		return this;
	}
}
