package org.cytoscape.internal.view;

import static org.cytoscape.internal.view.util.ViewUtil.invokeOnEDT;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.internal.actions.DestroyNetworksAction;
import org.cytoscape.internal.model.RootNetworkManager;
import org.cytoscape.internal.task.DynamicTaskFactory;
import org.cytoscape.internal.task.DynamicTogglableTaskFactory;
import org.cytoscape.internal.task.TaskFactoryTunableAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedEdgesListener;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedNodesListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.RootNetworkCollectionTaskFactory;
import org.cytoscape.task.edit.EditNetworkTitleTaskFactory;
import org.cytoscape.util.swing.JMenuTracker;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.Togglable;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

/**
 * This class mediates the communication between the Network UI and the rest of Cytoscape.
 */
public class NetworkMediator implements NetworkAddedListener, NetworkViewAddedListener,
		NetworkAboutToBeDestroyedListener, NetworkDestroyedListener, NetworkViewDestroyedListener, RowsSetListener,
		AddedNodesListener, AddedEdgesListener, RemovedEdgesListener, RemovedNodesListener,
		SessionAboutToBeLoadedListener, SessionLoadedListener {

	private final Map<Object, TaskFactory> provisionerMap = new HashMap<>();
	private final Map<Object, CyAction> netPopupActionMap = new WeakHashMap<>();
	private final Map<Object, CyAction> rootPopupActionMap = new WeakHashMap<>();
	
	private boolean loadingSession;
	
	private final NetworkMainPanel networkMainPanel;
	private final RootNetworkManager rootNetManager;
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkMediator(NetworkMainPanel networkMainPanel, RootNetworkManager rootNetManager,
			CyServiceRegistrar serviceRegistrar) {
		this.networkMainPanel = networkMainPanel;
		this.rootNetManager = rootNetManager;
		this.serviceRegistrar = serviceRegistrar;
		
		networkMainPanel.addPropertyChangeListener("rootNetworkPanelCreated", evt -> {
			var p = (RootNetworkPanel) evt.getNewValue();
			addMouseListenersForSelection(p, p.getHeaderPanel(), p.getNetworkCountLabel(), p.getNameLabel(), p);
		});
		networkMainPanel.addPropertyChangeListener("subNetworkPanelCreated", evt -> {
			var p = (SubNetworkPanel) evt.getNewValue();
			addMouseListenersForSelection(p, p.getNameLabel(), p.getViewIconLabel(), p.getViewCountLabel(),
					p.getNodeCountLabel(), p.getEdgeCountLabel(), p);
		});
	}
	
	public CyNetwork getCurrentNetwork() {
		return networkMainPanel.getCurrentNetwork();
	}
	
	// // Event handlers // //

	@Override
	public void handleEvent(final SessionAboutToBeLoadedEvent e) {
		loadingSession = true;
	}

	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		loadingSession = false;
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		if (e.getNetwork() instanceof CySubNetwork) {
			CySubNetwork network = (CySubNetwork) e.getNetwork();
			networkMainPanel.removeNetwork(network);
			networkMainPanel.updateNodeEdgeCount();
			
			CyRootNetwork rootNetwork = network.getRootNetwork();
			if(rootNetwork.getSubNetworkList().size() == 1 && rootNetwork.equals(networkMainPanel.getCurrentNetwork())) {
				networkMainPanel.setCurrentNetwork(null);
			}
		}
	}

	@Override
	public void handleEvent(NetworkDestroyedEvent e) {
		invokeOnEDT(() -> {
			networkMainPanel.getRootNetworkListPanel().update();
			networkMainPanel.updateCollapseExpandButtons();
		});
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		if (loadingSession)
			return;

		var net = e.getNetwork();

		invokeOnEDT(() -> {
			if (net instanceof CySubNetwork) {
				networkMainPanel.addNetwork((CySubNetwork) net);
				networkMainPanel.updateNodeEdgeCount();
			}
		});
	}

	@Override
	public void handleEvent(RowsSetEvent e) {
		if (loadingSession || networkMainPanel.getRootNetworkListPanel().isEmpty())
			return;

		// We only care about network name changes
		var nameRecords = e.getColumnRecords(CyNetwork.NAME);

		if (nameRecords == null || nameRecords.isEmpty())
			return;

		var tbl = e.getSource();
		var netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		var net = netTblMgr.getNetworkForTable(tbl);

		// And if there is no related network, nothing needs to be done
		if (net != null && tbl.equals(net.getDefaultNetworkTable())) {
			invokeOnEDT(() -> {
				var item = networkMainPanel.getNetworkItem(net);

				if (item != null)
					item.update();
			});
		}
	}

	@Override
	public void handleEvent(AddedEdgesEvent e) {
		networkMainPanel.updateNodeEdgeCount();
	}

	@Override
	public void handleEvent(AddedNodesEvent e) {
		networkMainPanel.updateNodeEdgeCount();
	}

	@Override
	public void handleEvent(RemovedNodesEvent e) {
		networkMainPanel.updateNodeEdgeCount();
	}

	@Override
	public void handleEvent(RemovedEdgesEvent e) {
		networkMainPanel.updateNodeEdgeCount();
	}

	@Override
	public void handleEvent(NetworkViewDestroyedEvent e) {
		if (loadingSession)
			return;
		
		invokeOnEDT(() -> {
			var netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			
			for (var snp : networkMainPanel.getAllSubNetworkItems()) {
				int count = netViewMgr.getNetworkViews(snp.getModel().getNetwork()).size();
				snp.getModel().setViewCount(count);
			}
			
			networkMainPanel.getRootNetworkListPanel().update();
		});
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		if (loadingSession)
			return;

		invokeOnEDT(() -> {
			updateViewCount(e.getNetworkView());
		});
	}
	
	public void addRootNetworkCollectionTaskFactory(RootNetworkCollectionTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> {
			var provisioner = factory instanceof Togglable ?
					new DynamicTogglableTaskFactory(factory, rootNetManager) :
					new DynamicTaskFactory(factory, rootNetManager);
			
			provisionerMap.put(factory, provisioner);
			addFactory(provisioner, props, true);
		});
	}
	
	public void removeRootNetworkCollectionTaskFactory(RootNetworkCollectionTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> {
			removeFactory(provisionerMap.remove(factory), false);
		});
	}
	
	public void addNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> {
			var factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);
			var provisioner = factoryProvisioner.createFor(factory);
			provisionerMap.put(factory, provisioner);
			addFactory(provisioner, props, false);
		});
	}

	public void removeNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> {
			removeFactory(provisionerMap.remove(factory), false);
		});
	}

	public void addNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> {
			var factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);
			var provisioner = factoryProvisioner.createFor(factory);
			provisionerMap.put(factory, provisioner);
			addFactory(provisioner, props, false);
		});
	}

	public void removeNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> {
			removeFactory(provisionerMap.remove(factory), false);
		});
	}

	public void addNetworkTaskFactory(NetworkTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> {
			var factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);
			var provisioner = factoryProvisioner.createFor(factory);
			provisionerMap.put(factory, provisioner);
			addFactory(provisioner, props, false);
		});
	}

	public void removeNetworkTaskFactory(NetworkTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> {
			removeFactory(provisionerMap.remove(factory), false);
		});
	}
	
	public void addNetworkViewTaskFactory(NetworkViewTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> {
			var factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);
			var provisioner = factoryProvisioner.createFor(factory);
			provisionerMap.put(factory, provisioner);
			addFactory(provisioner, props, false);
		});
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory factory, Map<?, ?> props) {
		invokeOnEDT(() -> removeFactory(provisionerMap.remove(factory), false));
	}
	
	public void addCyAction(CyAction action, Map<?, ?> props) {
		invokeOnEDT(() -> netPopupActionMap.put(action, action));
	}
	
	public void removeCyAction(CyAction action, Map<?, ?> props) {
		invokeOnEDT(() -> netPopupActionMap.remove(action));
	}
	
	// // Private Methods // //
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addFactory(TaskFactory factory, Map props, boolean rootPopup) {
		final CyAction action;
		
		if (props.containsKey(ServiceProperties.ENABLE_FOR))
			action = new TaskFactoryTunableAction(factory, props, serviceRegistrar);
		else
			action = new TaskFactoryTunableAction(serviceRegistrar, factory, props);

		if (rootPopup)
			rootPopupActionMap.put(factory, action);
		else
			netPopupActionMap.put(factory, action);
	}
	
	private void removeFactory(TaskFactory factory, boolean rootPopup) {
		if (rootPopup)
			rootPopupActionMap.remove(factory);
		else
			netPopupActionMap.remove(factory);
	}
	
	private void addMouseListenersForSelection(AbstractNetworkPanel<?> item, JComponent... components) {
		// This mouse listener listens for mouse pressed events to select the list items
		var selectionListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				networkMainPanel.onMousePressedItem(e, item);
			}
		};
		
		// This mouse listener listens for the right-click events to show the pop-up window
		var popupListener = new PopupListener(item);
		
		for (var c : components) {
			c.addMouseListener(selectionListener);
			c.addMouseListener(popupListener);
		}
	}
	
	private void updateViewCount(CyNetworkView view) {
		var subNetPanel = networkMainPanel.getSubNetworkPanel(view.getModel());

		if (subNetPanel != null) {
			int count = serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViews(view.getModel()).size();
			subNetPanel.getModel().setViewCount(count);
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
	
	// // Classes // //
	
	/**
	 * This class listens to mouse events from the TreeTable, if the mouse event
	 * is one that is canonically associated with a popup menu (ie, a right
	 * click) it will pop up the menu with option for destroying view, creating
	 * view, and destroying network (this is platform specific apparently)
	 */
	private final class PopupListener extends MouseAdapter {

		final AbstractNetworkPanel<?> item;
		
		PopupListener(AbstractNetworkPanel<?> item) {
			this.item = item;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopupMenu(e);
		}

		// On Windows, popup is triggered by mouse release, not press 
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopupMenu(e);
		}

		/**
		 * if the mouse press is of the correct type, this function will maybe display the popup
		 */
		private final void maybeShowPopupMenu(MouseEvent e) {
			// Ignore if not valid trigger.
			if (!e.isPopupTrigger())
				return;

			// If the item is not selected, select it first
			var selectedItems = networkMainPanel.getSelectedItems();
			
			if (!selectedItems.contains(item)) {
				networkMainPanel.selectAndSetCurrent(item);
				selectedItems = networkMainPanel.getSelectedItems();
			}
			
			var taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
			var network = item.getModel().getNetwork();
			var popup = new JPopupMenu();
			
			if (network instanceof CySubNetwork) {
				addMenuItems(popup, netPopupActionMap.values());
			} else {
				// Basic actions for root-networks
				{
					var mi = new JMenuItem("Rename Network Collection...");
					mi.addActionListener(evt -> {
						var factory = serviceRegistrar.getService(EditNetworkTitleTaskFactory.class);
						taskMgr.execute(factory.createTaskIterator(network));
					});
					popup.add(mi);
					mi.setEnabled(selectedItems.size() == 1);
				}
				{
					var action = new DestroyNetworksAction(0.0f, networkMainPanel, serviceRegistrar);
					var mi = new JMenuItem(action);
					popup.add(mi);
					action.updateEnableState();
				}
				
				if (!rootPopupActionMap.isEmpty()) {
					popup.addSeparator();
					addMenuItems(popup, rootPopupActionMap.values());
				}
			}
			
			sanitize(popup);
			
			popup.addPropertyChangeListener("visible", ev -> {
				boolean visible = Boolean.TRUE.equals(ev.getNewValue());
				
				if (!visible)
					popup.setInvoker(null); // avoid memory leak
			});
			popup.show(e.getComponent(), e.getX(), e.getY());
		}

		private void addMenuItems(JPopupMenu popup, Collection<CyAction> actions) {
			// Sort the actions by gravity first
			var menuTracker = new JMenuTracker(popup);
			var menuString = ".";

			// Create the menu items
			for (var a : actions) {
				var mi = a.useCheckBoxMenuItem() ? new JCheckBoxMenuItem(a) : new JMenuItem(a);
				var gravityTracker = menuTracker.getGravityTracker(menuString);
				
				if (a.insertSeparatorBefore())
					gravityTracker.addMenuSeparator(a.getMenuGravity() - .0001);

				gravityTracker.addMenuItem(mi, a.getMenuGravity());

				if (a.insertSeparatorAfter())
					gravityTracker.addMenuSeparator(a.getMenuGravity() + .0001);
				
				a.updateEnableState();
				
				if (a instanceof PopupMenuListener)
					popup.addPopupMenuListener((PopupMenuListener) a);
			}
		}
	}
}
