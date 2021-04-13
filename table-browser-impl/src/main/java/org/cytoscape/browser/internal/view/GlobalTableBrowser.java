package org.cytoscape.browser.internal.view;

import static org.cytoscape.browser.internal.util.ViewUtil.invokeOnEDT;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.browser.internal.view.tools.GeneralOptionsControl;
import org.cytoscape.browser.internal.view.tools.RowHeightControl;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.TablePrivacyChangedEvent;
import org.cytoscape.model.events.TablePrivacyChangedListener;
import org.cytoscape.service.util.CyServiceRegistrar;

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
public class GlobalTableBrowser extends AbstractTableBrowser implements TablePrivacyChangedListener {

	private GlobalTableChooser tableChooser;

	public GlobalTableBrowser(String tabTitle, CyServiceRegistrar serviceRegistrar) {
		super(tabTitle, null, serviceRegistrar);
		
		var controls = List.of(
				new RowHeightControl(serviceRegistrar),
				new GeneralOptionsControl(serviceRegistrar)
		);
		
		getOptionsBar().setFormatControls(controls);
	}
	
	@Override
	public String getIdentifier() {
		return "org.cytoscape.UnassignedTables";
	}

	public void setCurrentTable() {
		var table = (CyTable) getTableChooser().getSelectedItem();

		if (table == currentTable || table == null)
			return;

		setCurrentTable(table);
		showSelectedTable();
		
		serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(table);
	}

	@Override
	public void handleEvent(TablePrivacyChangedEvent e) {
		var table = e.getSource();
		var comboBoxModel = (GlobalTableComboBoxModel) getTableChooser().getModel();
		boolean showPrivateTables = TableBrowserUtil.isShowPrivateTables(serviceRegistrar);
		
		if (!table.isPublic() && !showPrivateTables){
			comboBoxModel.removeElement(table);

			if (comboBoxModel.getSize() == 0) {
				invokeOnEDT(() -> {
					getTableChooser().setEnabled(false);
					// The last table is deleted, refresh the browser table (this is a special case)
					removeTable(table);
					serviceRegistrar.unregisterService(GlobalTableBrowser.this, CytoPanelComponent.class);
					showSelectedTable();
				});
			}
		} else if (table.isPublic() || showPrivateTables) {
			invokeOnEDT(() -> {
				comboBoxModel.addElement(table);
				comboBoxModel.setSelectedItem(table);
			});
		}
		
		serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(currentTable);
	}
	
	@Override
	protected GlobalTableChooser getTableChooser() {
		if (tableChooser == null) {
			tableChooser = new GlobalTableChooser();
			tableChooser.addActionListener(e -> setCurrentTable());
			var d = new Dimension(SELECTOR_WIDTH, tableChooser.getPreferredSize().height);
			tableChooser.setMaximumSize(d);
			tableChooser.setMinimumSize(d);
			tableChooser.setPreferredSize(d);
			tableChooser.setSize(d);
			tableChooser.setToolTipText("\"Tables\" are data tables not associated with specific networks.");
		}
		
		return tableChooser;
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

		GlobalTableComboBoxModel(Map<CyTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
			tables = new ArrayList<>();
			tableComparator = new Comparator<CyTable>() {
				@Override
				public int compare(CyTable table1, CyTable table2) {
					return table1.getTitle().compareTo(table2.getTitle());
				}
			};
		}

		private void updateTableToStringMap() {
			tableToStringMap.clear();
			
			for (var table : tables)
				tableToStringMap.put(table, table.getTitle());
		}

		@Override
		public int getIndexOf(Object obj) {
			return tables.indexOf(obj);
		}
		
		@Override
		public int getSize() {
			return tables.size();
		}

		@Override
		public CyTable getElementAt(int index) {
			return tables.get(index);
		}

		@Override
		public void addElement(CyTable table) {
			if (!tables.contains(table)) {
				tables.add(table);
				Collections.sort(tables, tableComparator);
				updateTableToStringMap();
				fireContentsChanged(this, 0, tables.size() - 1);
			}
		}
		
		@Override
		public void removeElement(Object table) {
			if (tables.contains(table)) {
				tables.remove(table);
				
				if (tables.isEmpty()) {
					setSelectedItem(null);
				} else {
					Collections.sort(tables, tableComparator);
					setSelectedItem(tables.get(0));
				}
			}
		}
	}
}
