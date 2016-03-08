package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.internal.task.TaskFactoryTunableAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
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
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
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
import org.cytoscape.task.edit.EditNetworkTitleTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
public class NetworkMediator
		implements NetworkAddedListener, NetworkViewAddedListener, NetworkAboutToBeDestroyedListener,
		NetworkDestroyedListener, NetworkViewDestroyedListener, RowsSetListener, AddedNodesListener, AddedEdgesListener,
		RemovedEdgesListener, RemovedNodesListener, SessionAboutToBeLoadedListener, SessionLoadedListener {

	private final JPopupMenu popup;
	private JMenuItem editRootNetworTitle;
	
	private final Map<Object, JMenuItem> popupMap = new WeakHashMap<>();
	private HashMap<JMenuItem, Double> actionGravityMap = new HashMap<>();
	
	private final Map<Object, TaskFactory> provisionerMap = new HashMap<>();
	private final Map<Object, CyAction> popupActionMap = new WeakHashMap<>();
	
	private boolean loadingSession;
	
	private NetworkMainPanel networkMainPanel;
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkMediator(final NetworkMainPanel networkMainPanel, final CyServiceRegistrar serviceRegistrar) {
		this.networkMainPanel = networkMainPanel;
		this.serviceRegistrar = serviceRegistrar;
		
		popup = new JPopupMenu();
		
		networkMainPanel.addPropertyChangeListener("rootNetworkPanelCreated", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				final RootNetworkPanel p = (RootNetworkPanel) evt.getNewValue();
				addMouseListenersForSelection(p, p.getHeaderPanel(), p.getNetworkCountLabel(), p.getNameLabel());
			}
		});
		networkMainPanel.addPropertyChangeListener("subNetworkPanelCreated", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				final SubNetworkPanel p = (SubNetworkPanel) evt.getNewValue();
				addMouseListenersForSelection(p, p, p.getNameLabel(), p.getViewIconLabel());
			}
		});
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
	public void handleEvent(final NetworkAboutToBeDestroyedEvent e) {
		if (e.getNetwork() instanceof CySubNetwork) {
			networkMainPanel.removeNetwork((CySubNetwork) e.getNetwork());
			networkMainPanel.updateNodeEdgeCount();
		}
	}

	@Override
	public void handleEvent(final NetworkDestroyedEvent e) {
		invokeOnEDT(() -> {
			networkMainPanel.getRootNetworkListPanel().update();
			networkMainPanel.updateCollapseExpandButtons();
		});
	}

	@Override
	public void handleEvent(final NetworkAddedEvent e) {
		if (loadingSession)
			return;

		final CyNetwork net = e.getNetwork();

		invokeOnEDT(() -> {
			if (net instanceof CySubNetwork) {
				final SubNetworkPanel snp = networkMainPanel.addNetwork((CySubNetwork) net);
				networkMainPanel.selectAndSetCurrent(snp);
				networkMainPanel.updateNodeEdgeCount();
			}
		});
	}

	@Override
	public void handleEvent(final RowsSetEvent e) {
		if (loadingSession || networkMainPanel.getRootNetworkListPanel().isEmpty())
			return;

		// We only care about network name changes
		final Collection<RowSetRecord> nameRecords = e.getColumnRecords(CyNetwork.NAME);

		if (nameRecords == null || nameRecords.isEmpty())
			return;

		final CyTable tbl = e.getSource();
		final CyNetworkTableManager netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		final CyNetwork net = netTblMgr.getNetworkForTable(tbl);

		// And if there is no related network, nothing needs to be done
		if (net != null && tbl.equals(net.getDefaultNetworkTable())) {
			invokeOnEDT(() -> {
				final AbstractNetworkPanel<?> item = networkMainPanel.getNetworkItem(net);

				if (item != null)
					item.update();
			});
		}
	}

	@Override
	public void handleEvent(final AddedEdgesEvent e) {
		networkMainPanel.updateNodeEdgeCount();
	}

	@Override
	public void handleEvent(final AddedNodesEvent e) {
		networkMainPanel.updateNodeEdgeCount();
	}

	@Override
	public void handleEvent(final RemovedNodesEvent e) {
		networkMainPanel.updateNodeEdgeCount();
	}

	@Override
	public void handleEvent(final RemovedEdgesEvent e) {
		networkMainPanel.updateNodeEdgeCount();
	}

	@Override
	public void handleEvent(final NetworkViewDestroyedEvent e) {
		invokeOnEDT(() -> {
			networkMainPanel.getRootNetworkListPanel().update();
		});
	}

	@Override
	public void handleEvent(final NetworkViewAddedEvent nde) {
		if (loadingSession)
			return;

		final CyNetworkView netView = nde.getNetworkView();

		invokeOnEDT(() -> {
			final SubNetworkPanel subNetPanel = networkMainPanel.getSubNetworkPanel(netView.getModel());

			if (subNetPanel != null)
				subNetPanel.update();
		});
	}
	
	public void addTaskFactory(TaskFactory factory, Map<?, ?> props) {
		addFactory(factory, props);
	}

	public void removeTaskFactory(TaskFactory factory, Map<?, ?> props) {
		removeFactory(factory);
	}

	public void addNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map<?, ?> props) {
		final DynamicTaskFactoryProvisioner factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);
		TaskFactory provisioner = factoryProvisioner.createFor(factory);
		provisionerMap.put(factory, provisioner);
		addFactory(provisioner, props);
	}

	public void removeNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map<?, ?> props) {
		removeFactory(provisionerMap.remove(factory));
	}

	public void addNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory, Map<?, ?> props) {
		final DynamicTaskFactoryProvisioner factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);
		TaskFactory provisioner = factoryProvisioner.createFor(factory);
		provisionerMap.put(factory, provisioner);
		addFactory(provisioner, props);
	}

	public void removeNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory, Map<?, ?> props) {
		removeFactory(provisionerMap.remove(factory));
	}

	public void addNetworkTaskFactory(NetworkTaskFactory factory, Map<?, ?> props) {
		final DynamicTaskFactoryProvisioner factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);
		TaskFactory provisioner = factoryProvisioner.createFor(factory);
		provisionerMap.put(factory, provisioner);
		addFactory(provisioner, props);
	}

	public void removeNetworkTaskFactory(NetworkTaskFactory factory, Map<?, ?> props) {
		removeFactory(provisionerMap.remove(factory));
	}

	public void addNetworkViewTaskFactory(final NetworkViewTaskFactory factory, Map<?, ?> props) {
		final DynamicTaskFactoryProvisioner factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);
		TaskFactory provisioner = factoryProvisioner.createFor(factory);
		provisionerMap.put(factory, provisioner);
		addFactory(provisioner, props);
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory factory, Map<?, ?> props) {
		removeFactory(provisionerMap.remove(factory));
	}
	
	public void addCyAction(final CyAction action, Map<?, ?> props) {
		addAction(action);
	}
	
	public void removeCyAction(final CyAction action, Map<?, ?> props) {
		final JMenuItem item = popupMap.remove(action);
		
		if (item != null)
			popup.remove(item);
		
		popupActionMap.remove(action);
		popup.removePopupMenuListener(action);
	}
	
	// // Private Methods // //
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addFactory(final TaskFactory factory, final Map props) {
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
		
		final CyAction action;
		
		if (props.containsKey("enableFor"))
			action = new TaskFactoryTunableAction(taskMgr, factory, props, appMgr, netViewMgr);
		else
			action = new TaskFactoryTunableAction(taskMgr, factory, props);

		final JMenuItem item = new JMenuItem(action);

		Double gravity = 10.0;
		
		if (props.containsKey(ServiceProperties.MENU_GRAVITY))
			gravity = Double.valueOf(props.get(ServiceProperties.MENU_GRAVITY).toString());
		
		actionGravityMap.put(item, gravity);
		
		popupMap.put(factory, item);
		popupActionMap.put(factory, action);
		int menuIndex = getMenuIndexByGravity(item);
		popup.insert(item, menuIndex);
		popup.addPopupMenuListener(action);
	}
	
	private void removeFactory(TaskFactory factory) {
		final JMenuItem item = popupMap.remove(factory);
		
		if (item != null)
			popup.remove(item);
		
		final CyAction action = popupActionMap.remove(factory);
		
		if (action != null)
			popup.removePopupMenuListener(action);
	}
	
	private void addAction(final CyAction action) {
		final JMenuItem item = new JMenuItem(action);
		final double gravity = action.getMenuGravity();
		actionGravityMap.put(item, gravity);
		
		popupMap.put(action, item);
		popupActionMap.put(action, action);
		int menuIndex = getMenuIndexByGravity(item);
		popup.insert(item, menuIndex);
		popup.addPopupMenuListener(action);
	}
	
	private int getMenuIndexByGravity(JMenuItem item) {
		Double gravity = this.actionGravityMap.get(item);
		Double gravityX;

		for (int i = 0; i < popup.getComponentCount(); i++) {
			gravityX = this.actionGravityMap.get(popup.getComponent(i));

			if (gravity < gravityX)
				return i;
		}

		return popup.getComponentCount();
	}
	
	private void addMouseListenersForSelection(final AbstractNetworkPanel<?> item, final JComponent... components) {
		// This mouse listener listens for mouse pressed events to select the list items
		final MouseListener selectionListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				networkMainPanel.onMousePressedItem(e, item);
			};
		};
		
		// This mouse listener listens for the right-click events to show the pop-up window
		final PopupListener popupListener = new PopupListener(item);
		
		for (JComponent c : components) {
			c.addMouseListener(selectionListener);
			c.addMouseListener(popupListener);
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
		
		PopupListener(final AbstractNetworkPanel<?> item) {
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
		private final void maybeShowPopupMenu(final MouseEvent e) {
			// Ignore if not valid trigger.
			if (!e.isPopupTrigger())
				return;

			// If the item is not selected, select it first
			final List<AbstractNetworkPanel<?>> selectedItems = networkMainPanel.getSelectedItems();
			
			if (!selectedItems.contains(item))
				networkMainPanel.selectAndSetCurrent(item);
			
			final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
			final CyNetwork network = item.getModel().getNetwork();
			
			if (network instanceof CySubNetwork) {
				// Enable or disable the actions
				for (CyAction action : popupActionMap.values())
					action.updateEnableState();

				// Show popup menu
				popup.show(e.getComponent(), e.getX(), e.getY());
			} else {
				final JPopupMenu rootPopupMenu = new JPopupMenu();
				
				editRootNetworTitle = new JMenuItem("Rename Network Collection...");
				editRootNetworTitle.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final EditNetworkTitleTaskFactory taskFactory = serviceRegistrar.getService(EditNetworkTitleTaskFactory.class);
						taskMgr.execute(taskFactory.createTaskIterator(network));
					}
				});
				rootPopupMenu.add(editRootNetworTitle);
				
				editRootNetworTitle.setEnabled(selectedItems.size() == 1);
				rootPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
}
