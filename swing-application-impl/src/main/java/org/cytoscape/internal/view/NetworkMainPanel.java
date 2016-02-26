package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_DOWN;
import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_UP;
import static org.cytoscape.util.swing.IconManager.ICON_COG;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
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
import org.cytoscape.task.edit.EditNetworkTitleTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

@SuppressWarnings("serial")
public class NetworkMainPanel extends JPanel implements CytoPanelComponent2, NetworkAddedListener,
		NetworkViewAddedListener, NetworkAboutToBeDestroyedListener, NetworkDestroyedListener,
		NetworkViewDestroyedListener, RowsSetListener, AddedNodesListener, AddedEdgesListener, RemovedEdgesListener,
		SetCurrentNetworkListener, RemovedNodesListener, SessionAboutToBeLoadedListener, SessionLoadedListener {

	public static final float ICON_FONT_SIZE = 22.0f;
	
	private static final String TITLE = "Network";
	private static final String ID = "org.cytoscape.Network";
	
	private static final Dimension PANEL_SIZE = new Dimension(400, 700);

	private JScrollPane rootNetworkScroll;
	private RootNetworkListPanel rootNetworkListPanel;
	private JPanel networkHeader;
	private JButton expandAllButton;
	private JButton collapseAllButton;
	private JButton optionsBtn;
	private JLabel networkSelectionLabel;

	private final JPopupMenu popup;
	private JMenuItem editRootNetworTitle;
	
	private HashMap<JMenuItem, Double> actionGravityMap = new HashMap<>();
	private final Map<Object, TaskFactory> provisionerMap = new HashMap<>();
	private final Map<TaskFactory, JMenuItem> popupMap = new WeakHashMap<>();
	private final Map<TaskFactory, CyAction> popupActions = new WeakHashMap<>();
	private final Map<CyTable, CyNetwork> nameTables = new WeakHashMap<>();
	private final Map<CyTable, CyNetwork> nodeEdgeTables = new WeakHashMap<>();

	private AbstractNetworkPanel<?> selectionHead;
	private AbstractNetworkPanel<?> selectionTail;
	private AbstractNetworkPanel<?> lastSelected;
	
	private boolean showNodeEdgeCount = false;  // TODO Use CyProperty (user preference only)
	
	private boolean loadingSession;
	private boolean ignoreSelectionEvents;
	private boolean doNotUpdateCollapseExpandButtons;
	
	private NetworkViewPreviewDialog viewDialog;

	private CyServiceRegistrar serviceRegistrar;
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(NetworkMainPanel.class);

	public NetworkMainPanel(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		popup = new JPopupMenu();
		init();
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}
	
	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public Icon getIcon() {
		return null;
	}
	
	private void init() {
		setPreferredSize(PANEL_SIZE);
		setSize(PANEL_SIZE);
		setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua

		setLayout(new BorderLayout());
		add(getNetworkHeader(), BorderLayout.NORTH);
		add(getRootNetworkScroll(), BorderLayout.CENTER);
		
		updateNetworkHeader();
		updateNetworkToolBar();
	}
	
	private JScrollPane getRootNetworkScroll() {
		if (rootNetworkScroll == null) {
			rootNetworkScroll = new JScrollPane(getRootNetworkListPanel());
			rootNetworkScroll.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					getRootNetworkListPanel().updateScrollableTracksViewportHeight();
				}
			});
		}
		
		return rootNetworkScroll;
	}
	
	private RootNetworkListPanel getRootNetworkListPanel() {
		if (rootNetworkListPanel == null) {
			rootNetworkListPanel = new RootNetworkListPanel();
			setKeyBindings(rootNetworkListPanel);
		}
		
		return rootNetworkListPanel;
	}
	
	private JPanel getNetworkHeader() {
		if (networkHeader == null) {
			networkHeader = new JPanel();
			networkHeader.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			
			final GroupLayout layout = new GroupLayout(networkHeader);
			networkHeader.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getExpandAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getCollapseAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(getNetworkSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getExpandAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getCollapseAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNetworkSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return networkHeader;
	}
	
	private JButton getExpandAllButton() {
		if (expandAllButton == null) {
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			
			expandAllButton = new JButton(ICON_ANGLE_DOUBLE_DOWN);
			expandAllButton.setFont(iconManager.getIconFont(17.0f));
			expandAllButton.setToolTipText("Expand all network collections");
			expandAllButton.setBorderPainted(false);
			expandAllButton.setContentAreaFilled(false);
			expandAllButton.setFocusPainted(false);
			expandAllButton.setBorder(BorderFactory.createEmptyBorder());
			
			expandAllButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					expandAllRootNetworks();
				}
			});
		}
		
		return expandAllButton;
	}
	
	private JButton getCollapseAllButton() {
		if (collapseAllButton == null) {
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			
			collapseAllButton = new JButton(ICON_ANGLE_DOUBLE_UP);
			collapseAllButton.setFont(iconManager.getIconFont(17.0f));
			collapseAllButton.setToolTipText("Collapse all network collections");
			collapseAllButton.setBorderPainted(false);
			collapseAllButton.setContentAreaFilled(false);
			collapseAllButton.setFocusPainted(false);
			collapseAllButton.setBorder(BorderFactory.createEmptyBorder());
			
			collapseAllButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					collapseAllRootNetworks();
				}
			});
		}
		
		return collapseAllButton;
	}
	
	private JButton getOptionsButton() {
		if (optionsBtn == null) {
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			
			optionsBtn = new JButton(ICON_COG);
			optionsBtn.setFont(iconManager.getIconFont(ICON_FONT_SIZE * 4/5));
			optionsBtn.setToolTipText("Options...");
			optionsBtn.setBorderPainted(false);
			optionsBtn.setContentAreaFilled(false);
			optionsBtn.setFocusPainted(false);
			optionsBtn.setBorder(BorderFactory.createEmptyBorder());
			
			optionsBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					getNetworkOptionsMenu().show(optionsBtn, 0, optionsBtn.getHeight());
				}
			});
		}
		
		return optionsBtn;
	}
	
	private JLabel getNetworkSelectionLabel() {
		if (networkSelectionLabel == null) {
			networkSelectionLabel = new JLabel();
			networkSelectionLabel.setHorizontalAlignment(JLabel.CENTER);
			networkSelectionLabel.setFont(networkSelectionLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return networkSelectionLabel;
	}
	
	private JPopupMenu getNetworkOptionsMenu() {
		final JPopupMenu menu = new JPopupMenu();
		
		{
			final JMenuItem mi = new JCheckBoxMenuItem("Show Number of Nodes and edges");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showNodeEdgeCount = mi.isSelected();
					
					for (final RootNetworkPanel item : getRootNetworkListPanel().getAllItems())
						item.setShowNodeEdgeCount(mi.isSelected());
				}
			});
			mi.setSelected(showNodeEdgeCount);
			menu.add(mi);
		}
		
		return menu;
	}
	
	public Map<Long, Integer> getNetworkListOrder() {
		final Map<Long, Integer> order = new HashMap<>();
		
		final List<SubNetworkPanel> items = getAllSubNetworkItems();
		int count = 0;
		
		for (SubNetworkPanel snp : items) {
			final CySubNetwork net = snp.getModel().getNetwork();
			order.put(net.getSUID(), count++);
		}
				
		return order;
	}
	
	/**
	 * Replace the current network list with the passed ones.
	 */
	public void setNetworks(final Collection<CySubNetwork> networks) {
		clear();
				
		ignoreSelectionEvents = true;
		doNotUpdateCollapseExpandButtons = true;
		
		try {
			for (final CySubNetwork n : networks)
				addNetwork((CySubNetwork) n);
		} finally {
			doNotUpdateCollapseExpandButtons = false;
			ignoreSelectionEvents = false;
		}

		getRootNetworkListPanel().update();
		updateNetworkHeader();
		updateNetworkToolBar();
	}
	
	/**
	 * @param includeSelectedRootNetworks if true the CySubNetworks from selected CyRootNetworks are also included
	 * @return
	 */
	public Set<CyNetwork> getSelectedNetworks(final boolean includeSelectedRootNetworks) {
		final Set<CyNetwork> list = new LinkedHashSet<>();
		
		for (SubNetworkPanel p : getSelectedSubNetworkItems())
			list.add(p.getModel().getNetwork());
		
		if (includeSelectedRootNetworks) {
			for (RootNetworkPanel p : getSelectedRootNetworkItems())
				list.addAll(getNetworks(p.getAllItems()));
		}
		
		return list;
	}
	
	public void setSelectedNetworks(final List<CyNetwork> selectedNetworks) {
		ignoreSelectionEvents = true;
		
		try {
			for (SubNetworkPanel snp : getAllSubNetworkItems()) {
				if (!snp.isVisible()) {
					final RootNetworkPanel rnp = getRootNetworkPanel(snp.getModel().getNetwork().getRootNetwork());
					
					if (rnp != null)
						rnp.expand();
				}
				
				setSelected(snp, selectedNetworks.contains(snp.getModel().getNetwork()));
			}
		} finally {
			ignoreSelectionEvents = false;
		}
		
		final List<AbstractNetworkPanel<?>> selectedItems = getSelectedItems();
		selectionHead = selectedItems.isEmpty() ? null : selectedItems.get(0);
		selectionTail = selectedItems.size() > 1 ? selectedItems.get(selectedItems.size() - 1) : null;
		lastSelected = selectedItems.isEmpty() ? null : selectedItems.get(selectedItems.size() - 1);
		
		updateNetworkHeader();
		updateNetworkToolBar();
	}
	
	public void clear() {
		nameTables.clear();
		nodeEdgeTables.clear();
		
		ignoreSelectionEvents = true;
		doNotUpdateCollapseExpandButtons = true;
		
		try {
			getRootNetworkListPanel().removeAllItems();
		} finally {
			doNotUpdateCollapseExpandButtons = false;
			ignoreSelectionEvents = false;
		}
		
		lastSelected = selectionHead = selectionTail = null;
		
		updateNetworkHeader();
		updateNetworkToolBar();
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
		if (e.getNetwork() instanceof CySubNetwork)
			removeNetwork((CySubNetwork) e.getNetwork());
	}
	
	@Override
	public void handleEvent(final NetworkDestroyedEvent e) {
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				getRootNetworkListPanel().update();
				updateCollapseExpandButtons();
				updateNetworkToolBar();
			}
		});
	}

	@Override
	public void handleEvent(final NetworkAddedEvent e) {
		if (loadingSession)
			return;
		
		final CyNetwork net = e.getNetwork();
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				if (net instanceof CySubNetwork) {
					SubNetworkPanel snp = addNetwork((CySubNetwork) net);
					selectAndSetCurrent(snp);
				}
			}
		});
	}

	@Override
	public void handleEvent(final RowsSetEvent e) {
		if (loadingSession || getRootNetworkListPanel().isEmpty())
			return;
		
		// We only care about network name changes
		final Collection<RowSetRecord> nameRecords = e.getColumnRecords(CyNetwork.NAME);
		
		if (nameRecords == null || nameRecords.isEmpty())
			return;
		
		final CyTable tbl = e.getSource();
		final CyNetworkTableManager netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		final CyNetwork net = netTblMgr.getNetworkForTable(tbl);
		
		// And if there is no related view, nothing needs to be done
		if (net != null && tbl.equals(net.getDefaultNetworkTable())) {
			invokeOnEDT(new Runnable() {
				@Override
				public void run() {
					final AbstractNetworkPanel<?> item = getNetworkItem(net);
					
					if (item != null)
						item.update();
				}
			});
		}
	}

	@Override
	public void handleEvent(final AddedEdgesEvent e) {
		updateNodeEdgeCount(e.getSource());
	}

	@Override
	public void handleEvent(final AddedNodesEvent e) {
		updateNodeEdgeCount(e.getSource());
	}
	
	@Override
	public void handleEvent(final RemovedNodesEvent e) {
		updateNodeEdgeCount(e.getSource());
	}

	@Override
	public void handleEvent(final RemovedEdgesEvent e) {
		updateNodeEdgeCount(e.getSource());
	}

	@Override
	public void handleEvent(final NetworkViewDestroyedEvent e) {
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				getRootNetworkListPanel().update();
			}
		});
	}

	@Override
	public void handleEvent(final NetworkViewAddedEvent nde) {
		if (loadingSession)
			return;
		
		final CyNetworkView netView = nde.getNetworkView();
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				final SubNetworkPanel subNetPanel = getSubNetworkPanel(netView.getModel());
				
				if (subNetPanel != null)
					subNetPanel.update();
			}
		});
	}
	
	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		getRootNetworkListPanel().update();
	}
	
	// // Private Methods // //
	
	private SubNetworkPanel addNetwork(final CySubNetwork network) {
		final CyRootNetwork rootNetwork = network.getRootNetwork();
		RootNetworkPanel rootNetPanel = getRootNetworkPanel(rootNetwork);
		
		if (rootNetPanel == null) {
			rootNetPanel = getRootNetworkListPanel().addItem(rootNetwork);
			setKeyBindings(rootNetPanel);
			
			final RootNetworkPanel item = rootNetPanel;
			
			item.addPropertyChangeListener("expanded", new PropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent e) {
					// Deselect its selected subnetworks first
					if (e.getNewValue() == Boolean.FALSE) {
						final List<AbstractNetworkPanel<?>> selectedItems = getSelectedItems();
						
						if (!selectedItems.isEmpty()) {
							selectedItems.removeAll(item.getAllItems());
							setSelectedItems(selectedItems);
						}
					}
					
					updateCollapseExpandButtons();
				}
			});
			addMouseListenersForSelection(item, item.getHeaderPanel(), item.getNetworkCountLabel(), item.getNameLabel());
		}
		
		final SubNetworkPanel subNetPanel = rootNetPanel.addItem(network);
		setKeyBindings(subNetPanel);
		
		subNetPanel.addPropertyChangeListener("selected", new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				if (!ignoreSelectionEvents) {
					updateNetworkSelectionLabel();
					updateNetworkToolBar();
					
					final Set<CyNetwork> oldSelection = getSelectedNetworks(false);
					oldSelection.remove(subNetPanel.getModel().getNetwork());
					fireSelectedNetworksChange(oldSelection);
				}
			}
		});
		
		addMouseListenersForSelection(subNetPanel, subNetPanel, subNetPanel.getNameLabel(), subNetPanel.getViewIconLabel());
		
		subNetPanel.getViewIconLabel().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowViewPopup(subNetPanel);
			}
		});
		
		// Scroll to new item
		rootNetPanel.expand();
		getRootNetworkScroll().scrollRectToVisible(subNetPanel.getBounds());
		subNetPanel.requestFocus();
		
		nameTables.put(network.getDefaultNetworkTable(), network);
		nodeEdgeTables.put(network.getDefaultNodeTable(), network);
		nodeEdgeTables.put(network.getDefaultEdgeTable(), network);
		
		return subNetPanel;
	}
	
	private void addMouseListenersForSelection(final AbstractNetworkPanel<?> item, final JComponent... components) {
		// This mouse listener listens for mouse pressed events to select the list items
		final MouseListener selectionListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				onMousePressedItem(e, item);
			};
		};
		
		// This mouse listener listens for the right-click events to show the pop-up window
		final PopupListener popupListener = new PopupListener(item);
		
		for (JComponent c : components) {
			c.addMouseListener(selectionListener);
			c.addMouseListener(popupListener);
		}
	}

	/**
	 * Remove a network from the panel.
	 */
	private void removeNetwork(final CySubNetwork network) {
		nameTables.values().removeAll(Collections.singletonList(network));
		nodeEdgeTables.values().removeAll(Collections.singletonList(network));
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				final CyRootNetwork rootNet = network.getRootNetwork();
				final RootNetworkPanel item = getRootNetworkPanel(rootNet);
				
				if (item != null) {
					item.removeItem(network);
					
					if (item.isEmpty()) {
						getRootNetworkListPanel().removeItem(rootNet);
						nameTables.values().removeAll(Collections.singletonList(rootNet));
						nodeEdgeTables.values().removeAll(Collections.singletonList(rootNet));
					}
					
					updateNetworkHeader();
					updateNetworkToolBar();
				}
			}
		});
	}

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
		popupActions.put(factory, action);
		int menuIndex = getMenuIndexByGravity(item);
		popup.insert(item, menuIndex);
		popup.addPopupMenuListener(action);
	}
	
	private boolean setSelected(final AbstractNetworkPanel<?> item, final boolean selected) {
		if (item.isSelected() != selected) {
			item.setSelected(selected);
			
			if (!selected) {
				 if (item == selectionHead) selectionHead = null;
				 if (item == selectionTail) selectionTail = null;
			}
			
			return true;
		}
		
		return false;
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
	
	private void removeFactory(TaskFactory factory) {
		JMenuItem item = popupMap.remove(factory);
		
		if (item != null)
			popup.remove(item);
		
		CyAction action = popupActions.remove(factory);
		
		if (action != null)
			popup.removePopupMenuListener(action);
	}
	
	private AbstractNetworkPanel<?> getNetworkItem(final CyNetwork net) {
		if (net instanceof CySubNetwork)
			return getSubNetworkPanel(net);
		if (net instanceof CyRootNetwork)
			return getRootNetworkPanel(net);
		
		return null; // Should never happen!
	}
	
	private AbstractNetworkPanel<?> getPreviousItem(final AbstractNetworkPanel<?> item, final boolean includeInvisible) {
		final List<AbstractNetworkPanel<?>> allItems = getAllItems(includeInvisible);
		int index = allItems.indexOf(item);
		
		return index > 0 ? allItems.get(index - 1) : null;
	}
	
	private AbstractNetworkPanel<?> getNextItem(final AbstractNetworkPanel<?> item, final boolean includeInvisible) {
		final List<AbstractNetworkPanel<?>> allItems = getAllItems(includeInvisible);
		int index = allItems.indexOf(item);
		
		return index >= 0 && index < allItems.size() - 1 ? allItems.get(index + 1) : null;
	}
	
	private RootNetworkPanel getRootNetworkPanel(final CyNetwork net) {
		if (net instanceof CyRootNetwork)
			return getRootNetworkListPanel().getItem((CyRootNetwork) net);
		
		return null;
	}
	
	private SubNetworkPanel getSubNetworkPanel(final CyNetwork net) {
		if (net instanceof CySubNetwork) {
			final CySubNetwork subNet = (CySubNetwork) net;
			final CyRootNetwork rootNet = subNet.getRootNetwork();
			final RootNetworkPanel rootNetPanel = getRootNetworkPanel(rootNet);
			
			if (rootNetPanel != null)
				return rootNetPanel.getItem(subNet);
		}
		
		return null;
	}
	
	private void updateNetworkHeader() {
		updateCollapseExpandButtons();
		updateNetworkSelectionLabel();
	}
	
	private void updateNetworkToolBar() {
		// Nothing to do here for now...
	}
	
	private void updateNodeEdgeCount(final CyNetwork network) {
		if (network instanceof CySubNetwork == false)
			return;
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				final RootNetworkPanel rootItem = getRootNetworkPanel(((CySubNetwork)network).getRootNetwork());
				
				if (rootItem != null)
					rootItem.updateCountInfo();
			}
		});
	}
	
	private void updateCollapseExpandButtons() {
		if (doNotUpdateCollapseExpandButtons)
			return;
		
		boolean enableCollapse = false;
		boolean enableExpand = false;
		final Collection<RootNetworkPanel> allItems = getRootNetworkListPanel().getAllItems();
		
		for (final RootNetworkPanel item : allItems) {
			if (item.isExpanded())
				enableCollapse = true;
			else
				enableExpand = true;
			
			if (enableExpand && enableCollapse)
				break;
		}
		
		getCollapseAllButton().setEnabled(enableCollapse);
		getExpandAllButton().setEnabled(enableExpand);
	}
	
	private void updateNetworkSelectionLabel() {
		final int total = getSubNetworkCount();
		
		if (total == 0) {
			getNetworkSelectionLabel().setText(null);
		} else {
			final int selected = getSelectedSubNetworkCount();
			getNetworkSelectionLabel().setText(
					selected + " of " + total + " Network" + (total == 1 ? "" : "s") + " selected");
		}
		
		getNetworkHeader().updateUI();
	}
	
	private void collapseAllRootNetworks() {
		doNotUpdateCollapseExpandButtons = true;
		final Collection<RootNetworkPanel> allItems = getRootNetworkListPanel().getAllItems();
		
		for (final RootNetworkPanel item : allItems)
			item.collapse();
		
		doNotUpdateCollapseExpandButtons = false;
		updateCollapseExpandButtons();
	}

	private void expandAllRootNetworks() {
		doNotUpdateCollapseExpandButtons = true;
		final Collection<RootNetworkPanel> allItems = getRootNetworkListPanel().getAllItems();
		
		for (final RootNetworkPanel item : allItems)
			item.expand();
		
		doNotUpdateCollapseExpandButtons = false;
		updateCollapseExpandButtons();
	}
	
	private void selectAll() {
		final List<AbstractNetworkPanel<?>> allItems = getAllItems(false);
		
		if (!allItems.isEmpty()) {
			setSelectedItems(getAllItems(false));
			selectionHead = allItems.get(0);
			selectionTail = allItems.get(allItems.size() - 1);
			lastSelected = selectionTail;
		}
	}
	
	private void deselectAll() {
		setSelectedItems(Collections.emptyList());
		lastSelected = selectionHead = selectionTail = null;
	}
	
	private List<AbstractNetworkPanel<?>> getAllItems(final boolean includeInvisible) {
		final ArrayList<AbstractNetworkPanel<?>> list = new ArrayList<>();
		
		for (final RootNetworkPanel item : getRootNetworkListPanel().getAllItems()) {
			list.add(item);
			
			if (includeInvisible || item.isExpanded())
				list.addAll(item.getAllItems());
		}
		
		return list;
	}
	
	private List<AbstractNetworkPanel<?>> getSelectedItems() {
		final List<AbstractNetworkPanel<?>> items = getAllItems(true);
		final Iterator<AbstractNetworkPanel<?>> iterator = items.iterator();
		
		while (iterator.hasNext()) {
			if (!iterator.next().isSelected())
				iterator.remove();
		}
		
		return items;
	}

	private void setSelectedItems(final Collection<AbstractNetworkPanel<?>> items) {
		final Set<CyNetwork> oldSelection = getSelectedNetworks(false);
		boolean changed = false;
		ignoreSelectionEvents = true;
		
		try {
			for (final AbstractNetworkPanel<?> p : getAllItems(true)) {
				final boolean b = setSelected(p, items.contains(p));
				
				if (b && p.getModel().getNetwork() instanceof CySubNetwork)
					changed = true;
			}
		} finally {
			ignoreSelectionEvents = false;
			updateNetworkHeader();
			updateNetworkToolBar();
		}
		
		if (changed)
			fireSelectedNetworksChange(oldSelection);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void selectAndSetCurrent(final AbstractNetworkPanel<?> item) {
		if (item == null)
			return;
		
		// First select the clicked item
		setSelectedItems((Set) (Collections.singleton(item)));
		
		// Then change the current network
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		
		if (item.getModel().getNetwork() instanceof CySubNetwork)
			appMgr.setCurrentNetwork(item.getModel().getNetwork());
		else
			appMgr.setCurrentNetwork(null);
		
		lastSelected = selectionHead = item;
		selectionTail = null;
	}
	
	private List<SubNetworkPanel> getAllSubNetworkItems() {
		final ArrayList<SubNetworkPanel> list = new ArrayList<>();
		
		for (final RootNetworkPanel item : getRootNetworkListPanel().getAllItems())
			list.addAll(item.getAllItems());
		
		return list;
	}
	
	private Collection<RootNetworkPanel> getSelectedRootNetworkItems() {
		final ArrayList<RootNetworkPanel> list = new ArrayList<>();
		
		for (final RootNetworkPanel rnp : getRootNetworkListPanel().getAllItems()) {
			if (rnp.isSelected())
				list.add(rnp);
		}
		
		return list;
	}
	
	private Collection<SubNetworkPanel> getSelectedSubNetworkItems() {
		final ArrayList<SubNetworkPanel> list = new ArrayList<>();
		
		for (final SubNetworkPanel snp : getAllSubNetworkItems()) {
			if (snp.isSelected())
				list.add(snp);
		}
		
		return list;
	}
	
	private int getSubNetworkCount() {
		int count = 0;
		
		for (final RootNetworkPanel item : getRootNetworkListPanel().getAllItems())
			count += item.getAllItems().size();
		
		return count;
	}
	
	private int getSelectedSubNetworkCount() {
		return getSelectedSubNetworkItems().size();
	}
	
	private static Set<CyNetwork> getNetworks(final Collection<SubNetworkPanel> items) {
		final Set<CyNetwork> list = new LinkedHashSet<>();
		
		for (final SubNetworkPanel snp : items)
			list.add(snp.getModel().getNetwork());
		
		return list;
	}
	
	private void onMousePressedItem(final MouseEvent e, final AbstractNetworkPanel<?> item) {
		item.requestFocusInWindow();
		
		if (e.isPopupTrigger()) {
			if (!item.isSelected())
				selectAndSetCurrent(item);
		} else if (SwingUtilities.isLeftMouseButton(e)) {
			// LEFT-CLICK...
			final boolean isMac = LookAndFeelUtil.isMac();
			
			if ((isMac && e.isMetaDown()) || (!isMac && e.isControlDown())) {
				// COMMAND button down on MacOS or CONTROL button down on another OS.
				// Toggle this item's selection state
				item.setSelected(!item.isSelected());
				// Find new selection range head
				selectionHead = item.isSelected() ? item : findNextSelectionHead(selectionHead);
				lastSelected = selectionHead;
			} else {
				if (e.isShiftDown()) {
					selectRange(item);
				} else {
					// No SHIFT/CTRL pressed
					selectAndSetCurrent(item);
				}
				
				if (getSelectedItems().size() == 1) {
					lastSelected = selectionHead = item;
					selectionTail = null;
				}
			}
		}
	}

	private void selectRange(final AbstractNetworkPanel<?> target) {
		if (selectionHead != null && selectionHead.isVisible() && selectionHead.isSelected()
				&& selectionHead != target) {
			final Set<CyNetwork> oldSelection = getSelectedNetworks(false);
			boolean changed = false;
			ignoreSelectionEvents = true;
			
			try {
				// First deselect previous range, if there is a tail
				if (selectionTail != null)
					changed = changeRangeSelection(selectionHead, selectionTail, false);
				
				// Now select the new range
				changed = changed | changeRangeSelection(selectionHead, (selectionTail = target), true);
			} finally {
				ignoreSelectionEvents = false;

				updateNetworkHeader();
				updateNetworkToolBar();
			}
			
			if (changed)
				fireSelectedNetworksChange(oldSelection);
		} else if (!target.isSelected()) {
			target.setSelected(true);
			lastSelected = target;
		}
	}

	private boolean changeRangeSelection(final AbstractNetworkPanel<?> item1, final AbstractNetworkPanel<?> item2,
			final boolean selected) {
		boolean changed = false;
		
		final List<AbstractNetworkPanel<?>> items = getAllItems(false);
		final int idx1 = items.indexOf(item1);
		final int idx2 = items.indexOf(item2);
		
		final List<AbstractNetworkPanel<?>> subList;
		
		if (idx2 >= idx1) {
			subList = items.subList(idx1 + 1, idx2 + 1);
			lastSelected = selectionTail;
		} else {
			subList = items.subList(idx2, idx1);
			Collections.reverse(subList);
			lastSelected = selectionHead;
		}
		
		for (final AbstractNetworkPanel<?> nextItem : subList) {
			if (nextItem.isVisible() && nextItem.isSelected() != selected) {
				nextItem.setSelected(selected);
				changed = true;
			}
		}
		
		return changed;
	}
	
	private AbstractNetworkPanel<?> findNextSelectionHead(final AbstractNetworkPanel<?> fromItem) {
		AbstractNetworkPanel<?> head = null;
		final List<AbstractNetworkPanel<?>> items = getAllItems(false);
		
		if (fromItem != null) {
			List<AbstractNetworkPanel<?>> subList = items.subList(items.indexOf(fromItem), items.size());
			
			// Try with the tail subset first
			for (final AbstractNetworkPanel<?> nextItem : subList) {
				if (nextItem.isVisible() && nextItem.isSelected()) {
					head = nextItem;
					break;
				}
			}
			
			if (head == null) {
				// Try with the head subset
				subList = items.subList(0, items.indexOf(fromItem));
				final ListIterator<AbstractNetworkPanel<?>> li = subList.listIterator(subList.size());
				
				while (li.hasPrevious()) {
					final AbstractNetworkPanel<?> previousItem = li.previous();
					
					if (previousItem.isVisible() && previousItem.isSelected()) {
						head = previousItem;
						break;
					}
				}
			}
		}
		
		return head;
	}
	
	private void setKeyBindings(final JComponent comp) {
		final ActionMap actionMap = comp.getActionMap();
		final InputMap inputMap = comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		final int CTRL = LookAndFeelUtil.isMac() ? InputEvent.META_DOWN_MASK :  InputEvent.CTRL_DOWN_MASK;

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KeyAction.VK_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KeyAction.VK_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_SHIFT_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_SHIFT_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, CTRL), KeyAction.VK_CTRL_A);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, CTRL + InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_CTRL_SHIFT_A);
		
		actionMap.put(KeyAction.VK_UP, new KeyAction(KeyAction.VK_UP));
		actionMap.put(KeyAction.VK_DOWN, new KeyAction(KeyAction.VK_DOWN));
		actionMap.put(KeyAction.VK_SHIFT_UP, new KeyAction(KeyAction.VK_SHIFT_UP));
		actionMap.put(KeyAction.VK_SHIFT_DOWN, new KeyAction(KeyAction.VK_SHIFT_DOWN));
		actionMap.put(KeyAction.VK_CTRL_A, new KeyAction(KeyAction.VK_CTRL_A));
		actionMap.put(KeyAction.VK_CTRL_SHIFT_A, new KeyAction(KeyAction.VK_CTRL_SHIFT_A));
	}

	private void fireSelectedNetworksChange(final Collection<CyNetwork> oldValue) {
		firePropertyChange("selectedSubNetworks", oldValue, getSelectedNetworks(false));
	}
	
	static void styleButton(final AbstractButton btn, final Font font) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setPreferredSize(new Dimension(32, 32));
	}
	
	// // Private Classes // //
	
	private class RootNetworkListPanel extends JPanel implements Scrollable {
		
		private final JPanel filler = new JPanel();
		private boolean scrollableTracksViewportHeight;
		
		private final Map<CyRootNetwork, RootNetworkPanel> items = new LinkedHashMap<>();
		
		RootNetworkListPanel() {
			setBackground(UIManager.getColor("Table.background"));
			
			filler.setAlignmentX(LEFT_ALIGNMENT);
			filler.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
			filler.setBackground(getBackground());
			filler.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(final MouseEvent e) {
					if (!e.isPopupTrigger())
						deselectAll();
				}
			});
			
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(filler);
			
			updateScrollableTracksViewportHeight();
		}
		
		void update() {
			for (final RootNetworkPanel item : getAllItems())
				item.update();
			
			updateScrollableTracksViewportHeight();
		}
		
		void updateScrollableTracksViewportHeight() {
			final boolean oldValue = scrollableTracksViewportHeight;
			
			if (items == null || items.isEmpty()) {
				scrollableTracksViewportHeight = true;
			} else {
				int ih = 0; // Total items height
				
				for (RootNetworkPanel rnp : items.values())
					ih += rnp.getHeight();
				
				scrollableTracksViewportHeight = ih <= getRootNetworkScroll().getViewport().getHeight();
			}
			
			if (oldValue != scrollableTracksViewportHeight)
				updateUI();
		}

		Collection<RootNetworkPanel> getAllItems() {
			return new ArrayList<>(items.values());
		}
		
		RootNetworkPanel addItem(final CyRootNetwork rootNetwork) {
			if (!items.containsKey(rootNetwork)) {
				final RootNetworkPanelModel model = new RootNetworkPanelModel(rootNetwork, serviceRegistrar);
				final RootNetworkPanel rootNetworkPanel = new RootNetworkPanel(model, showNodeEdgeCount, serviceRegistrar);
				rootNetworkPanel.setAlignmentX(LEFT_ALIGNMENT);
				
				rootNetworkPanel.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent e) {
						updateScrollableTracksViewportHeight();
					}
				});
				
				items.put(rootNetwork, rootNetworkPanel);
				add(rootNetworkPanel, getComponentCount() - 1);
			}
			
			return items.get(rootNetwork);
		}
		
		RootNetworkPanel removeItem(final CyRootNetwork rootNetwork) {
			final RootNetworkPanel rootNetworkPanel = items.remove(rootNetwork);
			
			if (rootNetworkPanel != null)
				remove(rootNetworkPanel);
			
			return rootNetworkPanel;
		}
		
		void removeAllItems() {
			items.clear();
			removeAll();
			add(filler);
		}
		
		RootNetworkPanel getItem(final CyRootNetwork rootNetwork) {
			return items.get(rootNetwork);
		}
		
		boolean isEmpty() {
			return items.isEmpty();
		}
		
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return scrollableTracksViewportHeight;
		}
	}
	
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
			final List<AbstractNetworkPanel<?>> selectedItems = getSelectedItems();
			
			if (!selectedItems.contains(item))
				selectAndSetCurrent(item);
			
			final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
			final CyNetwork network = item.getModel().getNetwork();
			
			if (network instanceof CySubNetwork) {
				// Enable or disable the actions
				for (CyAction action : popupActions.values())
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
	
	private void maybeShowViewPopup(final SubNetworkPanel item) {
		final CySubNetwork network = item.getModel().getNetwork();
		
		if (viewDialog != null) {
			if (viewDialog.getNetwork().equals(network)) // Clicking the same item--will probably never happen
				return;
		
			disposeViewPopup();
		}
		
		if (item.getModel().getViewCount() > 0) {
			final Window windowAncestor = SwingUtilities.getWindowAncestor(item);
			final CyNetworkView currentView = serviceRegistrar.getService(CyApplicationManager.class)
					.getCurrentNetworkView();
			
			viewDialog = new NetworkViewPreviewDialog(network, currentView, windowAncestor, serviceRegistrar);
			
			viewDialog.addWindowFocusListener(new WindowFocusListener() {
				@Override
				public void windowLostFocus(WindowEvent e) {
					disposeViewPopup();
				}
				@Override
				public void windowGainedFocus(WindowEvent e) {
				}
			});
			viewDialog.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
						disposeViewPopup();
				}
			});
			viewDialog.addPropertyChangeListener("currentNetworkView", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					disposeViewPopup();
					serviceRegistrar.getService(CyApplicationManager.class)
							.setCurrentNetworkView((CyNetworkView) evt.getNewValue());
				}
			});
			
			final Point screenPt = item.getViewIconLabel().getLocationOnScreen();
			final Point compPt = item.getViewIconLabel().getLocation();
			int xOffset = screenPt.x - compPt.x - item.getViewIconLabel().getWidth() / 2;
			int yOffset = screenPt.y - compPt.y + item.getViewIconLabel().getBounds().height - 2;
		    final Point pt = item.getViewIconLabel().getBounds().getLocation();
		    pt.translate(xOffset, yOffset);
		    
			viewDialog.setLocation(pt);
			viewDialog.setVisible(true);
			viewDialog.requestFocusInWindow();
		}
	}
	
	private void disposeViewPopup() {
		if (viewDialog != null) {
			viewDialog.dispose();
			viewDialog = null;
		}
	}
	
	private class KeyAction extends AbstractAction {

		final static String VK_UP = "VK_UP";
		final static String VK_DOWN = "VK_DOWN";
		final static String VK_SHIFT_UP = "VK_SHIFT_UP";
		final static String VK_SHIFT_DOWN = "VK_SHIFT_DOWN";
		final static String VK_CTRL_A = "VK_CTRL_A";
		final static String VK_CTRL_SHIFT_A = "VK_CTRL_SHIFT_A";
		
		KeyAction(final String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String cmd = e.getActionCommand();
			final List<AbstractNetworkPanel<?>> allItems = getAllItems(false);
			
			if (allItems.isEmpty())
				return;
			
			if (cmd.equals(VK_UP)) {
				if (lastSelected != null) {
					final AbstractNetworkPanel<?> previous = getPreviousItem(lastSelected, false);
					selectAndSetCurrent(previous != null ? previous : allItems.get(0));
				}
			} else if (cmd.equals(VK_DOWN)) {
				if (lastSelected != null) {
					final AbstractNetworkPanel<?> next = getNextItem(lastSelected, false);
					selectAndSetCurrent(next != null ? next : allItems.get(allItems.size() - 1));
				}
			} else if (cmd.equals(VK_SHIFT_UP)) {
				final AbstractNetworkPanel<?> previous = getPreviousItem(lastSelected, false);
				
				if (previous != null)
					selectRange(previous);
			} else if (cmd.equals(VK_SHIFT_DOWN)) {
				final AbstractNetworkPanel<?> next = getNextItem(lastSelected, false);
				
				if (next != null)
					selectRange(next);
			} else if (cmd.equals(VK_CTRL_A)) {
				selectAll();
			} else if (cmd.equals(VK_CTRL_SHIFT_A)) {
				deselectAll();
			}
		}
	}
}
