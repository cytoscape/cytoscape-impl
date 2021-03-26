package org.cytoscape.ding.impl;

import static org.cytoscape.work.ServiceProperties.APPS_MENU;
import static org.cytoscape.work.ServiceProperties.EDGE_APPS_MENU;
import static org.cytoscape.work.ServiceProperties.EDGE_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.EDGE_LINKOUTS_MENU;
import static org.cytoscape.work.ServiceProperties.EDGE_PREFERENCES_MENU;
import static org.cytoscape.work.ServiceProperties.EDGE_SELECT_MENU;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NETWORK_ADD_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_APPS_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_DELETE_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_GROUP_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_PREFERENCES_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_SELECT_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_APPS_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_GROUP_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_LINKOUTS_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_NESTED_NETWORKS_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_PREFERENCES_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_SELECT_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_ACTION;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.SMALL_ICON_ID;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.JMenuTracker;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.Togglable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

// TODO Consider generalizing this class so that it can be used by anyone
// who needs a popup menu based on TaskFactories.

/**
 * A class that encapsulates the creation of JPopupMenus based
 * on TaskFactory services.
 */
class PopupMenuHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(PopupMenuHelper.class);

	// MKTODO replace with an enum
	public static final String ACTION_NEW  = "NEW";
	public static final String ACTION_OPEN = "OPEN";
	
	/** the component we should create the popup menu on */
	private Component invoker;

	private final StaticTaskFactoryProvisioner factoryProvisioner;

	private DRenderingEngine re;
	private final ViewTaskFactoryListener vtfl;
	private final CyServiceRegistrar serviceRegistrar;

	PopupMenuHelper(DRenderingEngine re, Component inv, CyServiceRegistrar serviceRegistrar) {
		this.re = re;
		invoker = inv;
		this.serviceRegistrar = serviceRegistrar;
		this.vtfl = serviceRegistrar.getService(ViewTaskFactoryListener.class);
		factoryProvisioner = new StaticTaskFactoryProvisioner();
	}

	/**
	 * Creates a menu based on the EdgeView.
	 *
	 * @param action Acceptable values are "NEW" or "OPEN." Case does not matter.
	 */
	void createEdgeViewMenu(View<CyEdge> edgeView, int x, int y, String action) {
		edgeView = re.getViewModelSnapshot().getMutableEdgeView(edgeView.getSUID());
		
		if (edgeView != null) {
			var usableTFs = getPreferredActions(vtfl.getEdgeViewTaskFactoryMap(), action);
			var usableCMFs = getPreferredActions(vtfl.getCyEdgeViewContextMenuFactoryMap(), action);
			
			// remove TaskFactories that can't be executed from double-click menu
			if (action.equalsIgnoreCase("OPEN")) {
				var i = usableTFs.iterator();
				
				while (i.hasNext()) {
					if (!i.next().isReady(edgeView, re.getViewModel()))
						i.remove();
				}
			}
			
			int tfCount = usableTFs.size();
			int menuItemCount = usableTFs.size()+ usableCMFs.size();

			if (action.equalsIgnoreCase("OPEN") && menuItemCount == 1 && tfCount == 1) {
				var tf = usableTFs.iterator().next();
				serviceRegistrar.getService(DialogTaskManager.class).execute(tf.createTaskIterator(edgeView, re.getViewModel()));
			} else {
				var edgeLabel = re.getViewModel().getModel().getRow(edgeView.getModel()).get(CyEdge.INTERACTION, String.class);
				var menu = createMenu(edgeLabel);
				var tracker = new JMenuTracker(menu);

				if (!action.equalsIgnoreCase("OPEN")) {
					initializeEdgeTracker(tracker);
					tracker.getGravityTracker(".").addMenuSeparator(-0.1);
					tracker.getGravityTracker(".").addMenuSeparator(999.99);
				}

				for (var evtf : usableTFs) {
					Object context = null;
					var provisioner = factoryProvisioner.createFor(evtf, edgeView, re.getViewModel());
					addMenuItem(edgeView, menu, provisioner, context, tracker, vtfl.getEdgeViewTaskFactoryMap().get(evtf));
				}

				for (var edgeCMF : usableCMFs) {
					// menu.add(edgeCMF.createMenuItem(m_view, ev).getMenuItem());
					try {
						var menuItem = edgeCMF.createMenuItem(re.getViewModel(), edgeView);
						addCyMenuItem(edgeView, menu, menuItem, tracker, vtfl.getCyEdgeViewContextMenuFactoryMap().get(edgeCMF));
					} catch (Throwable t) {
						logger.error("Could not display context menu.", t);
					}
				}
				
				sanitize(menu);
				
				menu.show(invoker, x, y);
			}
		}
	}

	/**
	 * Creates a menu based on the NodeView.
	 *
	 * @param action Acceptable values are "NEW", "OPEN", or "EDGE". Case does not matter.
	 */
	void createNodeViewMenu(View<CyNode> nodeView, int x, int y, String action) {
		nodeView = re.getViewModelSnapshot().getMutableNodeView(nodeView.getSUID()); // get the mutable node view
		
		if (nodeView != null) {
			var usableTFs = getPreferredActions(vtfl.getNodeViewTaskFactoryMap(), action);
			var usableCMFs = getPreferredActions(vtfl.getCyNodeViewContextMenuFactoryMap(),action);
			
			// If the action is NEW, we should also include the Edge Actions
			if (action.equalsIgnoreCase("NEW")) {
				usableTFs.addAll(getPreferredActions(vtfl.getNodeViewTaskFactoryMap(),"Edge"));
				usableCMFs.addAll(getPreferredActions(vtfl.getCyNodeViewContextMenuFactoryMap(),"Edge"));
			} else if(action.equalsIgnoreCase("OPEN")) {
				// remove TaskFactories that can't be executed from double-click menu
				var i = usableTFs.iterator();
				
				while (i.hasNext()) {
					if (!i.next().isReady(nodeView, re.getViewModel()))
						i.remove();
				}
			}

			int menuItemCount = usableTFs.size() + usableCMFs.size();
			int tfCount = usableTFs.size();

			if ((action.equalsIgnoreCase("OPEN") || action.equalsIgnoreCase("Edge")) && menuItemCount == 1 && tfCount == 1) {
				var tf = usableTFs.iterator().next();
				serviceRegistrar.getService(DialogTaskManager.class).execute(tf.createTaskIterator(nodeView,re.getViewModel()));
			} else {
				var nodeLabel = re.getViewModel().getModel().getRow(nodeView.getModel()).get(CyNetwork.NAME, String.class);
				var menu = createMenu(nodeLabel);
				var tracker = new JMenuTracker(menu);

				if (!action.equalsIgnoreCase("OPEN")) {
					initializeNodeTracker(tracker);
					tracker.getGravityTracker(".").addMenuSeparator(-0.1);
					tracker.getGravityTracker(".").addMenuSeparator(999.99);
				}

				for (var nvtf : usableTFs) {
					Object context = null;
					var provisioner = factoryProvisioner.createFor(nvtf, nodeView, re.getViewModel());
					addMenuItem(nodeView, menu, provisioner, context, tracker, vtfl.getNodeViewTaskFactoryMap().get(nvtf));
				}

				for (var nodeCMF : usableCMFs) {
					// menu.add(nodeVMF.createMenuItem(m_view,  nv).getMenuItem());
					try {
						var menuItem = nodeCMF.createMenuItem(re.getViewModel(), nodeView);
						addCyMenuItem(nodeView, menu, menuItem, tracker, vtfl.getCyNodeViewContextMenuFactoryMap().get(nodeCMF));
					} catch (Throwable t) {
						logger.error("Could not display context menu.", t);
					}
				}
				
				sanitize(menu);
				
				menu.show(invoker, x, y);
			}
		}
	}

	/**
	 * Creates a menu based on the NetworkView.
	 *
	 * @param action Acceptable values are "NEW" or "OPEN." Case does not matter.
	 */
	void createNetworkViewMenu(Point rawPt, Point xformPt, String action) {
		var usableTFs = getPreferredActions(vtfl.getEmptySpaceTaskFactoryMap(), action);
		var usableTFs2 = getPreferredActions(vtfl.getNetworkViewLocationTaskFactoryMap(), action);
		var usableCMFs = getPreferredActions(vtfl.getCyNetworkViewContextMenuFactoryMap(), action);
		
		var graphView = re.getViewModel();
		
		// remove TaskFactories that can't be executed from double-click menu
		if (action.equalsIgnoreCase("OPEN")) {
			var i = usableTFs.iterator();
			
			while (i.hasNext()) {
				if (!i.next().isReady(graphView))
					i.remove();
			}
			
			var i2 = usableTFs2.iterator();
			
			while (i2.hasNext()) {
				if (!i2.next().isReady(graphView, rawPt, xformPt))
					i2.remove();
			}
		}
		
		int menuItemCount = usableTFs.size() + usableTFs2.size() + usableCMFs.size();
		int tfCount = usableTFs.size() + usableTFs2.size();

		if (action.equalsIgnoreCase("OPEN") && menuItemCount == 1 && tfCount == 1){
			// Double click on open space and there is only one menu item, execute it
			var taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			
			if (usableTFs.size() == 1) {
				var tf = usableTFs.iterator().next();
				taskManager.execute(tf.createTaskIterator(graphView));
			} else if (usableTFs2.size() == 1) {
				var tf = usableTFs2.iterator().next();
				taskManager.execute(tf.createTaskIterator(graphView, rawPt, xformPt));
			}
		} else {
			var menu = createMenu("Double Click Menu: empty");
			var tracker = new JMenuTracker(menu);

			if (!action.equalsIgnoreCase("OPEN")) {
				initializeNetworkTracker(tracker);
				tracker.getGravityTracker(".").addMenuSeparator(-0.1);
				tracker.getGravityTracker(".").addMenuSeparator(999.99);
			}

			for (var nvtf : usableTFs) {
				var provisioner = factoryProvisioner.createFor(nvtf, graphView);
				addMenuItem(null, menu, provisioner, null, tracker, vtfl.getEmptySpaceTaskFactoryMap().get(nvtf));
			}

			for (var nvltf : usableTFs2) {
				var provisioner = factoryProvisioner.createFor(nvltf, graphView, rawPt, xformPt);
				addMenuItem(null, menu, provisioner, null, tracker, vtfl.getNetworkViewLocationTaskFactoryMap().get(nvltf));
			}
			
			for (var netVMF: usableCMFs) {
				try {
					var menuItem = netVMF.createMenuItem(graphView);
					addCyMenuItem(graphView, menu, menuItem, tracker, vtfl.getCyNetworkViewContextMenuFactoryMap().get(netVMF));
				} catch (Throwable t) {
					logger.error("Could not display context menu." , t);
				}
			}
			
			sanitize(menu);
			
			// There are more than one menu item, let user make the selection
			menu.show(invoker,(int) rawPt.getX(), (int) rawPt.getY());		
		}
	}

	private JPopupMenu createMenu(String title) {
		var menu = new JPopupMenu(title);
		menu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				// Ignore...
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				menu.removeAll();
			}
			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				menu.removeAll();
			}
		});
		
		return menu;
	}
	
	/**
	 * This method creates popup menu submenus and menu items based on the
	 * "title" and "preferredMenu" keywords, depending on which are present
	 * in the service properties.
	 */
	private void addMenuItem(View<?> view, JPopupMenu popup, NamedTaskFactory tf, Object tunableContext,
			JMenuTracker tracker, Map<?, ?> props) {
		var title = (String) props.get(TITLE);
		var pref = (String) props.get(PREFERRED_MENU);
		var toolTip = (String) props.get(TOOLTIP);
		var menuGravity = (String) props.get(MENU_GRAVITY);
		var prefAction = (String) props.get(PREFERRED_ACTION);
		boolean insertSepBefore = getBooleanProperty(props, INSERT_SEPARATOR_BEFORE);
		boolean insertSepAfter = getBooleanProperty(props, INSERT_SEPARATOR_AFTER);
		var iconId = (String) props.get(SMALL_ICON_ID);

		if ("View".equalsIgnoreCase(pref))
			return; // TODO Should we show 'View' options here (e.g. zoom in/out, fit selected)?
		
		double gravity = menuGravity != null ? Double.parseDouble(menuGravity) : -1; // Alphabetize by default

		if (pref == null) {
			if (prefAction != null && prefAction.equalsIgnoreCase("OPEN"))
				pref = ".";
			else
				pref = APPS_MENU;
		}
		
		Icon icon = null;
		
		if (iconId != null && !iconId.toString().trim().isEmpty())
			icon = serviceRegistrar.getService(IconManager.class).getIcon(iconId.toString());
		
		if (title == null) { // No title
			int last = pref.lastIndexOf(".");

			// if the preferred menu is delimited
			if (last > 0) {
				title = pref.substring(last + 1);
				pref = pref.substring(0, last);

				if (APPS_MENU.equals(title))
					return;

				var gravityTracker = tracker.getGravityTracker(pref);
				var item = createMenuItem(tf, title, toolTip, icon);

				if (insertSepBefore)
					gravityTracker.addMenuSeparator(gravity - .0001);

				gravityTracker.addMenuItem(item, gravity);

				if (insertSepAfter)
					gravityTracker.addMenuSeparator(gravity + .0001);
			} else {
				// otherwise just use the preferred menu as the menu item name
				title = pref;
				
				if (APPS_MENU.equals(title))
					return;
				
				var gravityTracker = tracker.getGravityTracker(pref);
				var item = createMenuItem(tf, title, toolTip, icon);
				gravityTracker.addMenuItem(item, gravity);
			}
		} else { // Title and preferred menu
			var gravityTracker = tracker.getGravityTracker(pref);
			
			if (insertSepBefore)
				gravityTracker.addMenuSeparator(gravity - .0001);

			gravityTracker.addMenuItem(createMenuItem(tf, title, toolTip, icon), gravity);

			if (insertSepAfter)
				gravityTracker.addMenuSeparator(gravity + .0001);
		}
	}

	/**
	 * This method creates popup menu submenus and menu items based on the
	 * "preferredMenu" keyword.  
	 */
	private void addCyMenuItem(View<?> view, JPopupMenu popup, CyMenuItem menuItem, JMenuTracker tracker, Map<?, ?> props) {
		String pref = null;
		
		if (props != null)
			pref = (String)(props.get(PREFERRED_MENU));

		if (pref == null)
			pref = APPS_MENU;

		// This is a *very* special case we used to help with Dynamic Linkout
		if (pref.equalsIgnoreCase(menuItem.getMenuItem().getText()) && menuItem.getMenuItem() instanceof JMenu) {
			var gravityTracker = tracker.getGravityTracker(pref);
			var menu = (JMenu) menuItem.getMenuItem();
			
			for (int menuIndex = 0; menuIndex < menu.getItemCount(); menuIndex++) {
				var item = menu.getItem(menuIndex);
				gravityTracker.addMenuItem(item, -1);
			}
			
			return;
		}

		var gravityTracker = tracker.getGravityTracker(pref);
		gravityTracker.addMenuItem(menuItem.getMenuItem(), menuItem.getMenuGravity());
	}
	
	// We need to "seed" the tracker menu or we wind up with
	// some very unfortunate order effects if a bundle doesn't
	// use the right context menu specifiers
	private void initializeNetworkTracker(JMenuTracker tracker) {
		tracker.getGravityTracker(NETWORK_ADD_MENU);
		tracker.getGravityTracker(NETWORK_DELETE_MENU);
		tracker.getGravityTracker(NETWORK_EDIT_MENU);
		tracker.getGravityTracker(NETWORK_SELECT_MENU);
		tracker.getGravityTracker(NETWORK_GROUP_MENU);
		// tracker.getGravityTracker(NETWORK_LAYOUT_MENU);
		tracker.getGravityTracker(NETWORK_APPS_MENU);
		tracker.getGravityTracker(NETWORK_PREFERENCES_MENU);
	}
	
	// We need to "seed" the tracker menu or we wind up with
	// some very unfortunate order effects if a bundle doesn't
	// use the right context menu specifiers
	private void initializeNodeTracker(JMenuTracker tracker) {
		tracker.getGravityTracker(NODE_EDIT_MENU);
		tracker.getGravityTracker(NODE_SELECT_MENU);
		tracker.getGravityTracker(NODE_GROUP_MENU);
		tracker.getGravityTracker(NODE_NESTED_NETWORKS_MENU);
		tracker.getGravityTracker(NODE_APPS_MENU);
		tracker.getGravityTracker(NODE_LINKOUTS_MENU);
		// tracker.getGravityTracker(NODE_DYNAMIC_LINKOUTS_MENU);
		tracker.getGravityTracker(NODE_PREFERENCES_MENU);
	}
	
	// We need to "seed" the tracker menu or we wind up with
	// some very unfortunate order effects if a bundle doesn't
	// use the right context menu specifiers
	private void initializeEdgeTracker(JMenuTracker tracker) {
		tracker.getGravityTracker(EDGE_EDIT_MENU);
		tracker.getGravityTracker(EDGE_SELECT_MENU);
		tracker.getGravityTracker(EDGE_APPS_MENU);
		tracker.getGravityTracker(EDGE_LINKOUTS_MENU);
		// tracker.getGravityTracker(EDGE_DYNAMIC_LINKOUTS_MENU);
		tracker.getGravityTracker(EDGE_PREFERENCES_MENU);
	}

	private JMenuItem createMenuItem(TaskFactory tf, String title, String toolTipText, Icon icon) {
		var action = new PopupAction(tf, title);
		final JMenuItem item;
		
		if (tf instanceof Togglable) {
			item = new JCheckBoxMenuItem(action);
			((JCheckBoxMenuItem) item).setSelected(tf.isOn());
		} else {
			item = new JMenuItem(action);
		}

		boolean ready = tf.isReady();
		item.setEnabled(ready);
		action.setEnabled(ready);

		item.setToolTipText(toolTipText);
		item.setIcon(icon);
		
		return item;
	}

	/**
	 * Extract and return all T's that match the defined action.  If action is null, then return everything.
	 */
	private <T> Collection<T> getPreferredActions(Map<T, Map<String,String>> tfs, String action) {
		// if the action is null, return all available
		if (action == null)
			return tfs.keySet();

		// otherwise figure out if any TaskFactories match the specified preferred action
		var usableTFs = new ArrayList<T>();
		
		for (var evtf : tfs.keySet()) {
			var prefAction = (String) tfs.get(evtf).get(PREFERRED_ACTION);
			
			// assume action is NEW if no action specified
			if (prefAction == null)
				prefAction = "NEW";
			if (action.equalsIgnoreCase(prefAction))
				usableTFs.add(evtf);
		}
		
		return usableTFs;
	}

	/**
 	 * Get a boolean property
 	 */
	private boolean getBooleanProperty(Map<?, ?> props, String property) {
		var value = (String) props.get(property);

		if (value == null || value.length() == 0)
			return false;
		
		try {
			return Boolean.parseBoolean(value);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Hides duplicate separators and disables empty menus.
	 */
	private static void sanitize(JPopupMenu popupMenu) {
		for (var comp : popupMenu.getComponents()) {
			if (comp instanceof JMenu) {
				boolean hasSeparator = false;
				boolean hasMenuItem = false;
				
				var menu = (JMenu) comp;
				int i = 0;
				
				for (var mc : menu.getMenuComponents()) {
					if (mc instanceof JSeparator) {
						// Already has one separator? So hide this one.
						// Also hide if it's the first or last component.
						if (hasSeparator || i == 0 || i == menu.getItemCount() - 1)
							mc.setVisible(false);
						else
							hasSeparator = true;
					} else if (mc.isVisible()) {
						hasSeparator = false;
						hasMenuItem = true;
					}
					
					i++;
				}
				
				if (!hasMenuItem)
					comp.setEnabled(false);
			}
		}
	}
	
	/**
	 * A simple action that executes the specified TaskFactory
	 */
	@SuppressWarnings("serial")
	private class PopupAction extends AbstractAction {

		TaskFactory tf;

		PopupAction(TaskFactory tf, String title) {
			super(title);
			this.tf = tf;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			serviceRegistrar.getService(DialogTaskManager.class).execute(tf.createTaskIterator());
		}
	}

	public void dispose() {
		re = null;
	}
}
