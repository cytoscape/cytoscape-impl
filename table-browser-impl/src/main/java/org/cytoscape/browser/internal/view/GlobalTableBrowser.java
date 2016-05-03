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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.RowsDeletedEvent;
import org.cytoscape.model.events.RowsDeletedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TablePrivacyChangedEvent;
import org.cytoscape.model.events.TablePrivacyChangedListener;
import org.cytoscape.service.util.CyServiceRegistrar;

public class GlobalTableBrowser extends AbstractTableBrowser 
                                implements TableAboutToBeDeletedListener, RowsDeletedListener, RowsSetListener, 
                                           TableAddedListener, TablePrivacyChangedListener {

	private static final long serialVersionUID = 2269984225983802421L;

	private final GlobalTableChooser tableChooser;

	public GlobalTableBrowser(
			final String tabTitle,
			final CyServiceRegistrar serviceRegistrar,
			final PopupMenuHelper popupMenuHelper
	) {
		super(tabTitle, serviceRegistrar, popupMenuHelper);
		
		tableChooser = new GlobalTableChooser();
		tableChooser.addActionListener(this);
		final Dimension d = new Dimension(SELECTOR_WIDTH, tableChooser.getPreferredSize().height);
		tableChooser.setMaximumSize(d);
		tableChooser.setMinimumSize(d);
		tableChooser.setPreferredSize(d);
		tableChooser.setSize(d);
		tableChooser.setToolTipText("\"Tables\" are data tables not associated with specific networks.");
		tableChooser.setEnabled(false);
		
		attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, tableChooser, null);

		add(attributeBrowserToolBar, BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final CyTable table = (CyTable) tableChooser.getSelectedItem();
		
		if (table == currentTable || table == null)
			return;

		currentTable = table;
		
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		applicationManager.setCurrentTable(table);
		
		showSelectedTable();
	}

	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
		
		if (cyTable.isPublic() || showPrivateTables()) {
			final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
			comboBoxModel.removeItem(cyTable);
			attributeBrowserToolBar.updateEnableState(tableChooser);
			
			if (comboBoxModel.getSize() == 0) {
				// The last table is deleted, refresh the browser table (this is a special case)
				deleteTable(cyTable);
				SwingUtilities.invokeLater(() -> {
                    serviceRegistrar.unregisterService(GlobalTableBrowser.this, CytoPanelComponent.class);
                    
                    final CyApplicationManager applicationManager =
                            serviceRegistrar.getService(CyApplicationManager.class);
                    
                    applicationManager.setCurrentTable(null);
                    showSelectedTable();
                });
			}
		}
	}
	
	/**
	 * Switch to new table when it is registered to the table manager.
	 * 
	 * Note: This combo box only displays Global Table.
	 */
	@Override
	public void handleEvent(TableAddedEvent e) {
		final CyTable newTable = e.getTable();

		if (newTable.isPublic() || showPrivateTables()) {
			final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
			
			if (tableManager.getGlobalTables().contains(newTable)) {
				final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
				comboBoxModel.addAndSetSelectedItem(newTable);
				attributeBrowserToolBar.updateEnableState(tableChooser);
			}
			
			if (tableChooser.getItemCount() == 1) {
				SwingUtilities.invokeLater(
						() -> serviceRegistrar.registerService(GlobalTableBrowser.this, CytoPanelComponent.class,
                                new Properties()));
			}
		}
	}

	@Override
	public void handleEvent(TablePrivacyChangedEvent e) {
		final CyTable table = e.getSource();
		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
		final boolean showPrivateTables = showPrivateTables();
		
		if (!table.isPublic() && !showPrivateTables){
			comboBoxModel.removeItem(table);

			if (comboBoxModel.getSize() == 0) {
				tableChooser.setEnabled(false);
				// The last table is deleted, refresh the browser table (this is a special case)
				deleteTable(table);
				SwingUtilities.invokeLater(() -> {
                    serviceRegistrar.unregisterService(GlobalTableBrowser.this, CytoPanelComponent.class);
                    showSelectedTable();
                });
			}
		} else if (table.isPublic() || showPrivateTables) {
			comboBoxModel.addAndSetSelectedItem(table);
		}
		
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		applicationManager.setCurrentTable(currentTable);
	}
	
	@Override
	public void handleEvent(final RowsSetEvent e) {
		BrowserTable table = getCurrentBrowserTable();
		if (table == null)
			return;
		BrowserTableModel model = (BrowserTableModel) table.getModel();
		CyTable dataTable = model.getDataTable();

		if (e.getSource() != dataTable)
			return;		
		synchronized (this) {
				model.fireTableDataChanged();
		}
	}

	
	@Override
	public void handleEvent(final RowsDeletedEvent e) {
		BrowserTable table = getCurrentBrowserTable();
		if (table == null)
			return;
		BrowserTableModel model = (BrowserTableModel) table.getModel();
		CyTable dataTable = model.getDataTable();

		if (e.getSource() != dataTable)
			return;		
		synchronized (this) {
				model.fireTableDataChanged();
		}
	}
	
	private class GlobalTableChooser extends JComboBox<CyTable> {

		private static final long serialVersionUID = 2952839169799310442L;
		
		private final Map<CyTable, String> tableToStringMap;
		
		GlobalTableChooser() {
			tableToStringMap = new HashMap<CyTable, String>();
			setModel(new GlobalTableComboBoxModel(tableToStringMap));
			setRenderer(new TableChooserCellRenderer(tableToStringMap));
		}
	}
	
	private class GlobalTableComboBoxModel extends DefaultComboBoxModel<CyTable> {

		private static final long serialVersionUID = -5435833047656563358L;

		private final Comparator<CyTable> tableComparator;
		private final Map<CyTable, String> tableToStringMap;
		private final List<CyTable> tables;

		GlobalTableComboBoxModel(final Map<CyTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
			tables = new ArrayList<CyTable>();
			tableComparator = (table1, table2) -> table1.getTitle().compareTo(table2.getTitle());
		}

		private void updateTableToStringMap() {
			tableToStringMap.clear();
			
			for (final CyTable table : tables)
				tableToStringMap.put(table, table.getTitle());
		}

		@Override
		public int getSize() {
			return tables.size();
		}

		@Override
		public CyTable getElementAt(int index) {
			return tables.get(index);
		}

		void addAndSetSelectedItem(final CyTable newTable) {
			if (!tables.contains(newTable)) {
				tables.add(newTable);
				Collections.sort(tables, tableComparator);
				updateTableToStringMap();
				fireContentsChanged(this, 0, tables.size() - 1);
			}

			// This is necessary to avoid deadlock!
			SwingUtilities.invokeLater(() -> setSelectedItem(newTable));
		}

		void removeItem(final CyTable deletedTable) {
			if (tables.contains(deletedTable)) {
				tables.remove(deletedTable);
				
				if (tables.size() > 0) {
					Collections.sort(tables, tableComparator);
					setSelectedItem(tables.get(0));
				} else {
					setSelectedItem(null);
				}
			}
		}
	}
}
