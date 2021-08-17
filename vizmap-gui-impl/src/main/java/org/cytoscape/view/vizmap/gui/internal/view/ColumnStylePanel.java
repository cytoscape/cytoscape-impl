package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.createTitledBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Collections;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.util.IconUtil;

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
public class ColumnStylePanel {

	private final ServicesUtil servicesUtil;
	
	private OptionsButton optionsBtn;
	private JComboBox<CyTable> tableCombo;
	private CyColumnComboBox columnCombo;
	private JPanel columnPanel;
	
	public ColumnStylePanel(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}
	
	public JComponent getComponent() {
		return getColumnPanel();
	}
	
	public JPanel getColumnPanel() {
		if (columnPanel == null) {
			columnPanel = new JPanel();
			columnPanel.setOpaque(!isAquaLAF());
			columnPanel.setBorder(createTitledBorder("Apply Style To:"));
			
			// TODO: For some reason, the Styles button is naturally taller than the Options one on Nimbus and Windows.
			//       Let's force it to have the same height.
			getColumnComboBox().setPreferredSize(
					new Dimension(getColumnComboBox().getPreferredSize().width, getOptionsBtn().getOptionsBtn().getPreferredSize().height));
			
			var layout = new GroupLayout(columnPanel);
			columnPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			var tableLbl = new JLabel("Table:");
			var columnLbl = new JLabel("Column:");
			
			layout.setHorizontalGroup(
				layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
							.addComponent(tableLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getTableComboBox(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addGroup(layout.createSequentialGroup()
							.addComponent(columnLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getColumnComboBox(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getOptionsBtn().getOptionsBtn(), PREFERRED_SIZE, 64, PREFERRED_SIZE)
					)
			);
			
			layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(tableLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getTableComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(columnLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getColumnComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getOptionsBtn().getOptionsBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			equalizeSize(tableLbl, columnLbl);
		}
		
		return columnPanel;
	}
	
	JComboBox<CyTable> getTableComboBox() {
		if (tableCombo == null) {
			tableCombo = new JComboBox<>();
			
			var netTableManager = servicesUtil.get(CyNetworkTableManager.class);
			var iconManager = servicesUtil.get(IconManager.class);
			
			var globalTableIcon = new TextIcon(IconManager.ICON_TABLE, iconManager.getIconFont(14.0f), 16, 16);
			
			var iconFont = iconManager.getIconFont(IconUtil.CY_FONT_NAME, 14.0f);
			var nodeTableIcon = new TextIcon(IconUtil.NODE_TABLE, iconFont, 16, 16);
			var edgeTableIcon = new TextIcon(IconUtil.EDGE_TABLE, iconFont, 16, 16);
			var netTableIcon = new TextIcon(IconUtil.NETWORK_TABLE, iconFont, 16, 16);
			
			tableCombo.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					var comp = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					if (value == null) {
						setText("-- None --");
						setIcon(null);
					} else {
						var table = (CyTable) value;
						var text = table.getTitle();
						var icon = globalTableIcon;
						var namespace = netTableManager.getTableNamespace(table);
						var type = netTableManager.getTableType(table);
						
						if (type != null && CyNetwork.DEFAULT_ATTRS.equals(namespace))
							text = "Default " + type.getSimpleName().replace("Cy", "");
						
						if (type == CyNode.class)
							icon = nodeTableIcon;
						else if (type == CyEdge.class)
							icon = edgeTableIcon;
						else if (type == CyNetwork.class)
							icon = netTableIcon;
						
						comp.setText(text);
						comp.setIcon(icon);
					}
					
					return comp;
				}
			});
		}
		
		return tableCombo;
	}
	
	CyColumnComboBox getColumnComboBox() {
		if (columnCombo == null) {
			var columnPresentationManager = servicesUtil.get(CyColumnPresentationManager.class);
			columnCombo = new CyColumnComboBox(columnPresentationManager, Collections.emptyList());
		}
		
		return columnCombo;
	}
	
	OptionsButton getOptionsBtn() {
		if (optionsBtn == null) {
			optionsBtn = new OptionsButton(servicesUtil);
		}
		
		return optionsBtn;
	}
	
	public void updateColumns(Collection<CyTable> tables, CyTable selTable, Collection<CyColumn> columns, CyColumn selColumn) {
		var tableComboBox = getTableComboBox();
		tableComboBox.removeAllItems();
		
		if (tables != null)
			tables.forEach(tableComboBox::addItem);
		if (selTable != null)
			tableComboBox.setSelectedItem(selTable);
		
		var columnComboBox = getColumnComboBox();
		columnComboBox.removeAllItems();
		
		if (columns != null)
			columns.forEach(columnComboBox::addItem);
		if (selColumn != null)
			columnComboBox.setSelectedItem(selColumn);
		
		update();
	}

	private void update() {
		getTableComboBox().setEnabled(getTableComboBox().getItemCount() > 0);
		getColumnComboBox().setEnabled(getColumnComboBox().getItemCount() > 0);
		getOptionsBtn().getOptionsBtn().setEnabled(getColumnComboBox().getItemCount() > 0);
	}
}