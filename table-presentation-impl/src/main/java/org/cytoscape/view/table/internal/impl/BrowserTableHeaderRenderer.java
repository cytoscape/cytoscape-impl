package org.cytoscape.view.table.internal.impl;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.table.internal.util.ViewUtil;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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
final class BrowserTableHeaderRenderer extends JPanel implements TableCellRenderer {

	private static final int SELECTION_WIDTH = 2;
	
	private final JLabel namespaceLabel;
	private final JLabel namespaceIconLabel;
	private final JLabel nameLabel;
	private final JLabel pkLabel;
	private final JLabel sharedLabel;
	private final JLabel immutableLabel;
	private final JLabel sortLabel;
	
	private boolean isColumnSelected;
	
	private final Color defBgColor;
	private final Color selBgColor;
	
	private final CyServiceRegistrar serviceRegistrar;

	BrowserTableHeaderRenderer(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		var iconManager = serviceRegistrar.getService(IconManager.class);
		
		defBgColor = ViewUtil.getDefaultTableHeaderBg();
		selBgColor = ViewUtil.getSelectedTableHeaderBg();
		
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		
		namespaceLabel = new JLabel();
		namespaceLabel.setFont(UIManager.getFont("TableHeader.font"));
		namespaceLabel.setHorizontalAlignment(JLabel.CENTER);
		namespaceLabel.setForeground(UIManager.getColor("TableHeader.foreground"));
		
		namespaceIconLabel = new JLabel();
		
		nameLabel = new JLabel();
		nameLabel.setFont(UIManager.getFont("TableHeader.font"));
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		nameLabel.setForeground(UIManager.getColor("TableHeader.foreground"));
		
		pkLabel = new JLabel(IconManager.ICON_KEY);
		pkLabel.setFont(iconManager.getIconFont(12.0f));
		pkLabel.setForeground(UIManager.getColor("TextField.inactiveForeground"));
		
		sharedLabel = new JLabel(IconManager.ICON_SITEMAP);
		sharedLabel.setFont(iconManager.getIconFont(12.0f));
		sharedLabel.setForeground(UIManager.getColor("TextField.inactiveForeground"));
		
		immutableLabel = new JLabel(IconManager.ICON_LOCK);
		immutableLabel.setFont(iconManager.getIconFont(14.0f));
		immutableLabel.setForeground(UIManager.getColor("TextField.inactiveForeground"));
		
		sortLabel = new JLabel(IconManager.ICON_ANGLE_UP);
		sortLabel.setFont(iconManager.getIconFont(12.0f));
		sortLabel.setMinimumSize(sortLabel.getPreferredSize());
		sortLabel.setSize(sortLabel.getPreferredSize());
		
		var panel = new JPanel();
		panel.setOpaque(false);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.SOUTH);
		
