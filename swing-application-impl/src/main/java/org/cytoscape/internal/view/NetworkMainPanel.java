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
import java.util.Collections;
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
	
	private AbstractNetworkPanel<?> selectionHead;
	private AbstractNetworkPanel<?> selectionTail;
	private AbstractNetworkPanel<?> lastSelected;
	
	private boolean ignoreSelectionEvents;
	private boolean doNotUpdateCollapseExpandButtons;
	
	private NetworkViewPreviewDialog viewDialog;
	private TextIcon icon;
	
	private NetworksSortMode sortMode = CREATION;
	private Map<Long, Integer> networkListOrder;

	private final NetworkSearchBar networkSearchBar;
	private final CyServiceRegistrar serviceRegistrar;

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public NetworkMainPanel(NetworkSearchBar networkSearchBar, CyServiceRegistrar serviceRegistrar) {
		this.networkSearchBar = networkSearchBar;
		this.serviceRegistrar = serviceRegistrar;
		
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
			
			expandAllButton.addActionListener(e -> {
				expandAllRootNetworks();
			});
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
			
			collapseAllButton.addActionListener(e -> collapseAllRootNetworks());
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
			
			optionsBtn.addActionListener(e -> {
				getNetworkOptionsMenu().show(optionsBtn, 0, optionsBtn.getHeight());
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
	
	/**
	 * @return The model index of the network, which means the current sort mode is ignored.
	 */
	public int indexOf(CyNetwork network) {
		var netPos = getNetworkListOrder();
		int idx = netPos.get(network.getSUID());
		
		return idx;
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
	 * @return
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
		if (Util.equalSets(selectedNetworks, getSelectedNetworks(false))) {
			if (selectedNetworks.isEmpty()) {
				// Even if there are no selected networks still make sure we don't cause a memory leak.
				selectionHead = selectionTail = lastSelected = null;
			}
			return;
		}

		ignoreSelectionEvents = true;
		
		try {
			for (var rnp : getRootNetworkListPanel().getAllItems()) {
				setSelected(rnp, selectedNetworks.contains(rnp.getModel().getNetwork()));
				
				for (var snp : rnp.getAllItems()) {
					boolean selected = selectedNetworks.contains(snp.getModel().getNetwork());
					
					if (selected && !rnp.isExpanded())
						rnp.expand();
					
					setSelected(snp, selected);
				}
			}
		} finally {
			ignoreSelectionEvents = false;
		}
		
		var selectedItems = getSelectedItems();
		selectionHead = selectedItems.isEmpty() ? null : selectedItems.get(0);
		selectionTail = selectedItems.size() > 1 ? selectedItems.get(selectedItems.size() - 1) : null;
		lastSelected = selectedItems.isEmpty() ? null : selectedItems.get(selectedItems.size() - 1);
		
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
			getRootNetworkListPanel().removeAllItems();
		} finally {
			doNotUpdateCollapseExpandButtons = false;
			ignoreSelectionEvents = false;
		}
		
		lastSelected = selectionHead = selectionTail = null;
		
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
				// Deselect its selected subnetworks first
				if (Boolean.FALSE.equals(e.getNewValue())) {
					var selectedItems = getSelectedItems();
					
					if (!selectedItems.isEmpty()) {
						selectedItems.removeAll(item.getAllItems());
						setSelectedItems(selectedItems);
					}
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
		
		// Scroll to new item
		rootNetPanel.expand();
		scrollTo(network);
		
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
			
			if (!selected) {
				 if (item == selectionHead) selectionHead = null;
				 if (item == selectionTail) selectionTail = null;
			}
			
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
	
	private AbstractNetworkPanel<?> getPreviousItem(AbstractNetworkPanel<?> item, boolean includeInvisible) {
		var allItems = getAllItems(includeInvisible);
		int index = allItems.indexOf(item);
		
		return index > 0 ? allItems.get(index - 1) : null;
	}
	
	private AbstractNetworkPanel<?> getNextItem(AbstractNetworkPanel<?> item, boolean includeInvisible) {
		var allItems = getAllItems(includeInvisible);
		int index = allItems.indexOf(item);
		
		return index >= 0 && index < allItems.size() - 1 ? allItems.get(index + 1) : null;
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
		int total = getSubNetworkCount();
		
		if (total == 0) {
			getNetworkSelectionLabel().setText(null);
		} else {
			int selected = countSelectedSubNetworks(false);
			getNetworkSelectionLabel().setText(
					selected + " of " + total + " Network" + (total == 1 ? "" : "s") + " selected");
		}
		
		getNetworkHeader().updateUI();
	}
	
	private void collapseAllRootNetworks() {
		// First deselect all subnetwork items, to prevent selection issues
		// caused by selection and current net/view events being fired
		// every time one of the root panels is collapsed
		setSelectedItems(getSelectedRootNetworkItems()); // Keep only root networks that are already selected
		
		doNotUpdateCollapseExpandButtons = true;
		var allItems = getRootNetworkListPanel().getAllItems();
		
		for (var item : allItems)
			item.collapse();
		
		doNotUpdateCollapseExpandButtons = false;
		updateCollapseExpandButtons();
	}

	private void expandAllRootNetworks() {
		doNotUpdateCollapseExpandButtons = true;
		var allItems = getRootNetworkListPanel().getAllItems();
		
		for (var item : allItems)
			item.expand();
		
		doNotUpdateCollapseExpandButtons = false;
		updateCollapseExpandButtons();
	}
	
	private void selectAll() {
		var allItems = getAllItems(false);
		
		if (!allItems.isEmpty()) {
			setSelectedItems(getAllItems(false));
			selectionHead = allItems.get(0);
			selectionTail = allItems.get(allItems.size() - 1);
			lastSelected = selectionTail;
		}
	}
	
	private void deselectAll() {
		setSelectedItems(Collections.emptyList());
		setCurrentNetwork(null);
		lastSelected = selectionHead = selectionTail = null;
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
		var oldRootSelection = getSelectedRootNetworks();
		var oldSubSelection = getSelectedNetworks(false);
		boolean rootChanged = false;
		boolean subChanged = false;
		ignoreSelectionEvents = true;
		
		try {
			for (var p : getAllItems(true)) {
				boolean b = setSelected(p, items.contains(p));
				
				if (b) {
					if (p.getModel().getNetwork() instanceof CyRootNetwork)
						rootChanged = true;
					else if (p.getModel().getNetwork() instanceof CySubNetwork)
						subChanged = true;
				}
			}
		} finally {
			ignoreSelectionEvents = false;
			updateNetworkHeader();
		}
		
		if (rootChanged)
			fireSelectedRootNetworksChange(oldRootSelection);
		if (subChanged)
			fireSelectedSubNetworksChange(oldSubSelection);
	}
	
	void selectAndSetCurrent(AbstractNetworkPanel<?> item) {
		if (item == null)
			return;
		
		// First select the clicked item
		setSelectedItems((Collections.singleton(item)));
		lastSelected = selectionHead = item;
		selectionTail = null;
		
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
	
	Collection<RootNetworkPanel> getSelectedRootNetworkItems() {
		var list = new ArrayList<RootNetworkPanel>();
		
		for (var rnp : getRootNetworkListPanel().getAllItems()) {
			if (rnp.isSelected())
				list.add(rnp);
		}
		
		return list;
	}
	
	Collection<SubNetworkPanel> getSelectedSubNetworkItems() {
		var list = new ArrayList<SubNetworkPanel>();
		
		for (var snp : getAllSubNetworkItems()) {
			if (snp.isSelected())
				list.add(snp);
		}
		
		return list;
	}
	
	int getSubNetworkCount() {
		int count = 0;
		
		for (var item : getRootNetworkListPanel().getAllItems())
			count += item.getAllItems().size();
		
		return count;
	}
	
	private static Set<CyNetwork> getNetworks(Collection<SubNetworkPanel> items) {
		var list = new LinkedHashSet<CyNetwork>();
		
		for (var snp : items)
			list.add(snp.getModel().getNetwork());
		
		return list;
	}
	
	protected void onMousePressedItem(MouseEvent e, AbstractNetworkPanel<?> item) {
		item.requestFocusInWindow();
		
		if (!e.isPopupTrigger() && SwingUtilities.isLeftMouseButton(e)) {
			// LEFT-CLICK...
			boolean isMac = LookAndFeelUtil.isMac();
			
			if ((isMac && e.isMetaDown()) || (!isMac && e.isControlDown())) {
				// COMMAND button down on MacOS or CONTROL button down on another OS.
				// Toggle this item's selection state
				item.setSelected(!item.isSelected());
				// Find new selection range head
				selectionHead = item.isSelected() ? item : findNextSelectionHead(selectionHead);
				lastSelected = selectionHead;
			} else {
				if (e.isShiftDown()) {
					selectTo(item);
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

	private void selectTo(AbstractNetworkPanel<?> target) {
		if (selectionHead != null && selectionHead.isVisible() && selectionHead.isSelected() && selectionHead != target) {
			var oldRootSelection = getSelectedRootNetworks();
			var oldSubSelection = getSelectedNetworks(false);
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
			}
			
			if (changed) {
				fireSelectedRootNetworksChange(oldRootSelection);
				fireSelectedSubNetworksChange(oldSubSelection);
			}
		} else if (!target.isSelected()) {
			target.setSelected(true);
			lastSelected = target;
		}
	}

	private boolean changeRangeSelection(AbstractNetworkPanel<?> item1, AbstractNetworkPanel<?> item2,
			boolean selected) {
		boolean changed = false;
		
		var items = getAllItems(false);
		int idx1 = items.indexOf(item1);
		int idx2 = items.indexOf(item2);
		
		final List<AbstractNetworkPanel<?>> subList;
		
		if (idx2 >= idx1) {
			subList = items.subList(idx1 + 1, idx2 + 1);
			lastSelected = selectionTail;
		} else {
			subList = items.subList(idx2, idx1);
			Collections.reverse(subList);
			lastSelected = selectionHead;
		}
		
		for (var nextItem : subList) {
			if (nextItem.isVisible() && nextItem.isSelected() != selected) {
				nextItem.setSelected(selected);
				changed = true;
			}
		}
		
		return changed;
	}
	
	private AbstractNetworkPanel<?> findNextSelectionHead(AbstractNetworkPanel<?> fromItem) {
		AbstractNetworkPanel<?> head = null;
		var items = getAllItems(false);
		
		if (fromItem != null) {
			var subList = items.subList(items.indexOf(fromItem), items.size());
			
			// Try with the tail subset first
			for (var nextItem : subList) {
				if (nextItem.isVisible() && nextItem.isSelected()) {
					head = nextItem;
					break;
				}
			}
			
			if (head == null) {
				// Try with the head subset
				subList = items.subList(0, items.indexOf(fromItem));
				var li = subList.listIterator(subList.size());
				
				while (li.hasPrevious()) {
					var previousItem = li.previous();
					
					if (previousItem.isVisible() && previousItem.isSelected()) {
						head = previousItem;
						break;
					}
				}
			}
		}
		
		return head;
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
			
			// TODO clear or fix previous selection?
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
			var cmd = e.getActionCommand();
			var allItems = getAllItems(false);
			
			if (allItems.isEmpty())
				return;
			
			if (cmd.equals(VK_UP)) {
				if (lastSelected != null) {
					var previous = getPreviousItem(lastSelected, false);
					selectAndSetCurrent(previous != null ? previous : allItems.get(0));
				}
			} else if (cmd.equals(VK_DOWN)) {
				if (lastSelected != null) {
					var next = getNextItem(lastSelected, false);
					selectAndSetCurrent(next != null ? next : allItems.get(allItems.size() - 1));
				}
			} else if (cmd.equals(VK_SHIFT_UP)) {
				var previous = getPreviousItem(lastSelected, false);
				
				if (previous != null)
					selectTo(previous);
			} else if (cmd.equals(VK_SHIFT_DOWN)) {
				var next = getNextItem(lastSelected, false);
				
				if (next != null)
					selectTo(next);
			} else if (cmd.equals(VK_CTRL_A)) {
				selectAll();
			} else if (cmd.equals(VK_CTRL_SHIFT_A)) {
				deselectAll();
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
