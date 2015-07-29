package org.cytoscape.browser.internal.view;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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
import java.util.Collections;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.cytoscape.model.CyTable;

public class TableChooserCellRenderer extends JLabel implements ListCellRenderer<CyTable> {

	private static final long serialVersionUID = 3512300857227705136L;
	
	private final Map<CyTable, String> tableToStringMap;

	@SuppressWarnings("unchecked")
	TableChooserCellRenderer() {
		this(Collections.EMPTY_MAP);
	}
	
	TableChooserCellRenderer(final Map<CyTable, String> tableToStringMap) {
		this.tableToStringMap = tableToStringMap;
	}

	@Override
	public Component getListCellRendererComponent(final JList<? extends CyTable> list, final CyTable table,
			final int index, final boolean isSelected, final boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		String label = tableToStringMap.get(table);
		
		if (label == null)
			label = table == null ? "-- No Table --" : table.getTitle();

		if (!label.toLowerCase().contains("table"))
			label += " Table";
		
		setText(label);
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);

		return this;
	}
}
