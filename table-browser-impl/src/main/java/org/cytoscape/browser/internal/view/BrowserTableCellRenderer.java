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
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.browser.internal.util.ValidatedObjectAndEditString;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;


/** Cell renderer for attribute browser table. */
class BrowserTableCellRenderer extends JLabel implements TableCellRenderer {
	
	private static final long serialVersionUID = -4364566217397320318L;
	
	// Define fonts & colors for the cells
	private static final int H_PAD = 8;
	private static final int V_PAD = 2;
	private static EquationIcon EQUATION_ICON = new EquationIcon();
	private final Font defaultFont;
	private final IconManager iconManager;

	public BrowserTableCellRenderer(final IconManager iconManager) {
		this.iconManager = iconManager;
		defaultFont = getFont().deriveFont(LookAndFeelUtil.getSmallFontSize());
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
		
		if (validatedObj instanceof Boolean)
			setFont(iconManager.getIconFont(12.0f));
		else
			setFont(defaultFont);
		
		setBackground(UIManager.getColor("Table.background"));
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
			setToolTipText(null);
		} else {
			final String displayText;
			
			if (objEditStr.getErrorText() != null)
				displayText = "#ERR: " + objEditStr.getErrorText();
			else if (validatedObj instanceof Boolean)
				displayText = validatedObj == Boolean.TRUE ? IconManager.ICON_CHECK_SQUARE : IconManager.ICON_SQUARE_O;
			else if (validatedObj instanceof Double){
				final BrowserTableColumnModel model = (BrowserTableColumnModel) table.getColumnModel();
				final String colName = table.getColumnName(column);
				final String formatStr = model.getColumnFormat(colName);
				if (formatStr != null)
					displayText = String.format(formatStr, validatedObj);
				else
					displayText = validatedObj.toString();
			}else
				displayText = validatedObj.toString();
			
			setText(displayText);
			String tooltipText = validatedObj instanceof Boolean ? validatedObj.toString() : displayText;
			
			if (tooltipText.length() > 100)
				setToolTipText(tooltipText.substring(0, 100) + "...");
			else
				setToolTipText(tooltipText);
		}

		// If selected, return
		if (isSelected) {
			if (table.getSelectedColumn() == column && table.getSelectedRow() == row) { // Selected cell
				setBackground(UIManager.getColor("Table.focusCellBackground"));
				setForeground(UIManager.getColor("Table.focusCellForeground"));
			} else {
				setForeground(isError ? LookAndFeelUtil.getErrorColor() : UIManager.getColor("Table.selectionForeground"));
				setBackground(UIManager.getColor("Table.selectionBackground"));
			}
		} else {
			// If non-editable, grey it out.
			if (table.getModel() instanceof BrowserTableModel && !table.isCellEditable(0, column))
				setForeground(UIManager.getColor("TextField.inactiveForeground"));
			else
				setForeground(isError ? LookAndFeelUtil.getErrorColor() : UIManager.getColor("Table.foreground"));
		}
		
		return this;
	}
	
	private static class EquationIcon implements Icon {

		static final int HEIGHT = 8;
		static final int WIDTH = 8;
		
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
			g.setColor(LookAndFeelUtil.getSuccessColor());
			
			final int[] xPoints = new int[] { 0,      0, WIDTH };
			final int[] yPoints = new int[] { HEIGHT, 0, 0 };
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}
}
