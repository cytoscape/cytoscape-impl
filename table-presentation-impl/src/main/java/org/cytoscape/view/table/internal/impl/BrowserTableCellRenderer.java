package org.cytoscape.view.table.internal.impl;

import java.awt.Color;
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

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.presentation.property.table.CellFormat;
import org.cytoscape.view.table.internal.util.ValidatedObjectAndEditString;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

/** Cell renderer for attribute browser table. */
class BrowserTableCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -4364566217397320318L;

	// Define fonts & colors for the cells
	private static final int H_PAD = 8;
	private static final int V_PAD = 2;
	private static EquationIcon EQUATION_ICON = new EquationIcon();
	
	private final BrowserTablePresentation presentation;
	

	public BrowserTableCellRenderer(CyServiceRegistrar serviceRegistrar) {
		presentation = new BrowserTablePresentation(serviceRegistrar, getFont());
		
		setOpaque(true);

		// Add padding:
		Border border = getBorder();
		if (border == null)
			border = BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD);
		else
			border = BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD));
		setBorder(border);
	}
	

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
		final ValidatedObjectAndEditString objEditStr = (ValidatedObjectAndEditString) value;
		final Object validatedObj = objEditStr != null ? objEditStr.getValidatedObject() : null;

		BrowserTable browserTable = (BrowserTable) table;
		BrowserTableModel model = (BrowserTableModel) browserTable.getModel();
		CyTableView tableView = model.getTableView();
		CyColumn col = model.getColumnByModelIndex(browserTable.convertColumnIndexToModel(colIndex));
		CyRow row = model.getCyRow(browserTable.convertRowIndexToModel(rowIndex));
		
		CyColumnView colView = (CyColumnView) tableView.getColumnView(col);
		
		Color background = presentation.getBackgroundColor(row, colView);
		Color foreground = presentation.getForegroundColor(row, colView);
		Font font = presentation.getFont(row, colView, validatedObj);
	
		setBackground(background);
		setFont(font);
		
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
			else if (validatedObj instanceof Double) {
				String formatStr = null;
				CellFormat format = colView.getVisualProperty(BasicTableVisualLexicon.COLUMN_FORMAT);
				if(format != null) {
					formatStr = format.getFormat();
				}

				if (formatStr == null || formatStr.isBlank())
					displayText = validatedObj.toString();
				else
					displayText = String.format(formatStr, validatedObj);
			} else {
				displayText = validatedObj.toString();
			}

			setText(displayText);
			String tooltipText = validatedObj instanceof Boolean ? validatedObj.toString() : displayText;

			setToolTipText(tooltipText);
		}

		// If selected, return
		if (isSelected) {
			if (table.getSelectedColumn() == colIndex && table.getSelectedRow() == rowIndex) { // Selected
				setBackground(UIManager.getColor("Table.focusCellBackground"));
				setForeground(UIManager.getColor("Table.focusCellForeground"));
			} else {
				setForeground( isError ? LookAndFeelUtil.getErrorColor() : UIManager.getColor("Table.selectionForeground"));
				setBackground(UIManager.getColor("Table.selectionBackground"));
			}
		} else {
			// If non-editable, grey it out.
			if (table.getModel() instanceof BrowserTableModel && !table.isCellEditable(0, colIndex))
				setForeground(UIManager.getColor("TextField.inactiveForeground"));
			else
				setForeground(isError ? LookAndFeelUtil.getErrorColor() : foreground);
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

			final int[] xPoints = new int[] { 0, 0, WIDTH };
			final int[] yPoints = new int[] { HEIGHT, 0, 0 };
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}
}
