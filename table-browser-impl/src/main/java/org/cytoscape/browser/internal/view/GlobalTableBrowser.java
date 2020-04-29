package org.cytoscape.browser.internal.view;

import static org.cytoscape.browser.internal.util.ViewUtil.invokeOnEDT;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TablePrivacyChangedEvent;
import org.cytoscape.model.events.TablePrivacyChangedListener;
import org.cytoscape.service.util.CyServiceRegistrar;

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
public class GlobalTableBrowser extends AbstractTableBrowser 
                                implements TableAboutToBeDeletedListener,
                                           TableAddedListener, TablePrivacyChangedListener {

	private final GlobalTableChooser tableChooser;

	public GlobalTableBrowser(
			final String tabTitle,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(tabTitle, serviceRegistrar);
		
		tableChooser = new GlobalTableChooser();
		tableChooser.addActionListener(e -> setCurrentTable());
		final Dimension d = new Dimension(SELECTOR_WIDTH, tableChooser.getPreferredSize().height);
		tableChooser.setMaximumSize(d);
		tableChooser.setMinimumSize(d);
		tableChooser.setPreferredSize(d);
		tableChooser.setSize(d);
		tableChooser.setToolTipText("\"Tables\" are data tables not associated with specific networks.");
		tableChooser.setEnabled(false);
		
		setToolBar(new TableBrowserToolBar(serviceRegistrar, tableChooser, null));
	}

	@Override
	public String getIdentifier() {
		return "org.cytoscape.UnassignedTables";
	}
	
	public void setCurrentTable() {
		final CyTable table = (CyTable) tableChooser.getSelectedItem();
		if (table == currentTable || table == null)
			return;

		currentTable = table;
		serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(table);
		showSelectedTable();
	}

	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
		
		if (cyTable.isPublic() || showPrivateTables()) {
			invokeOnEDT(() -> {
				final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
				comboBoxModel.removeItem(cyTable);
				getToolBar().updateEnableState(tableChooser);
				
				if (comboBoxModel.getSize() == 0) {
					// The last table is deleted, refresh the browser table (this is a special case)
					removeTable(cyTable);
					serviceRegistrar.unregisterService(GlobalTableBrowser.this, CytoPanelComponent.class);
					serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(null);
					showSelectedTable();
				}
			});
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
				getToolBar().updateEnableState(tableChooser);
			}
			
			if (tableChooser.getItemCount() == 1) {
				invokeOnEDT(() -> {
					serviceRegistrar.registerService(GlobalTableBrowser.this, CytoPanelComponent.class,
							new Properties());
				});
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
				invokeOnEDT(() -> {
					tableChooser.setEnabled(false);
					// The last table is deleted, refresh the browser table (this is a special case)
					removeTable(table);
					serviceRegistrar.unregisterService(GlobalTableBrowser.this, CytoPanelComponent.class);
					showSelectedTable();
				});
			}
		} else if (table.isPublic() || showPrivateTables) {
			comboBoxModel.addAndSetSelectedItem(table);
		}
		
		serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(currentTable);
	}
	
	private class GlobalTableChooser extends JComboBox<CyTable> {

		private final Map<CyTable, String> tableToStringMap;
		
		GlobalTableChooser() {
			tableToStringMap = new HashMap<>();
			setModel(new GlobalTableComboBoxModel(tableToStringMap));
			setRenderer(new TableChooserCellRenderer(tableToStringMap));
		}
	}
	
	private class GlobalTableComboBoxModel extends DefaultComboBoxModel<CyTable> {

		private final Comparator<CyTable> tableComparator;
		private final Map<CyTable, String> tableToStringMap;
		private final List<CyTable> tables;

		GlobalTableComboBoxModel(final Map<CyTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
			tables = new ArrayList<>();
			tableComparator = new Comparator<CyTable>() {
				@Override
				public int compare(final CyTable table1, final CyTable table2) {
					return table1.getTitle().compareTo(table2.getTitle());
				}
			};
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
			invokeOnEDT(() -> {
				setSelectedItem(newTable);
			});
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
