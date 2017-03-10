package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_DOWN;
import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_UP;
import static org.cytoscape.util.swing.IconManager.ICON_COG;
import static org.cytoscape.util.swing.IconManager.ICON_PLUS;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
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
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.internal.util.Util;
import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@SuppressWarnings("serial")
public class NetworkMainPanel extends JPanel implements CytoPanelComponent2 {

	public static final float ICON_FONT_SIZE = 22.0f;
	
	private static final String TITLE = "Network";
	private static final String ID = "org.cytoscape.Network";
	
	private static final Dimension PANEL_SIZE = new Dimension(400, 700);

	private JScrollPane rootNetworkScroll;
	private RootNetworkListPanel rootNetworkListPanel;
	private JPanel networkHeader;
	private JPanel networkToolBar;
	private JButton expandAllButton;
	private JButton collapseAllButton;
	private JButton optionsBtn;
	private JLabel networkSelectionLabel;
	private JButton createButton;

	private CyNetwork currentNetwork;
	
	private final Map<CyTable, CyNetwork> nameTables = new WeakHashMap<>();
	private final Map<CyTable, CyNetwork> nodeEdgeTables = new WeakHashMap<>();

	private AbstractNetworkPanel<?> selectionHead;
	private AbstractNetworkPanel<?> selectionTail;
	private AbstractNetworkPanel<?> lastSelected;
	
	private boolean ignoreSelectionEvents;
	private boolean doNotUpdateCollapseExpandButtons;
	
	private NetworkViewPreviewDialog viewDialog;

	private CyServiceRegistrar serviceRegistrar;
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(NetworkMainPanel.class);

	public NetworkMainPanel(final CyServiceRegistrar serviceRegistrar) {
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
		return null;
	}
	
	private void init() {
		setPreferredSize(PANEL_SIZE);
		setSize(PANEL_SIZE);
		setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua

		setLayout(new BorderLayout());
		add(getNetworkHeader(), BorderLayout.NORTH);
		add(getRootNetworkScroll(), BorderLayout.CENTER);
		add(getNetworkToolBar(), BorderLayout.SOUTH);
		
		updateNetworkHeader();
		
		getNetworkToolBar().setVisible(isShowNetworkToolBar());
	}
	
