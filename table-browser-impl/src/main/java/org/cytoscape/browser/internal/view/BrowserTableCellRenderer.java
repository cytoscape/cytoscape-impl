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


import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.browser.internal.util.ValidatedObjectAndEditString;
import org.cytoscape.util.swing.LookAndFeelUtil;


/** Cell renderer for attribute browser table. */
class BrowserTableCellRenderer extends JLabel implements TableCellRenderer {
	
	private static final long serialVersionUID = -4364566217397320318L;
	
	// Define fonts & colors for the cells
	private static final Color SELECTED_ROW_BG_COLOR = new Color(0, 100, 255, 40);
	private static final Color ERROR_FG_COLOR = Color.RED;
	private static final int H_PAD = 8;
	private static final int V_PAD = 2;
	private static EquationIcon EQUATION_ICON = new EquationIcon();

	public BrowserTableCellRenderer() {
		setOpaque(true);
		
		// Add padding:
		Border border = getBorder();
		
		if (border == null)
			border = BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD);
		else
			border = BorderFactory.createCompoundBorder(border,
					BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD));
		
		setBorder(border);
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
	                                               final boolean hasFocus, final int row, final int column) {
		final ValidatedObjectAndEditString objEditStr = (ValidatedObjectAndEditString) value;
		final Object validatedObj = objEditStr != null ? objEditStr.getValidatedObject() : null;
		
		setFont(getFont().deriveFont(LookAndFeelUtil.INFO_FONT_SIZE));
		setBackground(table.getBackground());
		setIcon(objEditStr != null && objEditStr.isEquation() ? EQUATION_ICON : null);
		setVerticalTextPosition(JLabel.CENTER);
		setHorizontalTextPosition(JLabel.CENTER);
		
		if (validatedObj instanceof Boolean)
			setHorizontalAlignment(JLabel.CENTER);
		else
			setHorizontalAlignment(validatedObj instanceof Number ? JLabel.RIGHT : JLabel.LEFT);
		
		final boolean isError = objEditStr != null && objEditStr.getErrorText() != null;
		
		// First, set values
		if (objEditStr == null || (objEditStr.getValidatedObject() == null && objEditStr.getErrorText() == null)) {
			setText("");
		} else {
			final String displayText = (objEditStr.getErrorText() != null)
				? "#ERR: " + objEditStr.getErrorText()
				: objEditStr.getValidatedObject().toString();
			setText(displayText);
			String tooltipText = displayText;
			
			if (tooltipText.length() > 100)
				setToolTipText(tooltipText.substring(0, 100) + "...");
			else
				setToolTipText(tooltipText);
		}

		// If selected, return
		if (isSelected) {
			if (table.getSelectedColumn() == column && table.getSelectedRow() == row) { // Selected cell
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setForeground(isError ? ERROR_FG_COLOR : table.getForeground());
				setBackground(SELECTED_ROW_BG_COLOR);
			}
		} else {
			// If non-editable, grey it out.
			if (table.getModel() instanceof BrowserTableModel && !table.isCellEditable(0, column))
				setForeground(UIManager.getColor("TextField.inactiveForeground"));
			else
				setForeground(isError ? ERROR_FG_COLOR : table.getForeground());
		}
		
		return this;
	}
	
	private static class EquationIcon implements Icon {

		static final int HEIGHT = 8;
		static final int WIDTH = 8;
		static final Color COLOR = new Color(1, 128, 0);
		
		@Override
		public int getIconHeight() {
			return HEIGHT;
		}

		@Override
		public int getIconWidth() {
			return WIDTH;
		}

		@Override
		public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
			g.setColor(COLOR);
			
			final int[] xPoints = new int[] { 0,      0, WIDTH };
			final int[] yPoints = new int[] { HEIGHT, 0, 0 };
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}
}
