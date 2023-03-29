package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.view.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.internal.view.util.ViewUtil.NetworksSortMode.CREATION;
import static org.cytoscape.internal.view.util.ViewUtil.NetworksSortMode.NAME;
import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_DOWN;
import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_UP;
import static org.cytoscape.util.swing.IconManager.ICON_COG;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT;
import static org.cytoscape.util.swing.IconManager.ICON_SORT_ALPHA_ASC;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.internal.task.LoadFileListTask;
import org.cytoscape.internal.util.Util;
import org.cytoscape.internal.view.util.ViewUtil;
import org.cytoscape.internal.view.util.ViewUtil.NetworksSortMode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.edit.EditNetworkTitleTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@SuppressWarnings("serial")
public class NetworkMainPanel extends JPanel implements CytoPanelComponent2 {

	public static final float ICON_FONT_SIZE = 22.0f;
	
	private static final String TITLE = "Network";
	private static final String ID = "org.cytoscape.Network";
	
	private JPanel networksPanel;
	private JScrollPane rootNetworkScroll;
	private RootNetworkListPanel rootNetworkListPanel;
	private JPanel networkHeader;
	private JButton expandAllButton;
	private JButton collapseAllButton;
	private JButton optionsBtn;
	private JLabel networkSelectionLabel;

	private CyNetwork currentNetwork;
	
	private boolean ignoreSelectionEvents;
	private boolean ignoreExpandedEvents;
	private boolean doNotUpdateCollapseExpandButtons;
	private boolean fireSelectedNetworksEvent = true;
	
	private NetworkViewPreviewDialog viewDialog;
	private TextIcon icon;
	
	private NetworksSortMode sortMode = CREATION;
	private Map<Long, Integer> networkListOrder;

	private NetworkListSelectionModel selectionModel;
	
	private final NetworkSearchBar networkSearchBar;
	private final CyServiceRegistrar serviceRegistrar;

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public NetworkMainPanel(NetworkSearchBar networkSearchBar, CyServiceRegistrar serviceRegistrar) {
		this.networkSearchBar = networkSearchBar;
		this.serviceRegistrar = serviceRegistrar;
		
		selectionModel = new NetworkListSelectionModel();
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
		if (icon == null)
			icon = new TextIcon(ICON_SHARE_ALT,
					serviceRegistrar.getService(IconManager.class).getIconFont(14.0f), 16, 16);
		
		return icon;
	}
	
	private void init() {
		setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua

		setLayout(new BorderLayout());
		add(networkSearchBar, BorderLayout.NORTH);
		add(getNetworksPanel(), BorderLayout.CENTER);
		
		new NetworkDropListener(null);
		
		updateNetworkHeader();
	}
	
	private JPanel getNetworksPanel() {
		if (networksPanel == null) {
			networksPanel = new JPanel();
			networksPanel.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			
			networksPanel.setLayout(new BorderLayout());
			networksPanel.add(getNetworkHeader(), BorderLayout.NORTH);
			networksPanel.add(getRootNetworkScroll(), BorderLayout.CENTER);
		}
		
		return networksPanel;
	}
	
