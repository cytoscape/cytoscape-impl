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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.browser.internal.TableChooser.GlobalTableComboBoxModel;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TablePrivacyChangedEvent;
import org.cytoscape.model.events.TablePrivacyChangedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

public class GlobalTableBrowser extends AbstractTableBrowser implements TableAboutToBeDeletedListener, RowsSetListener, TableAddedListener, TablePrivacyChangedListener {

	private static final long serialVersionUID = 2269984225983802421L;

	static final Color GLOBAL_TABLE_COLOR = new Color(0x1E, 0x90, 0xFF);
	static final Color GLOBAL_TABLE_ENTRY_COLOR = new Color(0x1E, 0x90, 0xFF, 150);
	static final Color GLOBAL_TABLE_BACKGROUND_COLOR = new Color(0x87, 0xCE, 0xFA, 50);
	static final Font GLOBAL_FONT = new Font("SansSerif", Font.BOLD, 12);
	
	private final TableChooser tableChooser;

	public GlobalTableBrowser(String tabTitle, CyTableManager tableManager, CyNetworkTableManager networkTableManager,
			CyServiceRegistrar serviceRegistrar, EquationCompiler compiler,
			CyNetworkManager networkManager, DeleteTableTaskFactory deleteTableTaskFactoryService,
			DialogTaskManager guiTaskManagerServiceRef, PopupMenuHelper popupMenuHelper,
			CyApplicationManager applicationManager, CyEventHelper eventHelper){//, final MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryService) {
		super(tabTitle, tableManager, networkTableManager, serviceRegistrar, compiler, networkManager,
				deleteTableTaskFactoryService, guiTaskManagerServiceRef, popupMenuHelper, applicationManager, eventHelper);
		
		tableChooser = new TableChooser();
		tableChooser.addActionListener(this);
		tableChooser.setMaximumSize(SELECTOR_SIZE);
		tableChooser.setMinimumSize(SELECTOR_SIZE);
		tableChooser.setPreferredSize(SELECTOR_SIZE);
		tableChooser.setSize(SELECTOR_SIZE);
		tableChooser.setFont(GLOBAL_FONT);
		tableChooser.setForeground(GLOBAL_TABLE_COLOR);
		tableChooser.setToolTipText("\"Tables\" are data tables not associated with specific networks.");
		tableChooser.setEnabled(false);
		
		attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler,
				deleteTableTaskFactoryService, guiTaskManagerServiceRef, tableChooser, null, applicationManager);//, mapGlobalTableTaskFactoryService);

		add(attributeBrowserToolBar, BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final CyTable table = (CyTable) tableChooser.getSelectedItem();
		
		if (table == currentTable || table == null)
			return;

		currentTable = table;
		//applicationManager.setCurrentGlobalTable(table);
		showSelectedTable();
	}

	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
		
		if (cyTable.isPublic()) {
			final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
			comboBoxModel.removeItem(cyTable);
			
			if (comboBoxModel.getSize() == 0) {
				tableChooser.setEnabled(false);
				// The last table is deleted, refresh the browser table (this is a special case)
				deleteTable(cyTable);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						serviceRegistrar.unregisterService(GlobalTableBrowser.this, CytoPanelComponent.class);
						//applicationManager.setCurrentGlobalTable(null);
						showSelectedTable();
					}
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

		if (newTable.isPublic()) {
			if (tableManager.getGlobalTables().contains(newTable)) {
				final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
				comboBoxModel.addAndSetSelectedItem(newTable);
			}
			
			if (tableChooser.getItemCount() == 1) {
				SwingUtilities.invokeLater(
					new Runnable() {
						public void run() {
							serviceRegistrar.registerService(GlobalTableBrowser.this, CytoPanelComponent.class, new Properties());
							//applicationManager.setCurrentGlobalTable(newTable);
						}
					});
			}
			
			if (tableChooser.getItemCount() != 0)
				tableChooser.setEnabled(true);
		}
	}

	@Override
	public void handleEvent(TablePrivacyChangedEvent e) {

		final CyTable table = e.getSource();
		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
		if(!table.isPublic()){
			comboBoxModel.removeItem(table);

			if (comboBoxModel.getSize() == 0) {
				tableChooser.setEnabled(false);
				// The last table is deleted, refresh the browser table (this is a special case)
				deleteTable(table);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						serviceRegistrar.unregisterService(GlobalTableBrowser.this, CytoPanelComponent.class);
						showSelectedTable();
					}
				});
			}
		}else
			comboBoxModel.addAndSetSelectedItem(table);
		
	}
	
	@Override
	public void handleEvent(final RowsSetEvent e) {
		BrowserTableModel model = (BrowserTableModel) getCurrentBrowserTable().getModel();
		CyTable dataTable = model.getDataTable();

		if (e.getSource() != dataTable)
			return;		
		synchronized (this) {
				model.fireTableDataChanged();
		}
	}

	
}
