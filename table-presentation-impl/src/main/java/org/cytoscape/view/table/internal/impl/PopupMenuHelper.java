package org.cytoscape.view.table.internal.impl;

import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.SMALL_ICON_ID;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.util.swing.PopupMenuGravityTracker;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.table.internal.util.TableBrowserUtil;
import org.cytoscape.view.table.internal.util.ValidatedObjectAndEditString;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.Togglable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

	private static float SMALL_ICON_FONT_SIZE = 14.0f;
	private static int SMALL_ICON_SIZE = 16;
	
	private final Map<TableCellTaskFactory, Map<?, ?>> tableCellFactoryMap;
	private final Map<TableColumnTaskFactory, Map<?, ?>> tableColumnFactoryMap;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public PopupMenuHelper(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;

		tableCellFactoryMap = new HashMap<>();
		tableColumnFactoryMap = new HashMap<>();
	}

	@SuppressWarnings("serial")
	public void createColumnHeaderMenu(
			CyColumn column,
			Class<? extends CyIdentifiable> tableType,
			Component invoker,
			int x,
			int y,
			JTable table
	) {
		if (tableColumnFactoryMap.isEmpty())
			return;

		var menu = new JPopupMenu();
		var tracker = new PopupMenuGravityTracker(menu);
		var factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);

		for (var entry : tableColumnFactoryMap.entrySet()) {
			var taskFactory = entry.getKey();
			var provisioner = factoryProvisioner.createFor(taskFactory, column);
			createMenuItem(provisioner, tracker, entry.getValue(), tableType);
		}
		
		// Add preset menu items
		var iconFont = serviceRegistrar.getService(IconManager.class).getIconFont(SMALL_ICON_FONT_SIZE);
		var copyIcon = new TextIcon(IconManager.ICON_COPY, iconFont, SMALL_ICON_SIZE, SMALL_ICON_SIZE);
		
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new AbstractAction("Copy Column Values") {
			{
				putValue(SMALL_ICON, copyIcon);
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				// We assume the column (and only one column) is selected!
				var action = table.getActionMap().get("copy");
				
				if (action != null)
					action.actionPerformed(new ActionEvent(table, e.getID(), "copy"));
			}
		}));
		
		sanitize(menu);

		if (menu.getSubElements().length > 0)
			menu.show(invoker, x, y);
	}

	@SuppressWarnings("serial")
	public JPopupMenu createTableCellMenu(
			CyColumn column,
			Object primaryKeyValue,
			Class<? extends CyIdentifiable> tableType,
			Component invoker,
			int x,
			int y,
			JTable table
	) {
		var menu = new JPopupMenu();
		var value = column.getTable().getRow(primaryKeyValue).get(column.getName(), column.getType());

		if (value != null) {
			var urlString = value.toString();
			
			if (urlString != null && (urlString.startsWith("http:") || urlString.startsWith("https:")))
				menu.add(getOpenLinkMenu(value.toString()));
		}

		var tracker = new PopupMenuGravityTracker(menu);
		var factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);

		for (var entry : tableCellFactoryMap.entrySet()) {
			var taskFactory = entry.getKey();
			var provisioner = factoryProvisioner.createFor(taskFactory, column, primaryKeyValue);
			createMenuItem(provisioner, tracker, entry.getValue(), tableType);
		}

		menu.add(new JSeparator());

		// Add preset menu items
		var iconFont = serviceRegistrar.getService(IconManager.class).getIconFont(SMALL_ICON_FONT_SIZE);
		var editIcon = new TextIcon(IconManager.ICON_EDIT, iconFont, SMALL_ICON_SIZE, SMALL_ICON_SIZE);
		var copyIcon = new TextIcon(IconManager.ICON_COPY, iconFont, SMALL_ICON_SIZE, SMALL_ICON_SIZE);
		var pasteIcon = new TextIcon(IconManager.ICON_PASTE, iconFont, SMALL_ICON_SIZE, SMALL_ICON_SIZE);
		
		menu.add(new JMenuItem(new AbstractAction("Edit") {
			{
				putValue(SMALL_ICON, editIcon);
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				var point = new Point(x, y);
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				table.editCellAt(row, column);
				table.transferFocus();
			}
		}));
		menu.add(new JMenuItem(new AbstractAction("Copy") {
			{
				putValue(SMALL_ICON, copyIcon);
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				var point = new Point(x, y);
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				var object = table.getValueAt(row, column);
				var data = object instanceof ValidatedObjectAndEditString
						? TableBrowserUtil.createCopyString((ValidatedObjectAndEditString) object)
						: (object != null ? object.toString() : "");

				var stringSelection = new StringSelection(data);
				var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			}
		}));
		menu.add(new JMenuItem(new AbstractAction("Paste") {
			{
				putValue(SMALL_ICON, pasteIcon);
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				var sourceRow = column.getTable().getRow(primaryKeyValue);
				var columnName = column.getName();
				var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				
				try {
					var pasteValue = (String) clipboard.getData(DataFlavor.stringFlavor);
					var parsedData = TableBrowserUtil.parseCellInput(column.getTable(), columnName, pasteValue);

					if (parsedData.get(0) != null)
						sourceRow.set(columnName, parsedData.get(0));
					else
						JOptionPane.showMessageDialog(null, parsedData.get(1), "Invalid Value", JOptionPane.ERROR_MESSAGE);
				} catch (UnsupportedFlavorException | IOException ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage(), "Invalid Value", JOptionPane.ERROR_MESSAGE);
					logger.warn("Error pasting cell value", ex);
				}
			}
		}));
		
		menu.add(new JSeparator());
		
		menu.add(new JMenuItem(new AbstractAction("Copy Selected") {
			{
				putValue(SMALL_ICON, copyIcon);
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				var action = table.getActionMap().get("copy");
				
				if (action != null)
					action.actionPerformed(new ActionEvent(table, e.getID(), "copy"));
			}
		}));

		if (tableType == CyNode.class || tableType == CyEdge.class) {
			menu.add(new JSeparator());

			var name = String.format("Select %s from selected rows", tableType == CyNode.class ? "nodes" : "edges");

			var mi = new JMenuItem(new AbstractAction(name) {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectElementsFromSelectedRows(table, tableType);
				}

				@Override
				public boolean isEnabled() {
					var applicationManager = serviceRegistrar.getService(CyApplicationManager.class);

					return table.getSelectedRowCount() > 0 && applicationManager.getCurrentNetwork() != null;
				}
			});
			menu.add(mi);
		}
		
		sanitize(menu);

		if (menu.getSubElements().length > 0)
			menu.show(invoker, x, y);
		
		return menu;
	}

	/**
	 * This method creates popup menu submenus and menu items based on the "title"
	 * and "preferredMenu" keywords, depending on which are present in the service properties.
	 */
	private void createMenuItem(
			TaskFactory tf,
			PopupMenuGravityTracker tracker,
			Map<?, ?> props,
			Class<? extends CyIdentifiable> tableType
	) {
		if (!tf.isReady() || !enabledFor(tableType, props))
			return;

		var menuGravity = (String) props.get(MENU_GRAVITY);
		double gravity = menuGravity != null ? Double.parseDouble(menuGravity) : -1/* Alphabetize by default */;
		
		boolean insertSepBefore = getBooleanProperty(props, INSERT_SEPARATOR_BEFORE);
		
		if (insertSepBefore)
			tracker.addMenuSeparator(gravity - .0001);
		
		var title = (String) props.get(TITLE);
		
		if (title == null)
			title = "Unidentified Task: " + Integer.toString(tf.hashCode());

		var togglable = tf instanceof Togglable;
		
		var action = new PopupAction(tf, title);
		var mi = togglable ? new JCheckBoxMenuItem(action) : new JMenuItem(action);
		tracker.addMenuItem(mi, gravity);
		
		var iconId = props.get(SMALL_ICON_ID);
		
		if (iconId != null && !iconId.toString().trim().isEmpty()) {
			// Check if the icon is really registered
			var icon = serviceRegistrar.getService(IconManager.class).getIcon(iconId.toString());
			
			if (icon != null)
				mi.setIcon(icon);
		}
		
		if (togglable)
			((JCheckBoxMenuItem) mi).setSelected(tf.isOn());
		
		boolean insertSepAfter = getBooleanProperty(props, INSERT_SEPARATOR_AFTER);
		
		if (insertSepAfter)
			tracker.addMenuSeparator(gravity + .0001);
	}

	private boolean enabledFor(Class<? extends CyIdentifiable> tableType, Map<?, ?> props) {
		var types = (String) props.get("tableTypes");

		if (types == null)
			return true;

		for (var type : types.split(",")) {
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

	// Preset menu item: open browser
	protected JMenuItem getOpenLinkMenu(Object url) {
		var openLinkItem = new JMenuItem();
		openLinkItem.setText("Open URL in web browser...");

		if (url == null) {
			openLinkItem.setEnabled(false);
		} else if(!url.toString().startsWith("http:") && !url.toString().startsWith("https:")) {
			openLinkItem.setEnabled(false);
		} else {
			openLinkItem.addActionListener(evt -> {
				var openBrowser = serviceRegistrar.getService(OpenBrowser.class);
				openBrowser.openURL(url.toString());
			});
		}

		return openLinkItem;
	}

	private void selectElementsFromSelectedRows(JTable table, Class<? extends CyIdentifiable> tableType) {
		var t = new Thread() {
			@Override
			public void run() {
				var applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
				var net = applicationManager.getCurrentNetwork();

				if (net != null) {
					var tableModel = (BrowserTableModel) table.getModel();
					int[] selectedRows = table.getSelectedRows();
					var targetRows = new HashSet<CyRow>();

					for (int rowIndex : selectedRows) {
						// Getting the row from data table solves the problem with hidden or moved SUID column.
						// However, since the rows might be sorted we need to convert the index to model.
						var selected = (ValidatedObjectAndEditString) tableModel
								.getValueAt(table.convertRowIndexToModel(rowIndex), CyNetwork.SUID);
						targetRows.add(tableModel.getCyRow(selected.getValidatedObject()));
					}

					var cyTable = tableType == CyNode.class ? net.getDefaultNodeTable() : net.getDefaultEdgeTable();

					for (var cyRow : cyTable.getAllRows())
						cyRow.set(CyNetwork.SELECTED, targetRows.contains(cyRow));

					var view = applicationManager.getCurrentNetworkView();

					if (view != null) {
						serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();
						view.updateView();
					}
				}
			}
		};
		t.start();
	}
	
	private boolean getBooleanProperty(Map<?, ?> props, String property) {
		var value = (String) props.get(property); // get the property

		if (value == null || value.length() == 0)
			return false;
		
		try {
			return Boolean.parseBoolean(value);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Hides duplicate separators.
	 */
	private static void sanitize(JPopupMenu menu) {
		boolean hasSeparator = false;
		
		for (int i = 0; i < menu.getComponentCount(); i++) {
			var comp = menu.getComponent(i);
			
			if (comp instanceof JSeparator) {
				// Already has one separator? So hide this one.
				// Also hide if it's the first or last component.
				if (hasSeparator || i == 0 || i == menu.getComponentCount() - 1)
					comp.setVisible(false);
				else
					hasSeparator = true;
			} else if (comp.isVisible()) {
				hasSeparator = false;
			}
		}
	}
	
	/**
	 * A simple action that executes the specified TaskFactory
	 */
	@SuppressWarnings("serial")
	private class PopupAction extends AbstractAction {

		private final TaskFactory tf;

		PopupAction(TaskFactory tf, String menuLabel) {
			super(menuLabel);
			this.tf = tf;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			var taskManager = serviceRegistrar.getService(TaskManager.class);
			
			if (taskManager != null && tf != null)
				taskManager.execute(tf.createTaskIterator());
		}
	}
}
