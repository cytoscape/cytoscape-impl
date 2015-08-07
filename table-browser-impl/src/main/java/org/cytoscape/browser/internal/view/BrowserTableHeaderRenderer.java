package org.cytoscape.browser.internal.view;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

final class BrowserTableHeaderRenderer extends JPanel implements TableCellRenderer {

	private static final long serialVersionUID = 4656466166588715282L;

	private final Border AQUA_BORDER =
			BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground"));
	private final Border WIN_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(236, 236, 236)),
			BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(225, 238, 250)));
	private final Border NIMBUS_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(97, 102, 109));

	private final JLabel nameLabel;
	private final JLabel sharedLabel;
	private final JLabel sortLabel;

	BrowserTableHeaderRenderer(final IconManager iconManager) {
		if (LookAndFeelUtil.isAquaLAF()) {
			setBorder(AQUA_BORDER);
		} else if (LookAndFeelUtil.isWinLAF()) {
			setBorder(WIN_BORDER);
			setBackground(new Color(251, 251, 251));
		} else {
			setBorder(NIMBUS_BORDER);
		}
		
		nameLabel = new JLabel();
		nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.INFO_FONT_SIZE));
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		
		sharedLabel = new JLabel();
		sharedLabel.setFont(iconManager.getIconFont(12.0f));
		sharedLabel.setForeground(SystemColor.textInactiveText);
		
		sortLabel = new JLabel();
		sortLabel.setFont(iconManager.getIconFont(10.0f));
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(2)
				.addComponent(sharedLabel)
				.addComponent(nameLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(sortLabel)
				.addGap(6)
		);
		layout.setVerticalGroup(layout.createParallelGroup(LEADING, false)
				.addComponent(sharedLabel)
				.addGroup(layout.createSequentialGroup()
						.addGap(4)
						.addGroup(layout.createParallelGroup(CENTER, false)
								.addComponent(nameLabel)
								.addComponent(sortLabel)
						)
						.addGap(4)
				)
		);
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, boolean isSelected,
			boolean hasFocus, int row, int col) {
		// 'value' is column header value of column 'col'
		// rowIndex is always -1
		// isSelected is always false
		// hasFocus is always false

		// Configure the component with the specified value
		final String colName = value != null ? value.toString() : "";
		
		final Font font = nameLabel.getFont();
		nameLabel.setFont(colName.equals(CyIdentifiable.SUID) ? font.deriveFont(Font.BOLD) : font.deriveFont(Font.PLAIN));
		nameLabel.setText(colName);
		
		sharedLabel.setText("");
		
		sortLabel.setText(IconManager.ICON_SORT);
		sortLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		
		setToolTipText(colName);
		
		if (!(table.getModel() instanceof BrowserTableModel)) {
			invalidate();
			return this;
		}

		final BrowserTableModel model = (BrowserTableModel) table.getModel();
		final CyColumn column = model.getDataTable().getColumn(colName);
		
		if (column != null) {
			String toolTip = "<html><div style=\"text-align: center;\">";
	
			if (colName.equals(CyIdentifiable.SUID))
				toolTip += "Session-Unique ID (Primary Key)<br />This column is uneditable";
			else if (column.getType() == List.class)
				toolTip += "<b>" + column.getName() + "</b><br />(List of "+ getMinimizedType(column.getListElementType().getName()) + "s)";
			else
				toolTip += "<b>" + column.getName()+ "</b><br />(" + getMinimizedType(column.getType().getName()) + ")";
			
			if (column.getVirtualColumnInfo().isVirtual()) {
				toolTip += "<br /><i>Network Collection Column</i></html>";
				sharedLabel.setText(ColumnSelector.SHARED_COL_ICON_TEXT);
			} else {
				toolTip +="</div></html>";
			}
	
			// Set tool tip if desired
			setToolTipText(toolTip);
	
			//*****sorting icon**
			int index = -1;
			boolean ascending = true;
			
			RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
			int modelColumn = table.convertColumnIndexToModel(col);
			List<? extends SortKey> sortKeys = rowSorter.getSortKeys();
			
			if (sortKeys.size() > 0) {
				SortKey key = sortKeys.get(0);
				
				if (key.getColumn() == modelColumn) {
					index = col;
					ascending = key.getSortOrder() == SortOrder.ASCENDING;
				}
			}
			
			final String iconTxt = ascending ? IconManager.ICON_SORT_ASC : IconManager.ICON_SORT_DESC;
			sortLabel.setText(col == index ? iconTxt : IconManager.ICON_SORT);
			
			if (col == index)
				sortLabel.setForeground(UIManager.getColor("Label.foreground"));
		}

		invalidate();
		
		return this;
	}
	
	private String getMinimizedType (String type){
		return type.substring(type.lastIndexOf('.')+1);
	}
}
