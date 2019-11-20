package org.cytoscape.browser.internal.view;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.browser.internal.task.StaticTaskFactoryProvisioner;
import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.browser.internal.util.ValidatedObjectAndEditString;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.util.swing.PopupMenuGravityTracker;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2009 - 2019 The Cytoscape Consortium
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

/**
 * A class that encapsulates the creation of JPopupMenus based on TaskFactory services.
 */
public class PopupMenuHelper {

	private final Map<TableCellTaskFactory, Map<?, ?>> tableCellFactoryMap;
	private final Map<TableColumnTaskFactory, Map<?, ?>> tableColumnFactoryMap;
	private final StaticTaskFactoryProvisioner factoryProvisioner;

	private final CyServiceRegistrar serviceRegistrar;

	public PopupMenuHelper(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;

		tableCellFactoryMap = new HashMap<>();
		tableColumnFactoryMap = new HashMap<>();
		factoryProvisioner = new StaticTaskFactoryProvisioner();
	}

	public void createColumnHeaderMenu(CyColumn column, Class<? extends CyIdentifiable> tableType, Component invoker,
			int x, int y) {
		if (tableColumnFactoryMap.isEmpty())
			return;

		var menu = new JPopupMenu();
		var tracker = new PopupMenuGravityTracker(menu);

		for (var entry : tableColumnFactoryMap.entrySet()) {
			TableColumnTaskFactory taskFactory = entry.getKey();
			TaskFactory provisioner = factoryProvisioner.createFor(taskFactory, column);
			createMenuItem(provisioner, tracker, entry.getValue(), tableType);
		}

		if (menu.getSubElements().length > 0)
			menu.show(invoker, x, y);
	}

