package org.cytoscape.view.vizmap.gui.internal.view.cellrenderer;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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
import java.awt.Component;
import java.awt.Font;

import javax.swing.JList;
import javax.swing.JTable;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public class FontCellRenderer extends DefaultCellRenderer {

	private static final long serialVersionUID = -667720462619223580L;
	
	private static final Color BG_COLOR = Color.WHITE;
	private static final Color SELECTED_BG_COLOR = new Color(222, 234, 252);
	private static final float FONT_SIZE = 14.0f;

	public FontCellRenderer() {
		setOpaque(true);
	}
	
	@Override
	public Component getListCellRendererComponent(final JList list,
												  final Object value,
												  final int index,
												  final boolean isSelected,
												  final boolean cellHasFocus) {
		setBackground(isSelected ? SELECTED_BG_COLOR : BG_COLOR);
		final String label = convertToString(value);
		setText(label);
		setToolTipText(label);
		
		if (value instanceof Font)
			setFont(((Font) value).deriveFont(FONT_SIZE));

		return this;
	}
	
	@Override
	public Component getTableCellRendererComponent(final JTable table,
												   final Object value,
												   final boolean isSelected,
												   final boolean hasFocus,
												   final int row,
												   final int column) {
		setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
		setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
		
		setText(convertToString(value));

		if (value instanceof Font)
			setFont(((Font) value).deriveFont(FONT_SIZE));

		return this;
	}
	
	@Override
	protected String convertToString(final Object value) {
		if (value == null)
			return null;

		if (value instanceof Font) {
			final Font font = (Font) value;
			return font.getFontName();
		} else {
			return "-- Unknown Font --";
		}
	}
}
