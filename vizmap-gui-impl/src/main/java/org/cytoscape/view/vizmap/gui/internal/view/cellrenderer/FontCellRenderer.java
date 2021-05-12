package org.cytoscape.view.vizmap.gui.internal.view.cellrenderer;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.UIManager;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

@SuppressWarnings("serial")
public class FontCellRenderer extends DefaultCellRenderer {

	private static final float FONT_SIZE = 14.0f;

	public FontCellRenderer() {
		setOpaque(true);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		setBackground(isSelected ?
				UIManager.getColor("Table.selectionBackground") : UIManager.getColor("Table.background"));
		var label = convertToString(value);
		setText(label);
		setToolTipText(label);
		
		if (value instanceof Font)
			setFont(((Font) value).deriveFont(FONT_SIZE));

		return this;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
		setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
		
		setText(convertToString(value));

		if (value instanceof Font)
			setFont(((Font) value).deriveFont(FONT_SIZE));

		return this;
	}
	
	@Override
	protected String convertToString(Object value) {
		if (value == null)
			return null;

		if (value instanceof Font) {
			var font = (Font) value;
			return font.getFontName();
		} else {
			return "-- Unknown Font --";
		}
	}
}
