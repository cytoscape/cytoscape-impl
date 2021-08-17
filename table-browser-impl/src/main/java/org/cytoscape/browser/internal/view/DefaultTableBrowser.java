package org.cytoscape.browser.internal.view;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.browser.internal.util.IconUtil;
import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.browser.internal.view.tools.AbstractToolBarControl;
import org.cytoscape.browser.internal.view.tools.GeneralOptionsControl;
import org.cytoscape.browser.internal.view.tools.RowHeightControl;
import org.cytoscape.browser.internal.view.tools.ViewModeControl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.presentation.property.table.TableMode;
import org.cytoscape.view.presentation.property.table.TableModeVisualProperty;

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

@SuppressWarnings("serial")
public class DefaultTableBrowser extends AbstractTableBrowser {

	private JPopupMenu displayMode;
	private JComboBox<CyTable> tableChooser;
	
	private ViewModeControl viewModeControl;
	
	private boolean ignoreSetCurrentTable = true;
	
	public DefaultTableBrowser(
			String tabTitle,
			Class<? extends CyIdentifiable> objType,
			CyServiceRegistrar serviceRegistrar
	) {
		super(tabTitle, objType, serviceRegistrar);
		
		var controls = new ArrayList<AbstractToolBarControl>();
		
		if (objType != CyNetwork.class)
			controls.add(viewModeControl = new ViewModeControl(serviceRegistrar));
		
		controls.add(new RowHeightControl(serviceRegistrar));
		controls.add(new GeneralOptionsControl(objType, serviceRegistrar));
		
		getOptionsBar().setFormatControls(controls);

		createPopupMenu();
	}
	
	@Override
	public String getIdentifier() {
		return "org.cytoscape." + objType.getSimpleName().replace("Cy", "") + "Tables";
	}
	
	private TableMode getTableMode() {
		var renderer = getCurrentRenderer();
		
		return renderer == null ? TableRenderer.getDefaultTableMode() : renderer.getTableMode();
	}
	
	private void setTableMode(TableMode mode) {
		var renderer = getCurrentRenderer();
		renderer.setTableMode(mode);
	}
	
	@Override
	public Icon getIcon() {
		if (icon == null) {
			String text = null;
			
			if (objType == CyNode.class)
				text = IconUtil.NODE_TABLE;
			else if (objType == CyEdge.class)
				text = IconUtil.EDGE_TABLE;
			else if (objType == CyNetwork.class)
				text = IconUtil.NETWORK_TABLE;
			
			if (text != null)
				icon = new TextIcon(text,
						serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 14.0f), 16, 16);
			else
				return super.getIcon();
		}
		
		return icon;
	}
	
	private void createPopupMenu() {
		displayMode = new JPopupMenu();
		var tableMode = getTableMode();
		var displayAuto = new JCheckBoxMenuItem("Auto");
		displayAuto.setSelected(tableMode == TableModeVisualProperty.AUTO);
		var displayAll = new JCheckBoxMenuItem("Show all");
		displayAll.setSelected(tableMode == TableModeVisualProperty.ALL);
		var displaySelect = new JCheckBoxMenuItem("Show selected");
		displaySelect.setSelected(tableMode == TableModeVisualProperty.SELECTED);

		displayAuto.addActionListener(e -> {
			setTableMode(TableModeVisualProperty.AUTO);
			displayAuto.setSelected(true);
			displayAll.setSelected(false);
			displaySelect.setSelected(false);
		});
		
		displayAll.addActionListener(e -> {
			setTableMode(TableModeVisualProperty.ALL);
			displayAuto.setSelected(false);
			displayAll.setSelected(true);
			displaySelect.setSelected(false);
		});
		
		displaySelect.addActionListener(e -> {
			setTableMode(TableModeVisualProperty.SELECTED);
			displayAuto.setSelected(false);
			displayAll.setSelected(false);
			displaySelect.setSelected(true);
		});
		
		displayMode.add(displayAuto);
		displayMode.add(displayAll);
		displayMode.add(displaySelect);
	}

	public void setCurrentTable() {
		if (!ignoreSetCurrentTable) {
			var table = (CyTable) getTableChooser().getSelectedItem();
			setCurrentTable(table);
			
			showSelectedTable();
			
			// View Mode can only work if the current table's PK is a Long, which means it may be the node or edge
			// SUID, required for the synchronization with node/edge selection.
			if (viewModeControl != null && table != null)
				viewModeControl.setVisible(table.getPrimaryKey().getType() == Long.class);
			
			// Make sure the Application Manager has our current table
			var applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			
			if (table != null && !table.equals(applicationManager.getCurrentTable())) {
				var tableManager = serviceRegistrar.getService(CyTableManager.class);
			
				if (tableManager.getTable(table.getSUID()) != null)
					applicationManager.setCurrentTable(table);
			}
		}
	}

	public void update(CyNetwork network) {
		if (network != null) {
			if (objType == CyNode.class)
				setCurrentTable(network.getDefaultNodeTable());
			else if (objType == CyEdge.class)
				setCurrentTable(network.getDefaultEdgeTable());
			else
				setCurrentTable(network.getDefaultNetworkTable());
			
			currentTableType = objType;
		} else {
			setCurrentTable(null);
			currentTableType = null;
		}

		var tables = getPublicTables(network);
		ignoreSetCurrentTable = true;
		
		try {
			getTableChooser().removeAllItems();
			
			if (currentTable != null) {
				for (var tbl : tables)
					getTableChooser().addItem(tbl);
				
				getTableChooser().setSelectedItem(currentTable);
			}
		} finally {
			ignoreSetCurrentTable = false;
		}
		
		showSelectedTable();
	}
	
	@Override
	protected JComboBox<CyTable> getTableChooser() {
		if (tableChooser == null) {
			tableChooser = new JComboBox<>(new DefaultComboBoxModel<CyTable>());
			tableChooser.setRenderer(new TableChooserCellRenderer(serviceRegistrar));
			tableChooser.setMaximumSize(new Dimension(600, tableChooser.getPreferredSize().height));
			tableChooser.setVisible(false); // Table selector is invisible unless it has more than one item
			tableChooser.addActionListener(e -> setCurrentTable());
			tableChooser.getModel().addListDataListener(new ListDataListener() {
				@Override
				public void intervalRemoved(ListDataEvent e) {
					setVisible();
				}
				@Override
				public void intervalAdded(ListDataEvent e) {
					setVisible();
				}
				@Override
				public void contentsChanged(ListDataEvent e) {
				}
				private void setVisible() {
					tableChooser.setVisible(tableChooser.getItemCount() > 1);
				}
			});
		}
		
		// This avoids a memory leak on Mac
		tableChooser.getEditor().setItem("");
		
		return tableChooser;
	}
	
	private Set<CyTable> getPublicTables(CyNetwork network) {
		var tables = new LinkedHashSet<CyTable>();
		
		if (network == null)
			return tables;

		var netTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		var map = netTableManager.getTables(network, objType);
		
		if (TableBrowserUtil.isShowPrivateTables(serviceRegistrar)) {
			tables.addAll(map.values());
		} else {
			for (var tbl : map.values()) {
				if (tbl.isPublic())
					tables.add(tbl);
			}
		}
		return tables;
	}
}