	JScrollPane getRootNetworkScroll() {
		if (rootNetworkScroll == null) {
			rootNetworkScroll = new JScrollPane(getRootNetworkListPanel());
			rootNetworkScroll.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					getRootNetworkListPanel().updateScrollableTracksViewportHeight();
				}
			});
			
			new CyDropListener(rootNetworkScroll, serviceRegistrar);
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
			
			expandAllButton.addActionListener((ActionEvent e) -> {
				expandAllRootNetworks();
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
			
			collapseAllButton.addActionListener((ActionEvent e) -> {
				collapseAllRootNetworks();
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
			
			optionsBtn.addActionListener((ActionEvent e) -> {
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
	
	private JPanel getNetworkToolBar() {
		if (networkToolBar == null) {
			networkToolBar = new JPanel();
			
			final GroupLayout layout = new GroupLayout(networkToolBar);
			networkToolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(getCreateButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getCreateButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return networkToolBar;
	}
	
	private JButton getCreateButton() {
		if (createButton == null) {
			createButton = new JButton(ICON_PLUS);
			createButton.setToolTipText("Add...");
			styleButton(createButton, serviceRegistrar.getService(IconManager.class).getIconFont(ICON_FONT_SIZE));

			createButton.addActionListener((ActionEvent e) -> {
				getCreateMenu().show(createButton, 0, createButton.getHeight());
			});
		}
		
		return createButton;
	}
	
	private JPopupMenu getCreateMenu() {
		final JPopupMenu menu = new JPopupMenu();
		final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
		
		{
			final LoadNetworkFileTaskFactory factory = serviceRegistrar.getService(LoadNetworkFileTaskFactory.class);
			
			final JMenuItem mi = new JMenuItem("New Network From File...");
			mi.addActionListener((ActionEvent e) -> {
				taskMgr.execute(factory.createTaskIterator());
			});
			mi.setEnabled(factory.isReady());
			menu.add(mi);
		}
		{
			final CyAction action = serviceRegistrar.getService(CyAction.class,
					"(id=showImportNetworkFromWebServiceDialogAction)");
			
			final JMenuItem mi = new JMenuItem("New Network From Database...");
			mi.addActionListener((ActionEvent e) -> {
				action.actionPerformed(e);
			});
			mi.setEnabled(action.isEnabled());
			menu.add(mi);
		}

		return menu;
	}
	
	private JPopupMenu getNetworkOptionsMenu() {
		final JPopupMenu menu = new JPopupMenu();
		
		{
			final JMenuItem mi = new JCheckBoxMenuItem("Show Network Provenance Hierarchy");
			mi.addActionListener((ActionEvent e) -> {
				setShowNetworkProvenanceHierarchy(mi.isSelected());
			});
			mi.setSelected(isShowNetworkProvenanceHierarchy());
			menu.add(mi);
		}
		{
			final JMenuItem mi = new JCheckBoxMenuItem("Show Number of Nodes and Edges");
			mi.addActionListener((ActionEvent e) -> {
				setShowNodeEdgeCount(mi.isSelected());
			});
			mi.setSelected(isShowNodeEdgeCount());
			menu.add(mi);
		}
		{
			final JMenuItem mi = new JCheckBoxMenuItem("Show Network Toolbar");
			mi.addActionListener((ActionEvent e) -> {
				setShowNetworkToolBar(mi.isSelected());
			});
			mi.setSelected(isShowNetworkToolBar());
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
	
	public int indexOf(final CyNetwork network) {
		int idx = -1;
		final AbstractNetworkPanel<?> item = getNetworkItem(network);
		
		if (item != null) {
			final List<AbstractNetworkPanel<?>> allItems = getAllItems(true);
			idx = allItems.indexOf(item);
		}
		
		return idx;
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
		updateNodeEdgeCount();
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
	
	public void setSelectedNetworks(final Collection<CyNetwork> selectedNetworks) {
		if (Util.equalSets(selectedNetworks, getSelectedNetworks(false)))
			return;
		
		ignoreSelectionEvents = true;
		
		try {
			for (final RootNetworkPanel rnp : getRootNetworkListPanel().getAllItems()) {
				setSelected(rnp, selectedNetworks.contains(rnp.getModel().getNetwork()));
				
				for (SubNetworkPanel snp : rnp.getAllItems()) {
					final boolean selected = selectedNetworks.contains(snp.getModel().getNetwork());
					
					if (selected && !rnp.isExpanded())
						rnp.expand();
					
					setSelected(snp, selected);
				}
			}
		} finally {
			ignoreSelectionEvents = false;
		}
		
		final List<AbstractNetworkPanel<?>> selectedItems = getSelectedItems();
		selectionHead = selectedItems.isEmpty() ? null : selectedItems.get(0);
		selectionTail = selectedItems.size() > 1 ? selectedItems.get(selectedItems.size() - 1) : null;
		lastSelected = selectedItems.isEmpty() ? null : selectedItems.get(selectedItems.size() - 1);
		
		updateNetworkHeader();
	}
	
	public int countSelectedRootNetworks() {
		return getSelectedRootNetworkItems().size();
	}
	
	public int countSelectedSubNetworks(final boolean includeSelectedRootNetworks) {
		return getSelectedNetworks(includeSelectedRootNetworks).size();
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
	}
	
	public boolean isShowNodeEdgeCount() {
		return "true".equalsIgnoreCase(ViewUtil.getViewProperty(ViewUtil.SHOW_NODE_EDGE_COUNT_KEY, serviceRegistrar));
	}
	
	public void setShowNodeEdgeCount(final boolean b) {
		ViewUtil.setViewProperty(ViewUtil.SHOW_NODE_EDGE_COUNT_KEY, "" + b, serviceRegistrar);
		updateNodeEdgeCount();
	}
	
	public boolean isShowNetworkProvenanceHierarchy() {
		return "true".equalsIgnoreCase(
				ViewUtil.getViewProperty(ViewUtil.SHOW_NETWORK_PROVENANCE_HIERARCHY_KEY, serviceRegistrar));
	}
	
	public void setShowNetworkProvenanceHierarchy(final boolean b) {
		for (final RootNetworkPanel item : getRootNetworkListPanel().getAllItems())
			item.setShowIndentation(b);
		
		ViewUtil.setViewProperty(ViewUtil.SHOW_NETWORK_PROVENANCE_HIERARCHY_KEY, "" + b, serviceRegistrar);
	}
	
	public boolean isShowNetworkToolBar() {
		return "true".equalsIgnoreCase(
				ViewUtil.getViewProperty(ViewUtil.SHOW_NETWORK_TOOL_BAR, serviceRegistrar));
	}
	
	public void setShowNetworkToolBar(final boolean b) {
		ViewUtil.setViewProperty(ViewUtil.SHOW_NETWORK_TOOL_BAR, "" + b, serviceRegistrar);
		getNetworkToolBar().setVisible(b);
	}
	
	public void scrollTo(final CyNetwork network) {
		final AbstractNetworkPanel<?> target;
		
		if (network instanceof CySubNetwork)
			target = getSubNetworkPanel(network);
		else
			target = getRootNetworkPanel(network);
		
		if (target != null) {
			if (target instanceof SubNetworkPanel) {
				final RootNetworkPanel rnp = getRootNetworkPanel(
						((SubNetworkPanel) target).getModel().getNetwork().getRootNetwork());
				rnp.expand();
			}
			
			((JComponent) target.getParent()).scrollRectToVisible(target.getBounds());
		}
	}

	// // Private Methods // //
	
	protected SubNetworkPanel addNetwork(final CySubNetwork network) {
		final CyRootNetwork rootNetwork = network.getRootNetwork();
		RootNetworkPanel rootNetPanel = getRootNetworkPanel(rootNetwork);
		
		if (rootNetPanel == null) {
			rootNetPanel = getRootNetworkListPanel().addItem(rootNetwork);
			setKeyBindings(rootNetPanel);
			
			final RootNetworkPanel item = rootNetPanel;
			
			item.addPropertyChangeListener("expanded", (PropertyChangeEvent e) -> {
				// Deselect its selected subnetworks first
				if (e.getNewValue() == Boolean.FALSE) {
					final List<AbstractNetworkPanel<?>> selectedItems = getSelectedItems();
					
					if (!selectedItems.isEmpty()) {
						selectedItems.removeAll(item.getAllItems());
						setSelectedItems(selectedItems);
					}
				}
				
				updateCollapseExpandButtons();
			});
			
			firePropertyChange("rootNetworkPanelCreated", null, item);
		}
		
		final SubNetworkPanel subNetPanel = rootNetPanel.addItem(network);
		setKeyBindings(subNetPanel);
		
		subNetPanel.addPropertyChangeListener("selected", (PropertyChangeEvent e) -> {
			if (!ignoreSelectionEvents) {
				updateNetworkSelectionLabel();
				
				final boolean selected = (boolean) e.getNewValue();
				final Set<CyNetwork> oldSelection = getSelectedNetworks(false);
				
				if (selected) // Then it did not belong to the old selection value
					oldSelection.remove(subNetPanel.getModel().getNetwork());
				else // It means it was selected before
					oldSelection.add(subNetPanel.getModel().getNetwork());
				
				fireSelectedNetworksChange(oldSelection);
			}
		});
// TODO Uncomment when multiple views support is enabled
//		subNetPanel.getViewIconLabel().addMouseListener(new MouseAdapter() {
//			@Override
//			public void mousePressed(MouseEvent e) {
//				maybeShowViewPopup(subNetPanel);
//			}
//		});
		
		firePropertyChange("subNetworkPanelCreated", null, subNetPanel);
		
		// Scroll to new item
		rootNetPanel.expand();
		scrollTo(network);
		
		nameTables.put(network.getDefaultNetworkTable(), network);
		nodeEdgeTables.put(network.getDefaultNodeTable(), network);
		nodeEdgeTables.put(network.getDefaultEdgeTable(), network);
		
		return subNetPanel;
	}
	
	/**
	 * Remove a network from the panel.
	 */
	protected void removeNetwork(final CySubNetwork network) {
		nameTables.values().removeAll(Collections.singletonList(network));
		nodeEdgeTables.values().removeAll(Collections.singletonList(network));
		
		invokeOnEDT(() -> {
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
			}
		});
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

	protected AbstractNetworkPanel<?> getNetworkItem(final CyNetwork net) {
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
	
	RootNetworkPanel getRootNetworkPanel(final CyNetwork net) {
		if (net instanceof CyRootNetwork)
			return getRootNetworkListPanel().getItem((CyRootNetwork) net);
		
		return null;
	}
	
	SubNetworkPanel getSubNetworkPanel(final CyNetwork net) {
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
	
	protected void updateNodeEdgeCount() {
		invokeOnEDT(() -> {
			int nodeLabelWidth = 0;
			int edgeLabelWidth = 0;
			
			for (SubNetworkPanel snp : getAllSubNetworkItems()) {
				snp.getNodeCountLabel().setVisible(isShowNodeEdgeCount());
				snp.getEdgeCountLabel().setVisible(isShowNodeEdgeCount());
				
				if (isShowNodeEdgeCount()) {
					// Update node/edge count label text
					snp.updateCountLabels();
					// Get max label width
					final FontMetrics nfm = snp.getNodeCountLabel().getFontMetrics(snp.getNodeCountLabel().getFont());
					final FontMetrics efm = snp.getEdgeCountLabel().getFontMetrics(snp.getEdgeCountLabel().getFont());
					
					nodeLabelWidth = Math.max(nodeLabelWidth, nfm.stringWidth(snp.getNodeCountLabel().getText()));
					edgeLabelWidth = Math.max(edgeLabelWidth, efm.stringWidth(snp.getEdgeCountLabel().getText()));
				}
			}
			
			if (!isShowNodeEdgeCount())
				return;
			
			// Apply max width values to all labels so they align properly
			for (SubNetworkPanel snp : getAllSubNetworkItems()) {
				final Dimension nd = new Dimension(nodeLabelWidth, snp.getNodeCountLabel().getPreferredSize().height);
				snp.getNodeCountLabel().setPreferredSize(nd);
				snp.getNodeCountLabel().setSize(nd);
				
				final Dimension ed = new Dimension(edgeLabelWidth, snp.getEdgeCountLabel().getPreferredSize().height);
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
			final int selected = countSelectedSubNetworks(false);
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
		setCurrentNetwork(null);
		lastSelected = selectionHead = selectionTail = null;
	}
	
	public List<AbstractNetworkPanel<?>> getAllItems(final boolean includeInvisible) {
		final ArrayList<AbstractNetworkPanel<?>> list = new ArrayList<>();
		
		for (final RootNetworkPanel item : getRootNetworkListPanel().getAllItems()) {
			list.add(item);
			
			if (includeInvisible || item.isExpanded())
				list.addAll(item.getAllItems());
		}
		
		return list;
	}
	
	List<AbstractNetworkPanel<?>> getSelectedItems() {
		final List<AbstractNetworkPanel<?>> items = getAllItems(true);
		final Iterator<AbstractNetworkPanel<?>> iterator = items.iterator();
		
		while (iterator.hasNext()) {
			if (!iterator.next().isSelected())
				iterator.remove();
		}
		
		return items;
	}

	private void setSelectedItems(final Collection<? extends AbstractNetworkPanel<?>> items) {
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
		}
		
		if (changed)
			fireSelectedNetworksChange(oldSelection);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void selectAndSetCurrent(final AbstractNetworkPanel<?> item) {
		if (item == null)
			return;
		
		// First select the clicked item
		setSelectedItems((Set) (Collections.singleton(item)));
		lastSelected = selectionHead = item;
		selectionTail = null;
		
		setCurrentNetwork(item.getModel().getNetwork());
	}
	
	CyNetwork getCurrentNetwork() {
		return currentNetwork;
	}
	
	void setCurrentNetwork(final CyNetwork newValue) {
		if ((currentNetwork == null && newValue != null)
				|| (currentNetwork != null && !currentNetwork.equals(newValue))) {
			final CyNetwork oldValue = currentNetwork;
			currentNetwork = newValue;
			
			getRootNetworkListPanel().update();
			
			if (newValue != null)
				scrollTo(newValue);
			
			firePropertyChange("currentNetwork", oldValue, newValue);
		}
	}
	
	public List<SubNetworkPanel> getAllSubNetworkItems() {
		final ArrayList<SubNetworkPanel> list = new ArrayList<>();
		
		for (final RootNetworkPanel item : getRootNetworkListPanel().getAllItems())
			list.addAll(item.getAllItems());
		
		return list;
	}
	
	Collection<RootNetworkPanel> getSelectedRootNetworkItems() {
		final ArrayList<RootNetworkPanel> list = new ArrayList<>();
		
		for (final RootNetworkPanel rnp : getRootNetworkListPanel().getAllItems()) {
			if (rnp.isSelected())
				list.add(rnp);
		}
		
		return list;
	}
	
	Collection<SubNetworkPanel> getSelectedSubNetworkItems() {
		final ArrayList<SubNetworkPanel> list = new ArrayList<>();
		
		for (final SubNetworkPanel snp : getAllSubNetworkItems()) {
			if (snp.isSelected())
				list.add(snp);
		}
		
		return list;
	}
	
	int getSubNetworkCount() {
		int count = 0;
		
		for (final RootNetworkPanel item : getRootNetworkListPanel().getAllItems())
			count += item.getAllItems().size();
		
		return count;
	}
	
	private static Set<CyNetwork> getNetworks(final Collection<SubNetworkPanel> items) {
		final Set<CyNetwork> list = new LinkedHashSet<>();
		
		for (final SubNetworkPanel snp : items)
			list.add(snp.getModel().getNetwork());
		
		return list;
	}
	
	protected void onMousePressedItem(final MouseEvent e, final AbstractNetworkPanel<?> item) {
		item.requestFocusInWindow();
		
		if (!e.isPopupTrigger() && SwingUtilities.isLeftMouseButton(e)) {
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
	
	class RootNetworkListPanel extends JPanel implements Scrollable {
		
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
			for (final RootNetworkPanel rnp : getAllItems()) {
				boolean currentRoot = false;
				
				for (final SubNetworkPanel snp : rnp.getAllItems()) {
					final boolean current = snp.getModel().getNetwork().equals(currentNetwork);
					snp.getModel().setCurrent(current);
					
					if (current)
						currentRoot = true;
				}
				
				rnp.getModel().setCurrent(currentRoot);
				rnp.update();
			}
			
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
				final RootNetworkPanel rootNetworkPanel = new RootNetworkPanel(model,
						isShowNetworkProvenanceHierarchy(), serviceRegistrar);
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
	
	private void maybeShowViewPopup(final SubNetworkPanel item) {
		final CySubNetwork network = item.getModel().getNetwork();
		
		if (viewDialog != null) {
			if (viewDialog.getNetwork().equals(network)) // Clicking the same item--will probably never happen
				return;
		
			viewDialog.dispose();
		}
		
		if (item.getModel().getViewCount() > 0) {
			final Window windowAncestor = SwingUtilities.getWindowAncestor(item);
			final CyNetworkView currentView = serviceRegistrar.getService(CyApplicationManager.class)
					.getCurrentNetworkView();
			
			viewDialog = new NetworkViewPreviewDialog(network, currentView, windowAncestor, serviceRegistrar);
			
			viewDialog.addWindowFocusListener(new WindowFocusListener() {
				@Override
				public void windowLostFocus(WindowEvent e) {
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
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
						viewDialog.dispose();
				}
			});
			viewDialog.addPropertyChangeListener("currentNetworkView", (PropertyChangeEvent evt) -> {
				// TODO Move to NetworkSelectionMediator
				serviceRegistrar.getService(CyApplicationManager.class)
						.setCurrentNetworkView((CyNetworkView) evt.getNewValue());
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
