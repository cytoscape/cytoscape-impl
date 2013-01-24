package org.cytoscape.tableimport.internal.ui;

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

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class ComboBoxRenderer extends JLabel implements ListCellRenderer {
	private List<Byte> attributeDataTypes;

	/**
	 * Creates a new ComboBoxRenderer object.
	 *
	 * @param attributeDataTypes  DOCUMENT ME!
	 */
	public ComboBoxRenderer(List<Byte> attributeDataTypes) {
		this.attributeDataTypes = attributeDataTypes;
		setOpaque(true);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param list DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param index DOCUMENT ME!
	 * @param isSelected DOCUMENT ME!
	 * @param cellHasFocus DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index,
	                                              boolean isSelected, boolean cellHasFocus) {
		setText(value.toString());
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		if ((attributeDataTypes != null) && (attributeDataTypes.size() != 0)
		    && (index < attributeDataTypes.size()) && (index >= 0)) {
			final Byte dataType = attributeDataTypes.get(index);

			if (dataType == null) {
				setIcon(null);
			} else {
				setIcon(ImportTablePanel.getDataTypeIcon(dataType));
			}
		} else if ((attributeDataTypes != null) && (attributeDataTypes.size() != 0)
		           && (index < attributeDataTypes.size())) {
			setIcon(ImportTablePanel.getDataTypeIcon(attributeDataTypes.get(list.getSelectedIndex())));
		}

		return this;
	}
}