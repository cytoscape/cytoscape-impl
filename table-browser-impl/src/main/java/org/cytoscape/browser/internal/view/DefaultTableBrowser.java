package org.cytoscape.browser.internal.view;

import static org.cytoscape.browser.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.browser.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.browser.internal.util.IconUtil;
import org.cytoscape.browser.internal.view.BrowserTableModel.ViewMode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
public class DefaultTableBrowser extends AbstractTableBrowser implements SetCurrentNetworkListener,
		TableAddedListener, TableAboutToBeDeletedListener, ColumnCreatedListener, ColumnDeletedListener {

	private JPopupMenu displayMode;
	private JComboBox<CyTable> tableChooser;
	
	private final Class<? extends CyIdentifiable> objType;

	private BrowserTableModel.ViewMode rowSelectionMode = BrowserTableModel.ViewMode.AUTO;
	private boolean ignoreSetCurrentTable = true;
	
	public DefaultTableBrowser(
			final String tabTitle,
			final Class<? extends CyIdentifiable> objType,
			final CyServiceRegistrar serviceRegistrar,
			final PopupMenuHelper popupMenuHelper
	) {
		super(tabTitle, serviceRegistrar, popupMenuHelper);
		this.objType = objType;

		createPopupMenu();
		
		TableBrowserToolBar toolBar = new TableBrowserToolBar(serviceRegistrar, getTableChooser(), objType);
		setToolBar(toolBar);
		
		toolBar.getSelectionModeButton().addActionListener(e -> {
			DefaultTableBrowser.this.actionPerformed(e);
			displayMode.show(toolBar.getSelectionModeButton(), 0, toolBar.getSelectionModeButton().getHeight());
		});
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
		final JCheckBoxMenuItem displayAuto = new JCheckBoxMenuItem("Auto");
		displayAuto.setSelected(rowSelectionMode == BrowserTableModel.ViewMode.AUTO);
		final JCheckBoxMenuItem displayAll = new JCheckBoxMenuItem("Show all");
		displayAll.setSelected(rowSelectionMode == BrowserTableModel.ViewMode.ALL);
		final JCheckBoxMenuItem displaySelect = new JCheckBoxMenuItem("Show selected");
		displaySelect.setSelected(rowSelectionMode == BrowserTableModel.ViewMode.SELECTED);

		displayAuto.addActionListener(e -> {
			rowSelectionMode = BrowserTableModel.ViewMode.AUTO;
			changeSelectionMode();

			displayAuto.setSelected(true);
			displayAll.setSelected(false);
			displaySelect.setSelected(false);
		});
		
		displayAll.addActionListener(e -> {
			rowSelectionMode = BrowserTableModel.ViewMode.ALL;
			changeSelectionMode();

			displayAuto.setSelected(false);
			displayAll.setSelected(true);
			displaySelect.setSelected(false);
		});
		
		displaySelect.addActionListener(e -> {
			rowSelectionMode = BrowserTableModel.ViewMode.SELECTED;
			changeSelectionMode();
			
			displayAuto.setSelected(false);
			displayAll.setSelected(false);
			displaySelect.setSelected(true);
		});
		
		displayMode.add(displayAuto);
		displayMode.add(displayAll);
		displayMode.add(displaySelect);
	}

	private void changeSelectionMode() {
		final BrowserTable browserTable = getCurrentBrowserTable();
		
		if (browserTable == null)
			return;
		
		final BrowserTableModel model = (BrowserTableModel) browserTable.getModel();
		model.setViewMode(rowSelectionMode);
		model.updateViewMode();
		
		if (rowSelectionMode == ViewMode.ALL && currentTable.getColumn(CyNetwork.SELECTED) != null) {
			// Show the current selected rows
			final Set<Long> suidSelected = new HashSet<>();
			final Set<Long> suidUnselected = new HashSet<>();
			final Collection<CyRow> selectedRows = currentTable.getMatchingRows(CyNetwork.SELECTED, Boolean.TRUE);
	
			for (final CyRow row : selectedRows) {
				suidSelected.add(row.get(CyIdentifiable.SUID, Long.class));
			}
	
			if (!suidSelected.isEmpty())
				browserTable.changeRowSelection(suidSelected, suidUnselected);
		}
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (!ignoreSetCurrentTable) {
			final CyTable table = (CyTable) getTableChooser().getSelectedItem();
			currentTable = table;
			
			final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			
			if (table != null && !table.equals(applicationManager.getCurrentTable())) {
				final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
			
				if (tableManager.getTable(table.getSUID()) != null)
					applicationManager.setCurrentTable(table);
			}
			
			showSelectedTable();
			changeSelectionMode();
		}
	}

	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		final CyNetwork currentNetwork = e.getNetwork();
		
		invokeOnEDTAndWait(() -> {
			if (currentNetwork != null) {
				if (objType == CyNode.class) {
					currentTable = currentNetwork.getDefaultNodeTable();
				} else if (objType == CyEdge.class) {
					currentTable = currentNetwork.getDefaultEdgeTable();
				} else {
					currentTable = currentNetwork.getDefaultNetworkTable();
				}
				currentTableType = objType;
			} else {
				currentTable = null;
				currentTableType = null;
			}
	
			final Set<CyTable> tables = getPublicTables(currentNetwork);
			ignoreSetCurrentTable = true;
			
			try {
				getTableChooser().removeAllItems();
				
				if (currentTable != null) {
					for (final CyTable tbl : tables)
						getTableChooser().addItem(tbl);
					
					getToolBar().updateEnableState(getTableChooser());
					getTableChooser().setSelectedItem(currentTable);
				}
			} finally {
				ignoreSetCurrentTable = false;
			}
			
			serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(currentTable);
			
			showSelectedTable();
			changeSelectionMode();
		});
	}
	
	@Override
	public void handleEvent(final TableAddedEvent e) {
		final CyTable newTable = e.getTable();

		if (newTable.isPublic() || showPrivateTables()) {
			final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			final CyNetworkTableManager netTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
			
			final CyNetwork curNet = applicationManager.getCurrentNetwork();
			
			if (curNet != null && netTableManager.getTables(curNet, objType).containsValue(newTable)) {
				invokeOnEDT(() -> {
					if (((DefaultComboBoxModel<CyTable>)getTableChooser().getModel()).getIndexOf(newTable) < 0) {
						getTableChooser().addItem(newTable);
						getToolBar().updateEnableState(getTableChooser());
					}
				});
			}
		}
	}
	
	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
		final BrowserTable table = getBrowserTable(cyTable);
		
		if (table != null) {
			((DefaultComboBoxModel<CyTable>)getTableChooser().getModel()).removeElement(cyTable);
			
			// We need this to happen synchronously or we get royally messed up by the new table selection
			invokeOnEDTAndWait(() -> {
				getToolBar().updateEnableState(getTableChooser());
				removeTable(cyTable);
			});
		}
	}
	
	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		if (e.getSource() == currentTable)
			getToolBar().updateEnableState();
	}

	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		if (e.getSource() == currentTable)
			getToolBar().updateEnableState();
	}
	
	private JComboBox<CyTable> getTableChooser() {
		if (tableChooser == null) {
			tableChooser = new JComboBox<>(new DefaultComboBoxModel<CyTable>());
			tableChooser.setRenderer(new TableChooserCellRenderer());
			tableChooser.addActionListener(this);
			final Dimension d = new Dimension(SELECTOR_WIDTH, tableChooser.getPreferredSize().height);
			tableChooser.setMaximumSize(d);
			tableChooser.setMinimumSize(d);
			tableChooser.setPreferredSize(d);
			tableChooser.setSize(d);
			// Table selector is invisible unless it has more than one item
			tableChooser.setVisible(false);
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
		
		return tableChooser;
	}
	
	private Set<CyTable> getPublicTables(CyNetwork currentNetwork) {
		final Set<CyTable> tables = new LinkedHashSet<>();
		if (currentNetwork == null) return tables;
		
		final CyNetworkTableManager netTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		final Map<String, CyTable> map = netTableManager.getTables(currentNetwork, objType);
		
		if (showPrivateTables()) {
			tables.addAll(map.values());
		} else {
			for (final CyTable tbl : map.values()) {
				if (tbl.isPublic())
					tables.add(tbl);
			}
		}
		
		return tables;
	}
}
