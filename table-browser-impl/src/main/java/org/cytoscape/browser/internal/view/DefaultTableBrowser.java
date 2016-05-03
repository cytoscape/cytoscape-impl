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

import static org.cytoscape.util.swing.IconManager.ICON_COG;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
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


public class DefaultTableBrowser extends AbstractTableBrowser implements SetCurrentNetworkListener,
		TableAddedListener, TableAboutToBeDeletedListener, ColumnCreatedListener, ColumnDeletedListener {

	private static final long serialVersionUID = 627394119637512735L;

	private JButton selectionModeButton;
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
		
		if (objType != CyNetwork.class) {
			selectionModeButton = new JButton(ICON_COG);
			selectionModeButton.setToolTipText("Change Table Mode");
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			AttributeBrowserToolBar.styleButton(selectionModeButton,
					iconManager.getIconFont(AttributeBrowserToolBar.ICON_FONT_SIZE * 4/5));
			
			selectionModeButton.addActionListener(e -> {
                DefaultTableBrowser.this.actionPerformed(e);
                displayMode.show(selectionModeButton, 0, selectionModeButton.getHeight());
            });
			
			attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, getTableChooser(),
					selectionModeButton, objType);
		} else {
			attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, getTableChooser(), objType);
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

		displayAuto.addActionListener(e -> {
            rowSelectionMode = ViewMode.AUTO;
            changeSelectionMode();

            displayAuto.setSelected(true);
            displayAll.setSelected(false);
            displaySelect.setSelected(false);
        });
		
		displayAll.addActionListener(e -> {
            rowSelectionMode = ViewMode.ALL;
            changeSelectionMode();

            displayAuto.setSelected(false);
            displayAll.setSelected(true);
            displaySelect.setSelected(false);
        });
		
		displaySelect.addActionListener(e -> {
            rowSelectionMode = ViewMode.SELECTED;
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
		
		invokeOnEDT(() -> {
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
                    
                    attributeBrowserToolBar.updateEnableState(getTableChooser());
                    getTableChooser().setSelectedItem(currentTable);
                }
            } finally {
                ignoreSetCurrentTable = false;
            }
            
            if (currentTable != null) {
                final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
                applicationManager.setCurrentTable(currentTable);
            }
            
            showSelectedTable();
            changeSelectionMode();
        }, true);
		
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
                        attributeBrowserToolBar.updateEnableState(getTableChooser());
                    }
                }, false);
			}
		}
	}
	
	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
		final BrowserTable table = getAllBrowserTablesMap().get(cyTable);
		// System.out.println("Handling delete table event for table: "+cyTable);
		
		if (table != null) {
			((DefaultComboBoxModel<CyTable>)getTableChooser().getModel()).removeElement(cyTable);
			
			// We need this to happen synchronously or we get royally messed up by the
			// new table selection
			invokeOnEDT(() -> {
                // System.out.println("Deleting table "+cyTable+" from browser");
                attributeBrowserToolBar.updateEnableState(getTableChooser());
                deleteTable(cyTable);
            }, true);
			
//			final CyNetworkTableManager netTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
//			final CyNetwork network = netTableManager.getNetworkForTable(cyTable);
//			final String namespace = netTableManager.getTableNamespace(cyTable);
			
			// FIXME: why is the table browser removing the table??????
			// if (network != null && namespace != null)
			// 	networkTableManager.removeTable(network, objType, namespace);
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

	private void invokeOnEDT(Runnable doRun, boolean wait) {
		if (SwingUtilities.isEventDispatchThread()) {
			doRun.run();
			return;
		}

		if (wait) {
			try {
				SwingUtilities.invokeAndWait(doRun);
			} catch (InterruptedException e) {
				return;
			} catch (java.lang.reflect.InvocationTargetException e) {
				// FIXME: create rational message and log it
				e.printStackTrace();
			}
		} else {
			SwingUtilities.invokeLater(doRun);
		}
	}
}