	@SuppressWarnings("serial")
	public void createTableCellMenu(CyColumn column, Object primaryKeyValue, Class<? extends CyIdentifiable> tableType,
			Component invoker, int x, int y, JTable table) {
		var menu = new JPopupMenu();
		Object value = column.getTable().getRow(primaryKeyValue).get(column.getName(), column.getType());

		if (value != null) {
			String urlString = value.toString();
			
			if (urlString != null && (urlString.startsWith("http:") || urlString.startsWith("https:")))
				menu.add(getOpenLinkMenu(value.toString()));
		}

		var tracker = new PopupMenuGravityTracker(menu);

		for (var entry : tableCellFactoryMap.entrySet()) {
			TableCellTaskFactory taskFactory = entry.getKey();
			TaskFactory provisioner = factoryProvisioner.createFor(taskFactory, column, primaryKeyValue);
			createMenuItem(provisioner, tracker, entry.getValue(), tableType);
		}

		menu.add(new JSeparator());

		// Add preset menu items
		menu.add(new JMenuItem(new AbstractAction("Edit") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Point point = new Point(x, y);
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				table.editCellAt(row, column);
				table.transferFocus();
			}
		}));
		
		menu.add(new JMenuItem(new AbstractAction("Copy") {
			@Override
			public void actionPerformed(ActionEvent e) {
				CyRow sourceRow = column.getTable().getRow(primaryKeyValue);
				String columnName = column.getName();
				Object sourceValue = sourceRow.getRaw(columnName);
				
				StringSelection stringSelection = new StringSelection(sourceValue.toString());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			}
		}));
		
		menu.add(new JMenuItem(new AbstractAction("Paste") {
			@Override
			public void actionPerformed(ActionEvent e) {
				CyRow sourceRow = column.getTable().getRow(primaryKeyValue);
				String columnName = column.getName();
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				
				try {
					var pasteValue = (String) clipboard.getData(DataFlavor.stringFlavor);
					List<Object> parsedData = TableBrowserUtil.parseCellInput(column.getTable(), columnName,
							pasteValue);

					if (parsedData.get(0) != null)
						sourceRow.set(columnName, parsedData.get(0));
					else
						JOptionPane.showMessageDialog(null, parsedData.get(1), "Invalid Value",
								JOptionPane.ERROR_MESSAGE);
				} catch (UnsupportedFlavorException | IOException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Invalid Value", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		}));

		if (tableType == CyNode.class || tableType == CyEdge.class) {
			menu.add(new JSeparator());

			var name = String.format("Select %s from selected rows", tableType == CyNode.class ? "nodes" : "edges");

			JMenuItem mi = new JMenuItem(new AbstractAction(name) {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectElementsFromSelectedRows(table, tableType);
				}

				@Override
				public boolean isEnabled() {
					CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);

					return table.getSelectedRowCount() > 0 && applicationManager.getCurrentNetwork() != null;
				}
			});
			menu.add(mi);
		}

		if (menu.getSubElements().length > 0)
			menu.show(invoker, x, y);
	}

	/**
	 * This method creates popup menu submenus and menu items based on the "title"
	 * and "preferredMenu" keywords, depending on which are present in the service
	 * properties.
	 */
	private void createMenuItem(TaskFactory tf, PopupMenuGravityTracker tracker, Map<?, ?> props,
			Class<? extends CyIdentifiable> tableType) {
		if (!enabledFor(tableType, props))
			return;

		String menuLabel = (String) (props.get("title"));
		if (menuLabel == null)
			menuLabel = "Unidentified Task: " + Integer.toString(tf.hashCode());

		if (tf.isReady())
			tracker.addMenuItem(new JMenuItem(new PopupAction(tf, menuLabel)), GravityTracker.USE_ALPHABETIC_ORDER);
	}

	private boolean enabledFor(Class<? extends CyIdentifiable> tableType, Map<?, ?> props) {
		String types = (String) props.get("tableTypes");

		if (types == null)
			return true;

		for (String type : types.split(",")) {
			type = type.trim();

			if ("all".equals(type))
				return true;
			if ("node".equals(type) && CyNode.class.equals(tableType))
				return true;
			if ("edge".equals(type) && CyEdge.class.equals(tableType))
				return true;
			if ("network".equals(type) && CyNetwork.class.equals(tableType))
				return true;
			if ("unassigned".equals(type) && tableType == null)
				return true;
		}

		return false;
	}

	public void addTableColumnTaskFactory(TableColumnTaskFactory newFactory, Map<?, ?> properties) {
		tableColumnFactoryMap.put(newFactory, properties);
	}

	public void removeTableColumnTaskFactory(TableColumnTaskFactory factory, Map<?, ?> properties) {
		tableColumnFactoryMap.remove(factory);
	}

	public void addTableCellTaskFactory(TableCellTaskFactory newFactory, Map<?, ?> properties) {
		tableCellFactoryMap.put(newFactory, properties);
	}

	public void removeTableCellTaskFactory(TableCellTaskFactory factory, Map<?, ?> properties) {
		tableCellFactoryMap.remove(factory);
	}

	/**
	 * A simple action that executes the specified TaskFactory
	 */
	private class PopupAction extends AbstractAction {

		private static final long serialVersionUID = -2841342029789163004L;

		private final TaskFactory tf;

		PopupAction(final TaskFactory tf, final String menuLabel) {
			super(menuLabel);
			this.tf = tf;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			final TaskManager<?, ?> taskManager = serviceRegistrar.getService(TaskManager.class);
			if (taskManager != null && tf != null)
				taskManager.execute(tf.createTaskIterator());
		}
	}

	// Preset menu item: open browser
	protected JMenuItem getOpenLinkMenu(Object urlString) {
		var openLinkItem = new JMenuItem();
		openLinkItem.setText("Open URL in web browser...");

		if (urlString == null || urlString.toString().startsWith("http:") == false) {
			openLinkItem.setEnabled(false);
		} else {
			openLinkItem.addActionListener(evt -> {
				var openBrowser = serviceRegistrar.getService(OpenBrowser.class);
				openBrowser.openURL(urlString.toString());
			});
		}

		return openLinkItem;
	}

	private void selectElementsFromSelectedRows(JTable table, Class<? extends CyIdentifiable> tableType) {
		Thread t = new Thread() {
			@Override
			public void run() {
				CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
				CyNetwork net = applicationManager.getCurrentNetwork();

				if (net != null) {
					BrowserTableModel tableModel = (BrowserTableModel) table.getModel();
					int[] selectedRows = table.getSelectedRows();
					Set<CyRow> targetRows = new HashSet<CyRow>();

					for (int rowIndex : selectedRows) {
						// Getting the row from data table solves the problem with hidden or moved SUID
						// column.
						// However, since the rows might be sorted we need to convert the index to
						// model.
						ValidatedObjectAndEditString selected = (ValidatedObjectAndEditString) tableModel
								.getValueAt(table.convertRowIndexToModel(rowIndex), CyNetwork.SUID);
						targetRows.add(tableModel.getRow(selected.getValidatedObject()));
					}

					CyTable cyTable = tableType == CyNode.class ? net.getDefaultNodeTable() : net.getDefaultEdgeTable();

					for (var cyRow : cyTable.getAllRows())
						cyRow.set(CyNetwork.SELECTED, targetRows.contains(cyRow));

					CyNetworkView view = applicationManager.getCurrentNetworkView();

					if (view != null) {
						CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
						eventHelper.flushPayloadEvents();
						view.updateView();
					}
				}
			}
		};
		t.start();
	}
}
