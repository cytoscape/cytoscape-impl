package org.cytoscape.browser.internal.view;

import static org.cytoscape.browser.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.browser.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.JToolBar;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentTableEvent;
import org.cytoscape.application.events.SetCurrentTableListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.TableToolBarComponent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.browser.internal.action.TaskFactoryTunableAction;
import org.cytoscape.browser.internal.task.DynamicTableTaskFactory;
import org.cytoscape.browser.internal.task.DynamicTogglableTableTaskFactory;
import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.Togglable;

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

public class TableBrowserMediator implements SetCurrentNetworkListener, SetCurrentTableListener, TableAddedListener,
		TableAboutToBeDeletedListener, CytoPanelComponentSelectedListener {

	private final HashMap<Class<? extends CyIdentifiable>, AbstractTableBrowser> tableBrowsers = new HashMap<>();
	
	private final Map<TaskFactory, CyAction> taskMap = new HashMap<>();
	private final Map<Object, TaskFactory> provisionerMap = new IdentityHashMap<>();
	
	private final CyServiceRegistrar serviceRegistrar;

	public TableBrowserMediator(
			DefaultTableBrowser nodeTableBrowser,
			DefaultTableBrowser edgeTableBrowser,
			DefaultTableBrowser networkTableBrowser,
			GlobalTableBrowser globalTableBrowser,
			CyServiceRegistrar serviceRegistrar
	) {
		this.serviceRegistrar = serviceRegistrar;
		
		tableBrowsers.put(CyNode.class, nodeTableBrowser);
		tableBrowsers.put(CyEdge.class, edgeTableBrowser);
		tableBrowsers.put(CyNetwork.class, networkTableBrowser);
		tableBrowsers.put(null, globalTableBrowser);
		
		for (var tb : tableBrowsers.values()) {
			if (tb instanceof DefaultTableBrowser) {
				tb.getTableChooser().addActionListener(e -> {
					((DefaultTableBrowser) tb).updateCurrentTable();
					
					// Make sure the Application Manager has our current table
					var applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
					var table = tb.getCurrentTable();
					
					if (table != null && !table.equals(applicationManager.getCurrentTable())) {
						var tableManager = serviceRegistrar.getService(CyTableManager.class);
						
						if (tableManager.getTable(table.getSUID()) != null)
							applicationManager.setCurrentTable(table);
					}
				});
			}
		}
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkEvent evt) {
		updateCurrentNetworkTables(evt.getNetwork());
	}

	@Override
	public void handleEvent(SetCurrentTableEvent evt) {
		var table = evt.getTable();
		
		if (table == null)
			return;
		
		var type = getTableType(table);
		var tb = tableBrowsers.get(type);
		
		if (tb != null) {
			invokeOnEDT(() -> {
				if (tb.containsTable(table)) {
					tb.setCurrentTable(table);
					selectTableBrowser(tb);
				}
			});
		}
	}

	/**
	 * Switch to new table when it is registered to the table manager.
	 */
	@Override
	public void handleEvent(TableAddedEvent e) {
		var table = e.getTable();

		if (table.isPublic() || TableBrowserUtil.isShowPrivateTables(serviceRegistrar)) {
			var type = getTableType(table);
			var tb = tableBrowsers.get(type);
			
			if (tb != null) {
				invokeOnEDT(() -> {
					tb.addTable(table);
					tb.selectTable(table);
					
					// Show the Unassigned Tables panel
					if (tb.getObjectType() == null && tb.getTableChooser().getItemCount() == 1) {
						serviceRegistrar.registerService(tb, CytoPanelComponent.class);
						
						var appManager = serviceRegistrar.getService(CyApplicationManager.class);
						
						if (table.equals(appManager.getCurrentTable()))
							selectTableBrowser(tb);
					}
				});
			}
		}
	}

	@Override
	public void handleEvent(TableAboutToBeDeletedEvent e) {
		var table = e.getTable();
		
		if (table.isPublic() || TableBrowserUtil.isShowPrivateTables(serviceRegistrar)) {
			// We need this to happen synchronously or we get royally messed up by the new table selection!
			invokeOnEDTAndWait(() -> {
				// We can't use getTableType() here because the table might have been removed from the
				// CyNetworkTableManager, so we just find the table browser that has it
				for (var tb : tableBrowsers.values()) {
					tb.removeTable(table);
					
					if (tb.isEmpty()) {
						// The last table is deleted, hide the table browser
						serviceRegistrar.unregisterService(tb, CytoPanelComponent.class);
						
						// Make sure the application manager is sync'd with our current table
						serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(getCurrentTable());
					}
				}
			});
		}
	}
	
	@Override
	public void handleEvent(CytoPanelComponentSelectedEvent evt) {
		var cytoPanel = evt.getCytoPanel();
		var name = cytoPanel.getCytoPanelName();
		int idx = evt.getSelectedIndex();
		int count = cytoPanel.getCytoPanelComponentCount();
		
		if (name != AbstractTableBrowser.CYTO_PANEL_NAME || idx < 0 || idx >= count)
			return;
		
		var comp = cytoPanel.getComponentAt(idx);
			
		if (comp == null)
			return;
		
		var table = getCurrentTable();
		
		if (table == null || table.isPublic())
			serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(table);
	}
	
	public CyTable getCurrentTable() {
		var cytoPanel = getTableCytoPanel();
		var comp = cytoPanel.getSelectedComponent();
		CyTable table = null;
		
		for (var tb : tableBrowsers.values()) {
			if (tb.getComponent().equals(comp)) {
				table = tb.getCurrentTable();
				break;
			}
		}
		
		return table;
	}

	public TableRenderer getCurrentTableRenderer() {
		var table = getCurrentTable();
		
		return table != null ? getTableRenderer(table) : null;
	}
	
	public AbstractTableBrowser getTableBrowser(TableRenderer renderer) {
		for (var tb : tableBrowsers.values()) {
			if (renderer.equals(tb.getCurrentRenderer()))
				return tb;
		}
		
		return null;
	}
	
	public void selectTableBrowser(AbstractTableBrowser tableBrowser) {
		var cytoPanel = getTableCytoPanel();
		var idx = cytoPanel.indexOfComponent(tableBrowser.getComponent());
		
		if (idx >= 0 && idx < cytoPanel.getCytoPanelComponentCount() && idx != cytoPanel.getSelectedIndex())
			cytoPanel.setSelectedIndex(idx);
	}
	
	public CytoPanel getTableCytoPanel() {
		return serviceRegistrar.getService(CySwingApplication.class).getCytoPanel(AbstractTableBrowser.CYTO_PANEL_NAME);
	}
	
	public void hideColumn(CyColumn column) {
		var table = column.getTable();
		
		invokeOnEDTAndWait(() -> {
			var renderer = getTableRenderer(table);
			
			if (renderer != null)
				renderer.setColumnVisible(column.getName(), false);
		});
	}
	
	public void toggleTextWrap(CyColumn column) {
		var table = column.getTable();
		
		invokeOnEDTAndWait(() -> {
			var renderer = getTableRenderer(table);
			
			if (renderer != null)
				renderer.setTextWrap(column.getName(), !renderer.isTextWrap(column.getName()));
		});
	}
	
	public boolean isTextWrap(CyColumn column) {
		var renderer = getTableRenderer(column.getTable());
		
		return renderer != null ? renderer.isTextWrap(column.getName()) : false;
	}
	
	public void setOptionsBarVisible(JToolBar toolbar, boolean visible) {
		for (var tb : tableBrowsers.values()) {
			if (tb.getToolBar().equals(toolbar)) {
				tb.getOptionsBar().setVisible(visible);
				break;
			}
		}
	}
	
	/**
	 * Don't use this method if the table has been (or is being) deleted,
	 * because this can return false <code>null</code> types.
	 */
	public Class<? extends CyIdentifiable> getTableType(CyTable table) {
		var netTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		
		return netTableManager.getTableType(table);
	}
	
	public int getTableCount(Class<? extends CyIdentifiable> type) {
		var tb = tableBrowsers.get(type);
		
		return tb != null ? tb.getTableCount() : 0;
	}
	
	public void addAction(CyAction action, Map<String, String> props) {
		invokeOnEDT(() -> {
			if (action.isInNodeTableToolBar())
				tableBrowsers.get(CyNode.class).getToolBar().addAction(action);
			if (action.isInEdgeTableToolBar())
				tableBrowsers.get(CyEdge.class).getToolBar().addAction(action);
			if (action.isInNetworkTableToolBar())
				tableBrowsers.get(CyNetwork.class).getToolBar().addAction(action);
			if (action.isInUnassignedTableToolBar())
				tableBrowsers.get(null).getToolBar().addAction(action);
		});
	}
	
	public void removeAction(CyAction action, Map<String, String> props) {
		invokeOnEDT(() -> {
			if (action.isInNodeTableToolBar())
				tableBrowsers.get(CyNode.class).getToolBar().removeAction(action);
			if (action.isInEdgeTableToolBar())
				tableBrowsers.get(CyEdge.class).getToolBar().removeAction(action);
			if (action.isInNetworkTableToolBar())
				tableBrowsers.get(CyNetwork.class).getToolBar().removeAction(action);
			if (action.isInUnassignedTableToolBar())
				tableBrowsers.get(null).getToolBar().removeAction(action);
		});
	}
	
	public void addTableTaskFactory(TableTaskFactory factory, Map<String, String> props) {
		var provisioner = factory instanceof Togglable ?
				new DynamicTogglableTableTaskFactory(factory, serviceRegistrar) :
				new DynamicTableTaskFactory(factory, serviceRegistrar);
		
		provisionerMap.put(factory, provisioner);
		addTaskFactory(provisioner, props);
	}
	
	public void removeTableTaskFactory(TableTaskFactory factory, Map<String, String> props) {
		removeTaskFactory(provisionerMap.get(factory), props);
	}
	
	/**
	 * Wraps the task factory in a {@link TaskFactoryTunableAction}.
	 */
	public void addTaskFactory(TaskFactory factory, Map<String, String> props) {
		final CyAction action;
		
		if (props.containsKey(ServiceProperties.ENABLE_FOR))
			action = new TaskFactoryTunableAction(factory, props, serviceRegistrar);
		else
			action = new TaskFactoryTunableAction(serviceRegistrar, factory, props);

		taskMap.put(factory, action);
		addAction(action, props);
	}
	
	public void removeTaskFactory(TaskFactory factory, Map<String, String> props) {
		var action = taskMap.remove(factory);
		
		if (action != null)
			removeAction(action, props);
	}
	
	public void addTableToolBarComponent(TableToolBarComponent tbc, Map<String, String> props) {
		invokeOnEDTAndWait(() -> {
			var tableBrowser = tableBrowsers.get(tbc.getTableType());
			
			if (tableBrowser != null)
				tableBrowser.getToolBar().addToolBarComponent(tbc, props);
		});
	}

	public void removeTableToolBarComponent(TableToolBarComponent tbc, Map<String, String> props) {
		invokeOnEDTAndWait(() -> {
			for (var tb : tableBrowsers.values())
				tb.getToolBar().removeToolBarComponent(tbc);
		});
	}
	
	private TableRenderer getTableRenderer(CyTable table) {
		for (var tb : tableBrowsers.values()) {
			var renderer = tb.getTableRenderer(table);
			
			if (renderer != null)
				return renderer;
		}
		
		return null;
	}
	
	private void updateCurrentNetworkTables(CyNetwork network) {
		invokeOnEDTAndWait(() -> {
			// Update UI
			((DefaultTableBrowser) tableBrowsers.get(CyNode.class)).update(network);
			((DefaultTableBrowser) tableBrowsers.get(CyEdge.class)).update(network);
			((DefaultTableBrowser) tableBrowsers.get(CyNetwork.class)).update(network);
			
			// Get the new current table
			var table = getCurrentTable();
			
			// Update the CyApplicationManager
			if (table == null || table.isPublic())
				serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(table);
		});
	}
}
