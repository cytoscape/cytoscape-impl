package org.cytoscape.view.table.internal.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.table.internal.cg.NullCellCustomGraphics;
import org.cytoscape.view.table.internal.util.ValidatedObjectAndEditString;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

/** Cell renderer for attribute browser table. */
@SuppressWarnings("serial")
class BrowserTableCellRenderer extends JPanel implements TableCellRenderer {

	// Define fonts & colors for the cells
	private static final int H_PAD = 8;
	private static final int V_PAD = 2;
	private static EquationIcon EQUATION_ICON = new EquationIcon();
	
	private final JLabel label;
	
	private CyColumn col;
	private CyRow row;
	private CellCustomGraphics cg;
	
	private final DefaultCellRenderer defCellRenderer = new DefaultCellRenderer();
	private final BrowserTablePresentation presentation;
	
	public BrowserTableCellRenderer(CyServiceRegistrar serviceRegistrar) {
		presentation = new BrowserTablePresentation(serviceRegistrar, defCellRenderer.getFont());
		
		label = new JLabel();
		label.setOpaque(false);
		
		setLayout(new BorderLayout());
		add(label, BorderLayout.CENTER);
		
		// Add padding:
		var border = defCellRenderer.getBorder();
		
		if (border == null)
			border = BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD);
		else
			border = BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD));
		
		setBorder(border);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int colIndex) {
		if (colIndex < 0 || colIndex >= table.getColumnCount())
			return this;
		
		var objEditStr = (ValidatedObjectAndEditString) value;
		var validatedObj = objEditStr != null ? objEditStr.getValidatedObject() : null;
		
		var browserTable = (BrowserTable) table;
		var model = (BrowserTableModel) browserTable.getModel();
		var tableView = model.getTableView();
		col = model.getColumnByModelIndex(browserTable.convertColumnIndexToModel(colIndex));
		row = model.getCyRow(browserTable.convertRowIndexToModel(rowIndex));
		
		var colView = (CyColumnView) tableView.getColumnView(col);
		
		var bg = presentation.getBackgroundColor(row, colView);
		var fg = presentation.getForegroundColor(row, colView);
		var font = presentation.getFont(row, colView, validatedObj);
	
		label.setFont(font);
		label.setIcon(objEditStr != null && objEditStr.isEquation() ? EQUATION_ICON : null);
		label.setVerticalTextPosition(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);

		if (validatedObj instanceof Boolean)
			label.setHorizontalAlignment(JLabel.CENTER);
		else
			label.setHorizontalAlignment(validatedObj instanceof Number ? JLabel.RIGHT : JLabel.LEFT);

		String text = null;
		String tooltip = null;
		
		boolean isError = objEditStr != null && objEditStr.getErrorText() != null;
		
		// First, set values
		if (objEditStr == null || (objEditStr.getValidatedObject() == null && objEditStr.getErrorText() == null)) {
			text = "";
		} else {
			if (objEditStr.getErrorText() != null) {
				text = "#ERR: " + objEditStr.getErrorText();
			} else if (validatedObj instanceof Boolean) {
				text = validatedObj == Boolean.TRUE ? IconManager.ICON_CHECK_SQUARE : IconManager.ICON_SQUARE_O;
			} else if (validatedObj instanceof Double) {
				String formatStr = null;
				var format = colView.getVisualProperty(BasicTableVisualLexicon.COLUMN_FORMAT);
				
				if (format != null)
					formatStr = format.getFormat();

				if (formatStr == null || formatStr.isBlank())
					text = validatedObj.toString();
				else
					text = String.format(formatStr, validatedObj);
			} else {
				text = validatedObj.toString();
			}

			tooltip = validatedObj instanceof Boolean ? validatedObj.toString() : text;
			
			if (isSelected) {
				if (table.getSelectedColumn() == colIndex && table.getSelectedRow() == rowIndex) { // Selected
					bg = UIManager.getColor("Table.focusCellBackground");
					fg = UIManager.getColor("Table.focusCellForeground");
				} else {
					bg = UIManager.getColor("Table.selectionBackground");
					fg = isError ? LookAndFeelUtil.getErrorColor() : UIManager.getColor("Table.selectionForeground");
				}
			} else {
				// If non-editable, grey it out.
				if (table.getModel() instanceof BrowserTableModel && !table.isCellEditable(0, colIndex))
					fg = UIManager.getColor("TextField.inactiveForeground");
				else
					fg = isError ? LookAndFeelUtil.getErrorColor() : fg;
			}
		}
		
		// Save the custom graphics
		cg = presentation.getCustomGraphics(row, colView);
		
		setBackground(bg);
		setForeground(fg);
		label.setForeground(fg);
		
		label.setText(text);
		label.setToolTipText(tooltip);
		setToolTipText(tooltip);

		return this;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Draw custom graphics (sparklines, etc)
		if (cg != null && !cg.equals(NullCellCustomGraphics.getNullObject())) {
			var w = getWidth();
			var h = getHeight();
			var bounds = new Rectangle2D.Float(0, 0, w, h);
			cg.draw(g, bounds, col, row);
		}
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
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(LookAndFeelUtil.getSuccessColor());

			int[] xPoints = new int[] { 0, 0, WIDTH };
			int[] yPoints = new int[] { HEIGHT, 0, 0 };
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}
}