	JScrollPane getRootNetworkScroll() {
		if (rootNetworkScroll == null) {
			rootNetworkScroll = new JScrollPane(getRootNetworkListPanel());
			rootNetworkScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			rootNetworkScroll.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					getRootNetworkListPanel().updateScrollableTracksViewportHeight();
				}
			});
			rootNetworkScroll.setMinimumSize(new Dimension(280, 160));
			rootNetworkScroll.setPreferredSize(new Dimension(380, 420));
		}
		
		return rootNetworkScroll;
	}
	
	RootNetworkListPanel getRootNetworkListPanel() {
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
			
			var layout = new GroupLayout(networkHeader);
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
			var iconManager = serviceRegistrar.getService(IconManager.class);
			
			expandAllButton = new JButton(ICON_ANGLE_DOUBLE_DOWN);
			expandAllButton.setFont(iconManager.getIconFont(17.0f));
			expandAllButton.setToolTipText("Expand all network collections");
			expandAllButton.setBorderPainted(false);
			expandAllButton.setContentAreaFilled(false);
			expandAllButton.setFocusPainted(false);
			expandAllButton.setBorder(BorderFactory.createEmptyBorder());
			
			expandAllButton.addActionListener(e -> expandAll());
		}
		
		return expandAllButton;
	}
	
	private JButton getCollapseAllButton() {
		if (collapseAllButton == null) {
			var iconManager = serviceRegistrar.getService(IconManager.class);
			
			collapseAllButton = new JButton(ICON_ANGLE_DOUBLE_UP);
			collapseAllButton.setFont(iconManager.getIconFont(17.0f));
			collapseAllButton.setToolTipText("Collapse all network collections");
			collapseAllButton.setBorderPainted(false);
			collapseAllButton.setContentAreaFilled(false);
			collapseAllButton.setFocusPainted(false);
			collapseAllButton.setBorder(BorderFactory.createEmptyBorder());
			
			collapseAllButton.addActionListener(e -> collapseAll());
		}
		
		return collapseAllButton;
	}
	
	private JButton getOptionsButton() {
		if (optionsBtn == null) {
			var iconManager = serviceRegistrar.getService(IconManager.class);
			
			optionsBtn = new JButton(ICON_COG);
			optionsBtn.setFont(iconManager.getIconFont(ICON_FONT_SIZE * 4/5));
			optionsBtn.setToolTipText("Options...");
			optionsBtn.setBorderPainted(false);
			optionsBtn.setContentAreaFilled(false);
			optionsBtn.setFocusPainted(false);
			optionsBtn.setBorder(BorderFactory.createEmptyBorder());
			
			optionsBtn.addActionListener(e -> getNetworkOptionsMenu().show(optionsBtn, 0, optionsBtn.getHeight()));
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
		var menu = new JPopupMenu();
		var iconManager = serviceRegistrar.getService(IconManager.class);
		
		{
			var icon = new TextIcon(ICON_SORT_ALPHA_ASC, iconManager.getIconFont(16.0f), 16, 16);
			var mi = new JCheckBoxMenuItem("Sort Networks by Name", icon);
			mi.addActionListener(e -> sortNetworks(mi.isSelected() ? NAME : CREATION));
			mi.setSelected(sortMode == NAME);
			menu.add(mi);
		}
		menu.addSeparator();
		{
			var mi = new JCheckBoxMenuItem("Show Network Provenance Hierarchy");
			mi.addActionListener(e -> setShowNetworkProvenanceHierarchy(mi.isSelected()));
			mi.setSelected(isShowNetworkProvenanceHierarchy());
			mi.setEnabled(sortMode == CREATION);
			menu.add(mi);
		}
		{
			var mi = new JCheckBoxMenuItem("Show Number of Nodes and Edges");
			mi.addActionListener(e -> setShowNodeEdgeCount(mi.isSelected()));
			mi.setSelected(isShowNodeEdgeCount());
			menu.add(mi);
		}
		
		return menu;
	}
	
	/**
	 * Return the network creation positions.
	 */
	public Map<Long, Integer> getNetworkListOrder() {
		if (networkListOrder == null) {
			networkListOrder = new LinkedHashMap<Long, Integer>();
			int count = 0;
			
			for (var rootNet : getRootNetworkListPanel().getRootNetworks()) {
				var rnp = getRootNetworkListPanel().getItem(rootNet);
				
				for (var subNet : rnp.getSubNetworks())
					networkListOrder.put(subNet.getSUID(), count++);
			}
		}
		
		return networkListOrder;
	}
	
	AbstractNetworkPanel<?> getItem(int index, boolean includeVisible) {
		var allItems = getAllItems(includeVisible);
		
		return allItems.size() > index ? allItems.get(index) : null;
	}
	
	/**
	 * @return The model index of the network, which means the current sort mode is ignored.
	 */
	public int indexOf(CyNetwork network) {
		var netPos = getNetworkListOrder();
		var idx = netPos.get(network.getSUID());
		
		return idx != null ? idx.intValue() : -1;
	}
	
	/**
	 * Replace the current network list with the passed ones.
	 */
	public void setNetworks(Collection<CySubNetwork> networks) {
		clear();
				
		ignoreSelectionEvents = true;
		doNotUpdateCollapseExpandButtons = true;
		
		try {
			var netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			
			for (var n : networks) {
				var snp = addNetwork(n);
				int count = netViewMgr.getNetworkViews(snp.getModel().getNetwork()).size();
				snp.getModel().setViewCount(count);
			}
		} finally {
			doNotUpdateCollapseExpandButtons = false;
			ignoreSelectionEvents = false;
		}

		getRootNetworkListPanel().update();
		updateNetworkHeader();
		updateNodeEdgeCount();
	}
	
	/**
	 * @param includeSelectedRootNetworks if true the CySubNetworks from selected CyRootNetworks are also included
	 */
	public Set<CyNetwork> getSelectedNetworks(boolean includeSelectedRootNetworks) {
		var list = new LinkedHashSet<CyNetwork>();
		
		for (var p : getSelectedSubNetworkItems())
			list.add(p.getModel().getNetwork());
		
		if (includeSelectedRootNetworks) {
			for (var p : getSelectedRootNetworkItems())
				list.addAll(getNetworks(p.getAllItems()));
		}
		
		return list;
	}
	
	public void setSelectedNetworks(Collection<CyNetwork> selectedNetworks) {
		if (Util.equalSets(selectedNetworks, getSelectedNetworks(false)))
			return;

		selectionModel.setValueIsAdjusting(true);
		ignoreSelectionEvents = true;
		fireSelectedNetworksEvent = false;
		
		try {
			selectionModel.clearSelection();
			var allNetworks = getAllNetworks(false);
			int maxIdx = -1;
			
			for (var n : selectedNetworks) {
				int idx = allNetworks.indexOf(n);
				
				if (idx >= 0) {
					selectionModel.addSelectionInterval(idx, idx);
					maxIdx = Math.max(idx, maxIdx);
				}
			}
			
			selectionModel.setAnchorSelectionIndex(maxIdx);
			selectionModel.moveLeadSelectionIndex(maxIdx);
		} finally {
			fireSelectedNetworksEvent = true;
			ignoreSelectionEvents = false;
			selectionModel.setValueIsAdjusting(false);
		}
		
		updateNetworkHeader();
	}
	
	public Set<CyRootNetwork> getSelectedRootNetworks() {
		var list = new LinkedHashSet<CyRootNetwork>();
		
		for (var p : getSelectedRootNetworkItems())
			list.add(p.getModel().getNetwork());
		
		return list;
	}
	
	public int countSelectedRootNetworks() {
		return getSelectedRootNetworkItems().size();
	}
	
	public int countSelectedSubNetworks(boolean includeSelectedRootNetworks) {
		return getSelectedNetworks(includeSelectedRootNetworks).size();
	}
	
	public void clear() {
		ignoreSelectionEvents = true;
		doNotUpdateCollapseExpandButtons = true;
		
		try {
			deselectAll();
			getRootNetworkListPanel().removeAllItems();
		} finally {
			doNotUpdateCollapseExpandButtons = false;
			ignoreSelectionEvents = false;
		}
		
		networkListOrder = null;
		updateNetworkHeader();
	}
	
	public boolean isShowNodeEdgeCount() {
		return "true".equalsIgnoreCase(ViewUtil.getViewProperty(ViewUtil.SHOW_NODE_EDGE_COUNT_KEY, serviceRegistrar));
	}
	
	public void setShowNodeEdgeCount(boolean b) {
		ViewUtil.setViewProperty(ViewUtil.SHOW_NODE_EDGE_COUNT_KEY, "" + b, serviceRegistrar);
		updateNodeEdgeCount();
	}
	
	public boolean isShowNetworkProvenanceHierarchy() {
		return "true".equalsIgnoreCase(
				ViewUtil.getViewProperty(ViewUtil.SHOW_NETWORK_PROVENANCE_HIERARCHY_KEY, serviceRegistrar));
	}
	
	public void setShowNetworkProvenanceHierarchy(boolean b) {
		if (b) // If showing the indentation, Reorder by the original CREATION positions
			getRootNetworkListPanel().sortNetworks(CREATION);
		
		for (var item : getRootNetworkListPanel().getAllItems())
			item.setShowIndentation(b);
		
		// Save the user preference 
		ViewUtil.setViewProperty(ViewUtil.SHOW_NETWORK_PROVENANCE_HIERARCHY_KEY, "" + b, serviceRegistrar);
	}
	
	public NetworksSortMode getSortMode() {
		return sortMode;
	}
	
	public void sortNetworks(NetworksSortMode mode) {
		sortMode = mode;
		getRootNetworkListPanel().sortNetworks(mode);
	}
	
	public void scrollTo(CyNetwork network) {
		final AbstractNetworkPanel<?> target;
		
		if (network instanceof CySubNetwork)
			target = getSubNetworkPanel(network);
		else
			target = getRootNetworkPanel(network);
		
		if (target != null) {
			if (target instanceof SubNetworkPanel) {
				var rnp = getRootNetworkPanel(((SubNetworkPanel) target).getModel().getNetwork().getRootNetwork());
				rnp.expand();
			}
			
			((JComponent) target.getParent()).scrollRectToVisible(target.getBounds());
		}
	}

	// // Private Methods // //
	
	protected SubNetworkPanel addNetwork(CySubNetwork network) {
		var rootNetwork = network.getRootNetwork();
		var rootNetPanel = getRootNetworkPanel(rootNetwork);
		var sortMode = this.sortMode;
		
		if (sortMode != CREATION) // Go back to the original order first, so we get the correct creation position
			sortNetworks(CREATION);
		
		if (rootNetPanel == null) {
			rootNetPanel = getRootNetworkListPanel().addItem(rootNetwork);
			setKeyBindings(rootNetPanel);
			
			new NetworkDropListener(rootNetwork);
			
			var item = rootNetPanel;
			
			item.addPropertyChangeListener("expanded", e -> {
				if (ignoreExpandedEvents)
					return;
				
				var selectedItems = getSelectedItems();
				
				if (!selectedItems.isEmpty()) {
					boolean expanded = Boolean.TRUE.equals(e.getNewValue());
					
					// Adjust Selection Model's anchor/lead, because the visible indexes have changed
					int anchor = -1;
					int lead = -1;
					
					if (expanded) {
						// When expanded, just use the first/last selection indexes as the new anchor/lead
						var allItems = getAllItems(false);
						anchor = allItems.indexOf(selectedItems.get(0));
						lead = allItems.indexOf(selectedItems.get(selectedItems.size() - 1));
					}
						
					selectionModel.setValueIsAdjusting(true);
					selectionModel.setAnchorSelectionIndex(anchor);
					selectionModel.moveLeadSelectionIndex(lead);
					selectionModel.fireValueChanged(true);
					selectionModel.setValueIsAdjusting(false);
					
					// If collapsed, make sure all of its subnetwork items are unselected
					if (!expanded)
						selectedItems.removeAll(item.getAllItems());
						
					// Always select the items again to make sure the model's min/max selection index are updated
					setSelectedItems(selectedItems);
				}
				
				updateCollapseExpandButtons();
			});
			item.addPropertyChangeListener("selected", e -> {
				if (!ignoreSelectionEvents) {
					boolean selected = (boolean) e.getNewValue();
					var oldSelection = getSelectedRootNetworks();
					
					if (selected) // Then it did not belong to the old selection value
						oldSelection.remove(item.getModel().getNetwork());
					else // It means it was selected before
						oldSelection.add(item.getModel().getNetwork());
					
					if (fireSelectedNetworksEvent)
						fireSelectedRootNetworksChange(oldSelection);
				}
			});
			
			firePropertyChange("rootNetworkPanelCreated", null, item);
		}
		
		var subNetPanel = rootNetPanel.addItem(network);
		setKeyBindings(subNetPanel);
		
		subNetPanel.addPropertyChangeListener("selected", (PropertyChangeEvent e) -> {
			if (!ignoreSelectionEvents) {
				updateNetworkSelectionLabel();
				
				boolean selected = (boolean) e.getNewValue();
				var oldSelection = getSelectedNetworks(false);
				
				if (selected) // Then it did not belong to the old selection value
					oldSelection.remove(subNetPanel.getModel().getNetwork());
				else // It means it was selected before
					oldSelection.add(subNetPanel.getModel().getNetwork());
				
				if (fireSelectedNetworksEvent)
					fireSelectedSubNetworksChange(oldSelection);
			}
		});
		subNetPanel.getViewIconLabel().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowViewPopup(subNetPanel);
			}
		});
		
		networkListOrder = null;
		
		firePropertyChange("subNetworkPanelCreated", null, subNetPanel);
		
		if (sortMode != this.sortMode) // Apply the current sort mode again, if necessary
			sortNetworks(sortMode);
		
		// Expand the root panel
		ignoreExpandedEvents = true;
		rootNetPanel.expand();
		ignoreExpandedEvents = false;
		
		// Scroll to new item
		scrollTo(network);
		
		// Update other parts of the UI
		updateCollapseExpandButtons();
		
		return subNetPanel;
	}
	
	/**
	 * Remove a network from the panel.
	 */
	protected void removeNetwork(CySubNetwork network) {
		invokeOnEDT(() -> {
			var rootNet = network.getRootNetwork();
			var item = getRootNetworkPanel(rootNet);
			
			if (item != null) {
				item.removeItem(network);
				
				if (item.isEmpty())
					getRootNetworkListPanel().removeItem(rootNet);
				
				networkListOrder = null;
				updateNetworkHeader();
			}
		});
	}

	private boolean setSelected(AbstractNetworkPanel<?> item, boolean selected) {
		if (item.isSelected() != selected) {
			item.setSelected(selected);
			
			return true;
		}
		
		return false;
	}

	protected AbstractNetworkPanel<?> getNetworkItem(CyNetwork net) {
		if (net instanceof CySubNetwork)
			return getSubNetworkPanel(net);
		if (net instanceof CyRootNetwork)
			return getRootNetworkPanel(net);
		
		return null; // Should never happen!
	}
	
	RootNetworkPanel getRootNetworkPanel(CyNetwork net) {
		if (net instanceof CyRootNetwork)
			return getRootNetworkListPanel().getItem((CyRootNetwork) net);
		
		return null;
	}
	
	SubNetworkPanel getSubNetworkPanel(CyNetwork net) {
		if (net instanceof CySubNetwork) {
			var subNet = (CySubNetwork) net;
			var rootNet = subNet.getRootNetwork();
			var rootNetPanel = getRootNetworkPanel(rootNet);
			
			if (rootNetPanel != null)
				return rootNetPanel.getItem(subNet);
		}
		
		return null;
	}
	
	private void updateNetworkHeader() {
		updateCollapseExpandButtons();
		updateNetworkSelectionLabel();
	}
	
	protected void updateNodeEdgeCount() {
		invokeOnEDT(() -> {
			int nodeLabelWidth = 0;
			int edgeLabelWidth = 0;
			
			for (var snp : getAllSubNetworkItems()) {
				snp.getNodeCountLabel().setVisible(isShowNodeEdgeCount());
				snp.getEdgeCountLabel().setVisible(isShowNodeEdgeCount());
				
				if (isShowNodeEdgeCount()) {
					// Update node/edge count label text
					snp.updateCountLabels();
					// Get max label width
					var nfm = snp.getNodeCountLabel().getFontMetrics(snp.getNodeCountLabel().getFont());
					var efm = snp.getEdgeCountLabel().getFontMetrics(snp.getEdgeCountLabel().getFont());
					
					nodeLabelWidth = Math.max(nodeLabelWidth, nfm.stringWidth(snp.getNodeCountLabel().getText()));
					edgeLabelWidth = Math.max(edgeLabelWidth, efm.stringWidth(snp.getEdgeCountLabel().getText()));
				}
			}
			
			if (!isShowNodeEdgeCount())
				return;
			
			// Apply max width values to all labels so they align properly
			for (var snp : getAllSubNetworkItems()) {
				var nd = new Dimension(nodeLabelWidth, snp.getNodeCountLabel().getPreferredSize().height);
				snp.getNodeCountLabel().setPreferredSize(nd);
				snp.getNodeCountLabel().setSize(nd);
				
				var ed = new Dimension(edgeLabelWidth, snp.getEdgeCountLabel().getPreferredSize().height);
				snp.getEdgeCountLabel().setPreferredSize(ed);
				snp.getEdgeCountLabel().setSize(ed);
			}
		});
	}
	
	protected void updateCollapseExpandButtons() {
		if (doNotUpdateCollapseExpandButtons)
			return;
		
		boolean enableCollapse = false;
		boolean enableExpand = false;
		var allItems = getRootNetworkListPanel().getAllItems();
		
		for (var item : allItems) {
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
		int total = getSubNetworkCount(true);
		
		if (total == 0) {
			getNetworkSelectionLabel().setText(null);
		} else {
			int selected = countSelectedSubNetworks(false);
			getNetworkSelectionLabel().setText(
					selected + " of " + total + " Network" + (total == 1 ? "" : "s") + " selected");
		}
		
		getNetworkHeader().updateUI();
	}
	
	private void collapseAll() {
		expandOrCollapseAll(false);
	}

	private void expandAll() {
		expandOrCollapseAll(true);
	}
	
	private void expandOrCollapseAll(boolean expand) {
		// First deselect all items, to prevent selection issues
		// caused by selection and current net/view events being fired
		// every time one of the root panels is collapsed
		var selectedItems = expand ? getSelectedItems() : getSelectedRootNetworkItems(); // but save the selected roots ("Collection" items)
		deselectAll();
		
		ignoreExpandedEvents = true;
		doNotUpdateCollapseExpandButtons = true;
		
		var allItems = getRootNetworkListPanel().getAllItems();
		
		for (var item : allItems) {
			if (expand)
				item.expand();
			else
				item.collapse();
		}
		
		ignoreExpandedEvents = false;
		doNotUpdateCollapseExpandButtons = false;
		updateCollapseExpandButtons();
		
		// Restore and adjust the selection
		if (!selectedItems.isEmpty()) {
			// Adjust Selection Model's anchor/lead, because the visible indexes have changed
			int anchor = -1;
			int lead = -1;
			
			if (expand) {
				// When expanded, just use the first/last selection indexes as the new anchor/lead
				var visibleItems = getAllItems(false);
				anchor = visibleItems.indexOf(selectedItems.get(0));
				lead = visibleItems.indexOf(selectedItems.get(selectedItems.size() - 1));
			}
				
			selectionModel.setValueIsAdjusting(true);
			selectionModel.setAnchorSelectionIndex(anchor);
			selectionModel.moveLeadSelectionIndex(lead);
			selectionModel.fireValueChanged(true);
			selectionModel.setValueIsAdjusting(false);
		
			// Apply the selection
			setSelectedItems(selectedItems);
		}
	}
	
	private void selectAll() {
		var allItems = getAllItems(false);
		
		if (!allItems.isEmpty())
			selectionModel.setSelectionInterval(0, allItems.size() - 1);
	}
	
	private void deselectAll() {
		selectionModel.clearSelection();
		selectionModel.setAnchorSelectionIndex(-1);
		selectionModel.setLeadSelectionIndex(-1);
		setCurrentNetwork(null);
	}
	
	/**
     * Selects a single cell. Does nothing if the given index is greater
     * than or equal to the model size. This is a convenience method that uses
     * {@code setSelectionInterval} on the selection model. Refer to the
     * documentation for the selection model class being used for details on
     * how values less than {@code 0} are handled.
     *
     * @param index the index of the cell to select
     */
	void setSelectedIndex(int index) {
		if (index >= getTotalNetworkCount(false))
			return;
		
		selectionModel.setSelectionInterval(index, index);
	}
	
	public List<AbstractNetworkPanel<?>> getAllItems(boolean includeInvisible) {
		var list = new ArrayList<AbstractNetworkPanel<?>>();
		
		for (var item : getRootNetworkListPanel().getAllItems()) {
			list.add(item);
			
			if (includeInvisible || item.isExpanded())
				list.addAll(item.getAllItems());
		}
		
		return list;
	}
	
	List<AbstractNetworkPanel<?>> getSelectedItems() {
		var items = getAllItems(true);
		var iterator = items.iterator();
		
		while (iterator.hasNext()) {
			if (!iterator.next().isSelected())
				iterator.remove();
		}
		
		return items;
	}

	private void setSelectedItems(Collection<? extends AbstractNetworkPanel<?>> items) {
		selectionModel.setValueIsAdjusting(true);
		
		try {
			selectionModel.clearSelection();
			
			var allItems = getAllItems(false);
			
			for (var p : items) {
				int idx = allItems.indexOf(p);
				selectionModel.addSelectionInterval(idx, idx);
			}
		} finally {
			selectionModel.setValueIsAdjusting(false);
		}
	}
	
	void selectAndSetCurrent(AbstractNetworkPanel<?> item) {
		if (item == null)
			return;
		
		// First select the clicked item
		var allItems = getAllItems(false);
		setSelectedIndex(allItems.indexOf(item));
		setCurrentNetwork(item.getModel().getNetwork());
	}
	
	CyNetwork getCurrentNetwork() {
		return currentNetwork;
	}
	
	void setCurrentNetwork(CyNetwork newValue) {
		if (!Objects.equals(newValue, currentNetwork)) {
			var oldValue = currentNetwork;
			currentNetwork = newValue;
			
			getRootNetworkListPanel().update();
			
			if (newValue != null)
				scrollTo(newValue);
			
			firePropertyChange("currentNetwork", oldValue, newValue);
		}
	}
	
	public List<SubNetworkPanel> getAllSubNetworkItems() {
		var list = new ArrayList<SubNetworkPanel>();
		
		for (var item : getRootNetworkListPanel().getAllItems())
			list.addAll(item.getAllItems());
		
		return list;
	}
	
	List<RootNetworkPanel> getSelectedRootNetworkItems() {
		var list = new ArrayList<RootNetworkPanel>();
		
		for (var rnp : getRootNetworkListPanel().getAllItems()) {
			if (rnp.isSelected())
				list.add(rnp);
		}
		
		return list;
	}
	
	List<SubNetworkPanel> getSelectedSubNetworkItems() {
		var list = new ArrayList<SubNetworkPanel>();
		
		for (var snp : getAllSubNetworkItems()) {
			if (snp.isSelected())
				list.add(snp);
		}
		
		return list;
	}
	
	int getTotalNetworkCount(boolean includeInvisible) {
		int count = getSubNetworkCount(includeInvisible);
		count += getRootNetworkListPanel().getRootNetworkCount();
		
		return count;
	}
	
	int getSubNetworkCount(boolean includeInvisible) {
		int count = 0;
		
		for (var item : getRootNetworkListPanel().getAllItems()) {
			if (includeInvisible || item.isExpanded())
				count += item.getAllItems().size();
		}
		
		return count;
	}
	
	private List<CyNetwork> getAllNetworks(boolean includeInvisible) {
		var items = getAllItems(includeInvisible);
		
		return getNetworks(items);
	}
	
	private static List<CyNetwork> getNetworks(Collection<? extends AbstractNetworkPanel<?>> items) {
		var list = new ArrayList<CyNetwork>();
		
		for (var snp : items)
			list.add(snp.getModel().getNetwork());
		
		return list;
	}
	
	protected void onMousePressedItem(MouseEvent e, AbstractNetworkPanel<?> item) {
		item.requestFocusInWindow();
		
		if (!e.isPopupTrigger() && SwingUtilities.isLeftMouseButton(e)) {
			// LEFT-CLICK...
			boolean isMac = LookAndFeelUtil.isMac();
			boolean isControlDown = (isMac && e.isMetaDown()) || (!isMac && e.isControlDown());
			
			if (isControlDown) {
				// COMMAND button down on MacOS or CONTROL button down on another OS.
				// Toggle this item's selection state
				toggleSelection(item);
			} else if (e.isShiftDown()) {
				// SHIFT key pressed...
				var allItems = getAllItems(false);
				int index = allItems.indexOf(item);
				shiftSelectTo(index);
			} else if (e.getClickCount() == 2) {
				// Rename Network...
				var network = item.getModel().getNetwork();
				var factory = serviceRegistrar.getService(EditNetworkTitleTaskFactory.class);
				var taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
				taskMgr.execute(factory.createTaskIterator(network));
			} else {
				// No SHIFT/CTRL pressed
				selectAndSetCurrent(item);
			}
		}
	}
	
	private void shiftSelectTo(int index) {
		int size = getTotalNetworkCount(false);
		
		if (index < 0 || index >= size)
			return;
		
		int anchor = selectionModel.getAnchorSelectionIndex();
		int lead = selectionModel.getLeadSelectionIndex();
		
		selectionModel.setValueIsAdjusting(true);
		
		// 1. remove everything between anchor and focus (lead)
		if (anchor != lead && (anchor >= 0 || lead >= 0)) {
			fireSelectedNetworksEvent = false;
			
			try {
				selectionModel.removeSelectionInterval(Math.max(0, anchor), Math.max(0, lead));
			} finally {
				fireSelectedNetworksEvent = true;
			}
		}
		
		// 2. add everything between anchor and the new index, which  should also be made the new lead
		selectionModel.addSelectionInterval(Math.max(0, anchor), index);
		
		selectionModel.setValueIsAdjusting(false);
		
		// 3. Make sure the lead component is focused
		getItem(index, false).requestFocusInWindow();
	}

	private void toggleSelection(AbstractNetworkPanel<?> item) {
		var allItems = getAllItems(false);
		int index = allItems.indexOf(item);
		
		if (selectionModel.isSelectedIndex(index))
			selectionModel.removeSelectionInterval(index, index);
		else
			selectionModel.addSelectionInterval(index, index);
		
		selectionModel.setValueIsAdjusting(true);
		
		if (selectionModel.isSelectedIndex(index)) {
			selectionModel.setAnchorSelectionIndex(index);
			selectionModel.moveLeadSelectionIndex(index);
		} else {
			index = selectionModel.getMaxSelectionIndex();
			selectionModel.setAnchorSelectionIndex(index);
			selectionModel.moveLeadSelectionIndex(index);
		}
		
		selectionModel.setValueIsAdjusting(false);
	}

	private void setKeyBindings(JComponent comp) {
		var actionMap = comp.getActionMap();
		var inputMap = comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		int ctrl = LookAndFeelUtil.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KeyAction.VK_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KeyAction.VK_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_SHIFT_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_SHIFT_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl), KeyAction.VK_CTRL_A);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl + InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_CTRL_SHIFT_A);
		
		actionMap.put(KeyAction.VK_UP, new KeyAction(KeyAction.VK_UP));
		actionMap.put(KeyAction.VK_DOWN, new KeyAction(KeyAction.VK_DOWN));
		actionMap.put(KeyAction.VK_SHIFT_UP, new KeyAction(KeyAction.VK_SHIFT_UP));
		actionMap.put(KeyAction.VK_SHIFT_DOWN, new KeyAction(KeyAction.VK_SHIFT_DOWN));
		actionMap.put(KeyAction.VK_CTRL_A, new KeyAction(KeyAction.VK_CTRL_A));
		actionMap.put(KeyAction.VK_CTRL_SHIFT_A, new KeyAction(KeyAction.VK_CTRL_SHIFT_A));
	}

	private void fireSelectedSubNetworksChange(Collection<CyNetwork> oldValue) {
		firePropertyChange("selectedSubNetworks", oldValue, getSelectedNetworks(false));
	}
	
	private void fireSelectedRootNetworksChange(Collection<CyRootNetwork> oldValue) {
		firePropertyChange("selectedRootNetworks", oldValue, getSelectedRootNetworks());
	}
	
	static void styleButton(AbstractButton btn, Font font) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setPreferredSize(new Dimension(32, 32));
	}
	
	// // Private Classes // //
	
	class RootNetworkListPanel extends JPanel implements Scrollable {
		
		private final JPanel filler = new JPanel();
		private final JLabel dropIconLabel = new JLabel();
		private final JLabel dropLabel = new JLabel("Drag network files here");
		private final Border dropBorder;
		private boolean scrollableTracksViewportHeight;
		
		/** Contains all root networks in their original CREATION order */
		private final List<CyRootNetwork> rootNetworks = new LinkedList<>();
		/** Contains all root networks and their panels in their current "view" order */
		private final Map<CyRootNetwork, RootNetworkPanel> items = new LinkedHashMap<>();
		
		RootNetworkListPanel() {
			setBackground(UIManager.getColor("Table.background"));
			
			var fg = UIManager.getColor("Label.disabledForeground");
			fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 120);
			
			dropBorder = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(3, 3, 3, 3),
					BorderFactory.createDashedBorder(fg, 2, 2, 2, true)
			);
			
			dropIconLabel.setIcon(
					new ImageIcon(getClass().getClassLoader().getResource("/images/drop-net-file-56.png")));
			dropIconLabel.setForeground(fg);
			
			dropLabel.setFont(dropLabel.getFont().deriveFont(18.0f).deriveFont(Font.BOLD));
			dropLabel.setForeground(fg);
			
			filler.setAlignmentX(LEFT_ALIGNMENT);
			filler.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
			filler.setBackground(getBackground());
			filler.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (!e.isPopupTrigger())
						deselectAll();
				}
			});
			
			var layout = new GroupLayout(filler);
			filler.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(CENTER, true)
							.addComponent(dropIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(dropLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(dropIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(dropLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(filler);
			
			updateDropArea();
			updateScrollableTracksViewportHeight();
		}
		
		public void sortNetworks(NetworksSortMode mode) {
			var sortedRootNets = new ArrayList<>(rootNetworks); // This has the CREATION order already
			
			if (mode == NAME)
				ViewUtil.sortNetworksByName(sortedRootNets);
			
			// Save the current items
			var map = new HashMap<>(items);
			// Remove/clear everything except the rootNetworks list
			items.clear();
			removeAll();
			
			for (var rootNet : sortedRootNets) {
				var rootNetPanel = map.get(rootNet); 
				add(rootNetPanel);
				items.put(rootNet, rootNetPanel);
				
				rootNetPanel.sortNetworks(mode);
			}
			
			add(filler);
			revalidate();
		}
		
		void update() {
			updateDropArea();
			
			for (var rnp : getAllItems()) {
				boolean currentRoot = false;
				
				for (var snp : rnp.getAllItems()) {
					boolean current = snp.getModel().getNetwork().equals(currentNetwork);
					snp.getModel().setCurrent(current);
					
					if (current)
						currentRoot = true;
				}
				
				rnp.getModel().setCurrent(currentRoot);
				rnp.update();
			}
			
			updateScrollableTracksViewportHeight();
		}

		void updateDropArea() {
			boolean empty = items == null || items.isEmpty();
			dropIconLabel.setVisible(empty);
			dropLabel.setVisible(empty);
			filler.setBorder(empty ? dropBorder : null);
		}
		
		void updateScrollableTracksViewportHeight() {
			boolean oldValue = scrollableTracksViewportHeight;
			
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
		
		int getRootNetworkCount() {
			return items.size();
		}
		
		RootNetworkPanel addItem(CyRootNetwork rootNetwork) {
			if (!items.containsKey(rootNetwork)) {
				var model = new RootNetworkPanelModel(rootNetwork, serviceRegistrar);
				var rootNetworkPanel = new RootNetworkPanel(model, isShowNetworkProvenanceHierarchy(),
						serviceRegistrar);
				rootNetworkPanel.setAlignmentX(LEFT_ALIGNMENT);
				
				rootNetworkPanel.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent e) {
						updateScrollableTracksViewportHeight();
					}
				});
				
				add(rootNetworkPanel, getComponentCount() - 1);
				items.put(rootNetwork, rootNetworkPanel);
				rootNetworks.add(rootNetwork);
				
				networkListOrder = null;
			}
			
			return items.get(rootNetwork);
		}
		
		RootNetworkPanel removeItem(CyRootNetwork rootNetwork) {
			var rootNetworkPanel = items.remove(rootNetwork);
			
			if (rootNetworkPanel != null)
				remove(rootNetworkPanel);
			
			rootNetworks.remove(rootNetwork);
			
			networkListOrder = null;
			
			return rootNetworkPanel;
		}
		
		void removeAllItems() {
			rootNetworks.clear();
			items.clear();
			removeAll();
			add(filler);
			
			networkListOrder = null;
		}
		
		/**
		 * @return All CyRootNetworks in their original CREATION order (not affected by the current "view" order).
		 */
		List<CyRootNetwork> getRootNetworks() {
			return rootNetworks;
		}
		
		RootNetworkPanel getItem(CyRootNetwork rootNetwork) {
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
	
	private void maybeShowViewPopup(SubNetworkPanel item) {
		var network = item.getModel().getNetwork();
		
		if (viewDialog != null) {
			if (viewDialog.getNetwork().equals(network)) // Clicking the same item--will probably never happen
				return;
		
			viewDialog.dispose();
		}
		
		if (item.getModel().getViewCount() > 1) {
			var windowAncestor = SwingUtilities.getWindowAncestor(item);
			var currentView = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			
			viewDialog = new NetworkViewPreviewDialog(network, currentView, windowAncestor, serviceRegistrar);
			
			viewDialog.addWindowFocusListener(new WindowFocusListener() {
				@Override
				public void windowLostFocus(WindowEvent e) {
					if (viewDialog != null)
						viewDialog.dispose();
				}
				@Override
				public void windowGainedFocus(WindowEvent e) {
				}
			});
			viewDialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					viewDialog = null;
				}
			});
			viewDialog.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE && viewDialog != null)
						viewDialog.dispose();
				}
			});
			viewDialog.addPropertyChangeListener("currentNetworkView", (PropertyChangeEvent evt) -> {
				// TODO Move to NetworkSelectionMediator
				serviceRegistrar.getService(CyApplicationManager.class)
						.setCurrentNetworkView((CyNetworkView) evt.getNewValue());
			});
			
			var screenPt = item.getViewIconLabel().getLocationOnScreen();
			var compPt = item.getViewIconLabel().getLocation();
			int xOffset = screenPt.x - compPt.x - item.getViewIconLabel().getWidth() / 2;
			int yOffset = screenPt.y - compPt.y + item.getViewIconLabel().getBounds().height - 2;
		    var pt = item.getViewIconLabel().getBounds().getLocation();
		    pt.translate(xOffset, yOffset);
		    
			viewDialog.setLocation(pt);
			viewDialog.setVisible(true);
			viewDialog.requestFocusInWindow();
		}
	}
	
	private class NetworkListSelectionModel extends DefaultListSelectionModel {
		
		public NetworkListSelectionModel() {
			// Here is where we listen to the changed indexes in order to:
			// a) select/deselect the the actual items
			// b) call fireSelectedRootNetworksChange and fireSelectedSubNetworksChange when necessary
			addListSelectionListener(evt -> {
				if (!evt.getValueIsAdjusting()) {
					var oldRootSelection = getSelectedRootNetworks();
					var oldSubSelection = getSelectedNetworks(false);
					boolean rootChanged = false;
					boolean subChanged = false;
					
					int first = evt.getFirstIndex();
					int last = evt.getLastIndex();
					
					ignoreSelectionEvents = true;
					
					try {
						var allItems = getAllItems(false); // Ignore collapsed items! 
						
						for (int i = first; i <= last; i++) {
							if (i >= allItems.size())
								break;
							
							var p = allItems.get(i);
							
							boolean b = setSelected(p, selectionModel.isSelectedIndex(i));
						
							if (b) {
								if (p.getModel().getNetwork() instanceof CyRootNetwork)
									rootChanged = true;
								else
									subChanged = true;
							}
						}
					} finally {
						ignoreSelectionEvents = false;
						updateNetworkHeader();
					}
					
					if (fireSelectedNetworksEvent) {
						if (rootChanged)
							fireSelectedRootNetworksChange(oldRootSelection);
						if (subChanged)
							fireSelectedSubNetworksChange(oldSubSelection);
					}
				}
			});
		}
		
		/**
		 * This may have to deselect invisible (collapsed) items,
		 * but the selection model (actually our ListSelectionListener) only handles indexes of visible items.
		 * So we need to override this method to prevent it from triggering our
		 * ListSelectionListener with invalid (from hidden items) indexes. 
		 */
		@Override
		public void clearSelection() {
			var oldRootSelection = getSelectedRootNetworks();
			var oldSubSelection = getSelectedNetworks(false);
			boolean rootChanged = false;
			boolean subChanged = false;
			
			setValueIsAdjusting(true);
			ignoreSelectionEvents = true;
			
			super.clearSelection();
			
			try {
				var allItems = getAllItems(true);
				
				for (var p : allItems) {
					boolean b = setSelected(p, false);
				
					if (b) {
						if (p.getModel().getNetwork() instanceof CyRootNetwork)
							rootChanged = true;
						else
							subChanged = true;
					}
				}
				
				
			} finally {
				ignoreSelectionEvents = false;
				// so it does not fireValueChanged with false in the next line,
				// which would trigger our listener with potential bad indexes
				fireValueChanged(true);
				setValueIsAdjusting(false);
				
				updateNetworkHeader();
			}
			
			// Don't forget to fire these events!
			if (fireSelectedNetworksEvent) {
				if (rootChanged)
					fireSelectedRootNetworksChange(oldRootSelection);
				if (subChanged)
					fireSelectedSubNetworksChange(oldSubSelection);
			}
		}
		
		@Override
		public void fireValueChanged(boolean isAdjusting) { // We need this method to be public!
			super.fireValueChanged(isAdjusting);
		}
	}
	
	private class KeyAction extends AbstractAction {

		final static String VK_UP = "VK_UP";
		final static String VK_DOWN = "VK_DOWN";
		final static String VK_SHIFT_UP = "VK_SHIFT_UP";
		final static String VK_SHIFT_DOWN = "VK_SHIFT_DOWN";
		final static String VK_CTRL_A = "VK_CTRL_A";
		final static String VK_CTRL_SHIFT_A = "VK_CTRL_SHIFT_A";
		
		KeyAction(String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			var allItems = getAllItems(false);
			
			if (allItems.isEmpty())
				return;
			
			var cmd = e.getActionCommand();
			boolean shift = cmd.startsWith("VK_SHIFT_");
			int idx = selectionModel.getLeadSelectionIndex();
			int newIdx = idx;
			
			if (cmd.equals(VK_UP) || cmd.equals(VK_SHIFT_UP))
				newIdx = idx - 1;
			else if (cmd.equals(VK_DOWN) || cmd.equals(VK_SHIFT_DOWN))
				newIdx = idx + 1;
			else if (cmd.equals(VK_CTRL_A))
				selectAll();
			else if (cmd.equals(VK_CTRL_SHIFT_A))
				deselectAll();
			
			if (newIdx != idx) {
				if (shift)
					shiftSelectTo(newIdx);
				else
					setSelectedIndex(newIdx);
			}
		}
	}
	
	public class NetworkDropListener implements DropTargetListener {

		private final CyRootNetwork rootNetwork;
		/** the zone that accepts the drop */
		private final JComponent targetComponent;
		/** the component that is highlighted on dragEnter -- It may or may not be the same as the target */
		private final JComponent focusComponent;
		private final DropTarget dropTarget;
		private Border originalBorder;
		
		public NetworkDropListener(CyRootNetwork rootNetwork) {
			this.rootNetwork = rootNetwork;
			targetComponent = rootNetwork == null ? getNetworksPanel() : getNetworkItem(rootNetwork);
			focusComponent = targetComponent == getNetworksPanel() ? getRootNetworkScroll() : targetComponent;
			
			targetComponent.setTransferHandler(new TransferHandler() {
		        @Override
		        public boolean canImport(TransferHandler.TransferSupport info) {
		        	return dropTarget != null && dropTarget.isActive() && isAcceptable(info);
		        }
		        @Override
		        public boolean importData(TransferHandler.TransferSupport info) {
		            return dropTarget != null && dropTarget.isActive() && info.isDrop() && isAcceptable(info);
		        }
		    });
			
			dropTarget = new DropTarget(targetComponent, this);
		}

		@Override
		public void dragEnter(DropTargetDragEvent evt) {
			originalBorder = focusComponent.getBorder();
			focusComponent.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), 2));
		}

		@Override
		public void dragExit(DropTargetEvent evt) {
			focusComponent.setBorder(originalBorder);
		}

		@Override
		public void dragOver(DropTargetDragEvent evt) {
		}
		
		@Override
		public void dropActionChanged(DropTargetDragEvent evt) {
		}

		@Override
		@SuppressWarnings("unchecked")
		public void drop(DropTargetDropEvent evt) {
			focusComponent.setBorder(originalBorder);
	        
			if (!isAcceptable(evt)) {
				evt.rejectDrop();
	        	return;
			}
			
			evt.acceptDrop(evt.getDropAction());
			var t = evt.getTransferable();
			
			if (evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {       
	            // Get the fileList that is being dropped.
		        final List<File> data;
		        
		        try {
		            data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
		        } catch (Exception e) { 
		        	logger.error("Cannot load network files by Drag-and-Drop.", e);
		        	return; 
		        }
		        
		        new Thread(() -> {
		        	loadFiles(data);
		        }).start();
	        }
		}
		
		private void loadFiles(List<File> data) {
			var taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			taskManager.execute(new TaskIterator(new LoadFileListTask(data, rootNetwork, serviceRegistrar)));
		}
		
		private boolean isAcceptable(DropTargetDropEvent evt) {
			return evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}

		private boolean isAcceptable(TransferHandler.TransferSupport info) {
			return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}
	}
}
