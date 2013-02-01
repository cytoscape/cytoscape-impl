package org.cytoscape.browser.internal;

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


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;


/** Cell renderer for attribute browser table. */
class BrowserTableCellRenderer extends JLabel implements TableCellRenderer {
	private static final String HTML_BEG = "<html><body topmargin=\"5\" leftmargin=\"0\" marginheight=\"5\" marginwidth=\"5\" "
	                                       + "bgcolor=\"#ffffff\" text=\"#595959\" link=\"#0000ff\" vlink=\"#800080\" alink=\"#ff0000\">";
	private static final String HTML_STYLE = "<div style=\"width: 200px; background-color: #ffffff; padding: 3px;\"> ";

	// Define fonts & colors for the cells
	private final static Font labelFont = new Font("Sans-serif", Font.BOLD, 12);
	private Font normalFont = new Font("Sans-serif", Font.PLAIN, 12);
	private final Color metadataBackground = new Color(255, 210, 255);
	private static final Color NON_EDITABLE_COLOR = new Color(235, 235, 235, 100);
	private static final Color SELECTED_CELL_COLOR = new Color(0, 100, 255, 40);
	private static final Color SELECTED_LABEL_COLOR = Color.black.brighter();

	public BrowserTableCellRenderer() {
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
	}

	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
	                                               final boolean hasFocus, final int row, final int column)
	{
		setHorizontalAlignment(JLabel.LEFT);

		final ValidatedObjectAndEditString objectAndEditString = (ValidatedObjectAndEditString)value;

		// First, set values
		if (objectAndEditString == null
		    || (objectAndEditString.getValidatedObject() == null && objectAndEditString.getErrorText() == null))
			setText("");
		else {
			final String displayText = (objectAndEditString.getErrorText() != null)
				? objectAndEditString.getErrorText()
				: objectAndEditString.getValidatedObject().toString();
			setText(displayText);
			String tooltipText = displayText;
			if (tooltipText.length() > 100 )
				setToolTipText(tooltipText.substring(0, 100) + "...");
			else
				setToolTipText(tooltipText);
		}

		// If selected, return
		if (isSelected) {
			setFont(labelFont);
			setForeground(SELECTED_LABEL_COLOR);
			setBackground(SELECTED_CELL_COLOR);

			return this;
		}

		// set default colorings
		setForeground(table.getForeground());
		setFont(normalFont);
		setBackground(table.getBackground());
		
		// If ID, return default.
		//if (((BrowserTableModel)  table.getModel()).getDataTable().getPrimaryKey(). )
		if (table.getModel() instanceof BrowserTableModel){
			if (!table.isCellEditable(0, column)){
				setFont(labelFont);
				setBackground(NON_EDITABLE_COLOR);
			}
		}else{
			if (column == 0) {
				setFont(labelFont);
				setBackground(NON_EDITABLE_COLOR);
			}
		}
		return this;
	}
}