		var layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(namespaceIconLabel)
				.addGroup(layout.createSequentialGroup()
						.addComponent(pkLabel)
						.addComponent(sharedLabel)
						.addComponent(immutableLabel)
				)
			)
			.addGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(namespaceLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(nameLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			)
			.addComponent(sortLabel)
			.addGap(4)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(LEADING, false)
				.addComponent(namespaceIconLabel)
				.addComponent(namespaceLabel)
			)
			.addGap(2)
			.addGroup(layout.createParallelGroup(LEADING, false)
				.addComponent(pkLabel)
				.addComponent(sharedLabel)
				.addComponent(immutableLabel)
				.addComponent(nameLabel)
				.addComponent(sortLabel)
			)
			.addGap(2)
		);
	}

	private Icon getNamespaceIcon(String namespace) {
		var presentationManager = serviceRegistrar.getService(CyColumnPresentationManager.class);
		var presentation = presentationManager.getColumnPresentation(namespace);
		
		if (presentation == null)
			return null;
		
		var icon = presentation.getNamespaceIcon();
		
		if (icon == null)
			return null;
 		
 		return IconManager.resizeIcon(icon, 16);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int col) {
		// 'value' is column header value of column 'col'
		// rowIndex is always -1
		// isSelected is always false
		// hasFocus is always false
		
		isColumnSelected = Arrays.binarySearch(table.getColumnModel().getSelectedColumns(), col) >= 0;
		
		var isAllRowsSelected = table.getSelectedRowCount() == table.getRowCount();
		setBackground(isColumnSelected && isAllRowsSelected ? selBgColor : defBgColor);

		// Configure the component with the specified value
		var colName = value != null ? value.toString() : "";
		
		var parts = CyColumn.splitColumnName(colName);
		var namespace = parts[0];
		
		if (namespace == null) {
			namespaceLabel.setVisible(false);
			namespaceIconLabel.setVisible(false);
		} else {
			namespaceLabel.setVisible(true);
			namespaceLabel.setText(namespace);

			var icon = getNamespaceIcon(namespace);

			if (icon == null) {
				namespaceIconLabel.setVisible(false);
			} else {
				namespaceIconLabel.setVisible(true);
				namespaceIconLabel.setIcon(icon);
			}
		}

		var font = nameLabel.getFont();
		nameLabel.setFont(colName.equals(CyIdentifiable.SUID) ? font.deriveFont(Font.BOLD) : font.deriveFont(Font.PLAIN));
		nameLabel.setText(parts[1]);
		
		pkLabel.setVisible(false);
		sharedLabel.setVisible(false);
		immutableLabel.setVisible(false);
		
		sortLabel.setText(" ");
		sortLabel.setForeground(UIManager.getColor("TextField.inactiveForeground"));
		
		setToolTipText(colName);
		
		if (!(table.getModel() instanceof BrowserTableModel)) {
			invalidate();
			return this;
		}

		var model = (BrowserTableModel) table.getModel();
		var column = model.getDataTable().getColumn(colName);
		
		if (column != null) {
			var toolTip = new StringBuilder("<html><div style='text-align: center;'>");
	
			if (colName.equals(CyIdentifiable.SUID))
				toolTip.append("Session-Unique ID");
			else if (column.getType() == List.class)
				toolTip.append("<b>").append(column.getName())
					.append("</b><br /><font face='monospace'>(List of ")
					.append(getMinimizedType(column.getListElementType().getName()))
					.append("s)</font>");
			else
				toolTip.append("<b>").append(column.getName())
					.append("</b><br /><font face='monospace'>(")
					.append(getMinimizedType(column.getType().getName()))
					.append(")</font>");
			
			toolTip.append("</div>");
			
			if (column.getVirtualColumnInfo().isVirtual() || column.isImmutable() || column.isPrimaryKey()) {
				toolTip.append("<hr noshade />");
				
				if (column.isPrimaryKey()) {
					toolTip.append("<p>- <i>Primary Key</i></p>");
					pkLabel.setVisible(true);
				}
				
				if (column.getVirtualColumnInfo().isVirtual()) {
					toolTip.append("<p>- <i>Network Collection Column</i></p>");
					sharedLabel.setVisible(true);
				}
				
				if (column.isImmutable() || column.isPrimaryKey()) {
					toolTip.append("<p>- Column cannot be: ");
					
					if (column.isImmutable()) {
						toolTip.append("deleted, renamed");
						
						if (column.isPrimaryKey())
							toolTip.append(",");
						
						immutableLabel.setVisible(true);
					}
					
					if (column.isPrimaryKey())
						toolTip.append(" edited");
					
					toolTip.append("</p>");
				}
			}
			
			toolTip.append("</html>");
	
			// Set tool tip if desired
			setToolTipText(toolTip.toString());
	
			//*****sorting icon**
			int index = -1;
			boolean ascending = true;
			
			var rowSorter = table.getRowSorter();
			int modelColumn = col >= 0 && col < table.getColumnCount() ? table.convertColumnIndexToModel(col) : -1;
			var sortKeys = rowSorter.getSortKeys();
			
			if (sortKeys.size() > 0) {
				var key = sortKeys.get(0);
				
				if (key.getColumn() == modelColumn) {
					index = col;
					ascending = key.getSortOrder() == SortOrder.ASCENDING;
				}
			}
			
			if (col == index)
				sortLabel.setText(ascending ? IconManager.ICON_ANGLE_UP : IconManager.ICON_ANGLE_DOWN);
			
			if (col == index)
				sortLabel.setForeground(UIManager.getColor("TableHeader.foreground"));
		}

		invalidate();
		
		return this;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if (isColumnSelected) {
			var g2 = (Graphics2D) g.create();
			g2.setColor(UIManager.getColor("Table.focusCellBackground"));
			
			var w = getWidth();
			var h = getHeight();
			g2.fillRect(0, h - SELECTION_WIDTH, w, SELECTION_WIDTH);
			
			g2.dispose();
		}
	}
	
	private String getMinimizedType(String type) {
		return type.substring(type.lastIndexOf('.') + 1);
	}
}
