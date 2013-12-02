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

import static org.cytoscape.browser.internal.IconManager.ICON_COG;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.browser.internal.BrowserTableModel.ViewMode;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
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
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;


public class DefaultTableBrowser extends AbstractTableBrowser implements SetCurrentNetworkListener,
		TableAddedListener, TableAboutToBeDeletedListener, ColumnCreatedListener, ColumnDeletedListener {

	private static final long serialVersionUID = 627394119637512735L;

	private JButton selectionModeButton;
	private JPopupMenu displayMode;
	private JComboBox tableChooser;
	
	private final Class<? extends CyIdentifiable> objType;
	private final CyNetworkTableManager networkTableManager;

	private BrowserTableModel.ViewMode rowSelectionMode = BrowserTableModel.ViewMode.AUTO;
	private boolean ignoreSetCurrentTable = true;

	public DefaultTableBrowser(final String tabTitle,
							   final Class<? extends CyIdentifiable> objType,
							   final CyTableManager tableManager,
							   final CyNetworkTableManager networkTableManager,
							   final CyServiceRegistrar serviceRegistrar,
							   final EquationCompiler compiler,
							   final CyNetworkManager networkManager,
							   final DeleteTableTaskFactory deleteTableTaskFactory,
							   final DialogTaskManager guiTaskManager,
							   final PopupMenuHelper popupMenuHelper,
							   final CyApplicationManager applicationManager,
							   final CyEventHelper eventHelper,
							   final IconManager iconManager) {//, final MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryService) {
		super(tabTitle, tableManager, serviceRegistrar, compiler, networkManager,
				deleteTableTaskFactory, guiTaskManager, popupMenuHelper, applicationManager, eventHelper);

		this.objType = objType;
		this.networkTableManager = networkTableManager;

		createPopupMenu();
		
		if (objType != CyNetwork.class) {
			selectionModeButton = new JButton(ICON_COG);
			selectionModeButton.setToolTipText("Change Table Mode");
			AttributeBrowserToolBar.styleButton(selectionModeButton,
					iconManager.getIconFont(AttributeBrowserToolBar.ICON_FONT_SIZE * 4/5));
			
			selectionModeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					DefaultTableBrowser.this.actionPerformed(e);
					displayMode.show(selectionModeButton, 0, selectionModeButton.getHeight());
				}
			});
			
			attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler, deleteTableTaskFactory,
					guiTaskManager, getTableChooser(), selectionModeButton, objType, applicationManager, iconManager);// , mapGlobalTableTaskFactoryService);
		} else {
			attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler, deleteTableTaskFactory,
					guiTaskManager, getTableChooser(), objType, applicationManager, iconManager);
		}
		
		add(attributeBrowserToolBar, BorderLayout.NORTH);
	}
	
	private void createPopupMenu() {
		displayMode = new JPopupMenu();
		final JCheckBoxMenuItem displayAuto = new JCheckBoxMenuItem("Auto");
		displayAuto.setSelected(rowSelectionMode == BrowserTableModel.ViewMode.AUTO);
		final JCheckBoxMenuItem displayAll = new JCheckBoxMenuItem("Show all");
		displayAll.setSelected(rowSelectionMode == BrowserTableModel.ViewMode.ALL);
		final JCheckBoxMenuItem displaySelect = new JCheckBoxMenuItem("Show selected");
		displaySelect.setSelected(rowSelectionMode == BrowserTableModel.ViewMode.SELECTED);

		displayAuto.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rowSelectionMode = BrowserTableModel.ViewMode.AUTO;
				changeSelectionMode();

				displayAuto.setSelected(true);
				displayAll.setSelected(false);
				displaySelect.setSelected(false);
			}
		});
		
		displayAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rowSelectionMode = BrowserTableModel.ViewMode.ALL;
				changeSelectionMode();

				displayAuto.setSelected(false);
				displayAll.setSelected(true);
				displaySelect.setSelected(false);
			}
		});
		
		displaySelect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rowSelectionMode = BrowserTableModel.ViewMode.SELECTED;
				changeSelectionMode();
				
				displayAuto.setSelected(false);
				displayAll.setSelected(false);
				displaySelect.setSelected(true);
			}
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
		
		if (rowSelectionMode == ViewMode.ALL) {
			// Show the current selected rows
			final Set<Long> suidSelected = new HashSet<Long>();
			final Set<Long> suidUnselected = new HashSet<Long>();
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
			
			if (table != null
					&& !table.equals(applicationManager.getCurrentTable())
					&& tableManager.getTable(table.getSUID()) != null) {
				applicationManager.setCurrentTable(table);
			}
			
			showSelectedTable();
			changeSelectionMode();
		}
	}

	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		final CyNetwork currentNetwork = e.getNetwork();
		
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
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ignoreSetCurrentTable = true;
				
				try {
					getTableChooser().removeAllItems();
					
					if (currentTable != null) {
						for (final CyTable tbl : tables)
							getTableChooser().addItem(tbl);
						
						attributeBrowserToolBar.updateEnableState(getTableChooser());
						getTableChooser().setSelectedItem(currentTable);
					}
				} finally {
					ignoreSetCurrentTable = false;
				}
			}
		});
		
		showSelectedTable();
		changeSelectionMode();
	}
	
	@Override
	public void handleEvent(final TableAddedEvent e) {
		final CyTable newTable = e.getTable();

		if (newTable.isPublic()) {
			final CyNetwork curNet = applicationManager.getCurrentNetwork();
			
			if (curNet != null && networkTableManager.getTables(curNet, objType).containsValue(newTable)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (((DefaultComboBoxModel)getTableChooser().getModel()).getIndexOf(newTable) < 0) {
							getTableChooser().addItem(newTable);
							attributeBrowserToolBar.updateEnableState(getTableChooser());
						}
					}
				});
			}
		}
	}
	
	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
		final BrowserTable table = getAllBrowserTablesMap().get(cyTable);
		
		if (table != null) {
			((DefaultComboBoxModel)getTableChooser().getModel()).removeElement(cyTable);
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					attributeBrowserToolBar.updateEnableState(getTableChooser());
					deleteTable(cyTable);
				}
			});
			
			final CyNetwork network = networkTableManager.getNetworkForTable(cyTable);
			final String namespace = networkTableManager.getTableNamespace(cyTable);
			
			if (network != null && namespace != null)
				networkTableManager.removeTable(network, objType, namespace);
		}
	}
	
	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		if (e.getSource() == currentTable)
			attributeBrowserToolBar.updateEnableState();
	}

	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		if (e.getSource() == currentTable)
			attributeBrowserToolBar.updateEnableState();
	}
	
	private JComboBox getTableChooser() {
		if (tableChooser == null) {
			tableChooser = new JComboBox(new DefaultComboBoxModel());
			tableChooser.setRenderer(new TableChooserCellRenderer());
			tableChooser.addActionListener(this);
			tableChooser.setMaximumSize(SELECTOR_SIZE);
			tableChooser.setMinimumSize(SELECTOR_SIZE);
			tableChooser.setPreferredSize(SELECTOR_SIZE);
			tableChooser.setSize(SELECTOR_SIZE);
			tableChooser.setEnabled(false);
		}
		
		return tableChooser;
	}
	
	private Set<CyTable> getPublicTables(CyNetwork currentNetwork) {
		final Set<CyTable> tables = new LinkedHashSet<CyTable>();
		final Map<String, CyTable> map = networkTableManager.getTables(currentNetwork, objType);
		
		for (final CyTable tbl : map.values()) {
			if (tbl.isPublic())
				tables.add(tbl);
		}
		
		return tables;
	}
}
