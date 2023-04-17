package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.util.PropertySheetUtil;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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
public class VisualPropertySheet extends JPanel{

	private JPanel toolBarPnl;
	private JPanel vpListHeaderPnl;
	private JScrollPane vpListScr;
	private DropDownMenuButton vpsBtn;
	private JPopupMenu vpsMenu;
	private JButton expandAllBtn;
	private JButton collapseAllBtn;
	
	private final VisualPropertySheetModel model;
	
	private final TreeSet<VisualPropertySheetItem<?>> items;
	private final Map<VisualProperty<?>, VisualPropertySheetItem<?>> vpItemMap;
	private final Map<String/*dependency ID*/, VisualPropertySheetItem<?>> depItemMap;
	private final Map<VisualPropertySheetItem<?>, JCheckBoxMenuItem> menuItemMap;
	
	private DefaultListSelectionModel selectionModel;
	
	private boolean doNotUpdateCollapseExpandButtons;
	
	private final ServicesUtil servicesUtil;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VisualPropertySheet(VisualPropertySheetModel model, ServicesUtil servicesUtil) {
		if (model == null)
			throw new IllegalArgumentException("'model' must not be null");
		if (servicesUtil == null)
			throw new IllegalArgumentException("'servicesUtil' must not be null");
		
		this.model = model;
		this.servicesUtil = servicesUtil;
		
		items = new TreeSet<>();
		vpItemMap = new HashMap<>();
		depItemMap = new HashMap<>();
		menuItemMap = new HashMap<>();
		
		selectionModel = new DefaultListSelectionModel();
		// Here is where we listen to the changed indexes in order to select/deselect the the actual items
		selectionModel.addListSelectionListener(evt -> {
			if (!evt.getValueIsAdjusting()) {
				var allItems = getAllItems(false);
				int first = evt.getFirstIndex();
				int last = evt.getLastIndex();
				
				for (int i = first; i <= last; i++) {
					if (i >= allItems.size())
						break;
					
					var p = allItems.get(i);
					boolean selected = selectionModel.isSelectedIndex(i);
					
					if (p.isSelected() != selected)
						p.setSelected(selected);
				}
			}
		});
		
		init();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public VisualPropertySheetModel getModel() {
		return model;
	}
	
	public void setItems(Set<VisualPropertySheetItem<?>> newItems) {
		// Remove current items
		vpItemMap.clear();
		depItemMap.clear();
		items.clear();
		
		if (newItems != null) {
			items.addAll(newItems);
			
			// Create the internal panel that contains the visual property editors
			var p = new JPanel(new GridBagLayout());
			var c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.gridx = 0;
			c.weightx = 1;
			
			// Add the visual property editors to the internal panel
			int y = 0;
			int minWidth = 120;
			
			for (var i : items) {
				c.gridy = y++;
				p.add(i, c);
				
				// Save it for future use
				if (i.getModel().getVisualPropertyDependency() == null)
					vpItemMap.put(i.getModel().getVisualProperty(), i);
				else
					depItemMap.put(i.getModel().getVisualPropertyDependency().getIdString(), i);
				
				// Add listeners
				i.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						onMousePressedItem(e, i);
					}
				});
				
				if (i.getModel().isVisualMappingAllowed()) {
					i.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentShown(ComponentEvent e) {
							updateCollapseExpandButtons();
						}
						@Override
						public void componentHidden(ComponentEvent e) {
							updateCollapseExpandButtons();
						}
					});
					i.addPropertyChangeListener("enabled", evt -> {
						updateCollapseExpandButtons();
					});
					i.addPropertyChangeListener("expanded", evt -> {
						updateCollapseExpandButtons();
					});
					i.getPropSheetPnl().getTable().addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							if (!i.getPropSheetPnl().getTable().isEditing())
								onMousePressedItem(e, i);
						}
					});
				}
				
				minWidth = Math.max(minWidth, i.getPreferredSize().width);
			}
			
			// Add an empty panel to fill the vertical gap
			var fillPnl = new JPanel();
			fillPnl.setBackground(VisualPropertySheetItem.getBackgroundColor());
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 1;
			p.add(fillPnl, c);
			
			fillPnl.addMouseListener(new MouseAdapter() {
				@Override
				@SuppressWarnings("unchecked")
				public void mouseClicked(MouseEvent e) {
					if (!e.isShiftDown() || e.isControlDown()) // Deselect all items
						setSelectedItems(Collections.EMPTY_SET);
				}
			});
			
			getVpListScr().setViewportView(p);
			
			minWidth = Math.min((minWidth += 10), 400);
			getVpListScr().setMinimumSize(new Dimension(minWidth, 140));
			
			if (getParent() != null) {
				minWidth = Math.max(minWidth + 8, getParent().getMinimumSize().width);
				getParent().setMinimumSize(new Dimension(minWidth, getParent().getMinimumSize().height));
			}
		}
		
		updateCollapseExpandButtons();
		createMenuItems();
	}
	
	/**
	 * @return All {@link VisualPropertySheetItem}s, including the hidden ones.
	 */
	public List<VisualPropertySheetItem<?>> getAllItems() {
		return getAllItems(true);
	}
	
	/**
	 * @param includeInvisible if true, it includes the hidden items in the returned list
	 * @return
	 */
	public List<VisualPropertySheetItem<?>> getAllItems(boolean includeInvisible) {
		var list = new ArrayList<VisualPropertySheetItem<?>>();
		
		for (var i : items) {
			if (includeInvisible || i.isVisible())
				list.add(i);
		}
		
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public <T> VisualPropertySheetItem<T> getItem(VisualProperty<T> vp) {
		return (VisualPropertySheetItem<T>) vpItemMap.get(vp);
	}
	
	@SuppressWarnings("unchecked")
	public <T> VisualPropertySheetItem<T> getItem(VisualPropertyDependency<T> dep) {
		return (VisualPropertySheetItem<T>) depItemMap.get(dep.getIdString());
	}
	
	/**
	 * @return All selected {@link VisualPropertySheetItem}s that are also visible.
	 */
	public synchronized List<VisualPropertySheetItem<?>> getSelectedItems() {
		var list = new ArrayList<VisualPropertySheetItem<?>>();
		 
		 for (var i : items) {
			 if (i.isVisible() && i.isSelected())
				 list.add(i);
		 }
		 
		 return list;
	}
	
	public synchronized void setSelectedItems(Collection<VisualPropertySheetItem<?>> selectedItems) {
		selectionModel.setValueIsAdjusting(true);
		
		try {
			selectionModel.clearSelection();
			var allItems = getAllItems(false);
			int maxIdx = -1;
			
			for (var i : selectedItems) {
				int idx = allItems.indexOf(i);
				
				if (idx >= 0) {
					selectionModel.addSelectionInterval(idx, idx);
					maxIdx = Math.max(idx, maxIdx);
				}
			}
			
			selectionModel.setAnchorSelectionIndex(maxIdx);
			selectionModel.moveLeadSelectionIndex(maxIdx);
		} finally {
			selectionModel.setValueIsAdjusting(false);
		}
	}
	
	public void setVisibleItems(Collection<VisualPropertySheetItem<?>> visibleItems) {
		selectionModel.setValueIsAdjusting(true);
		
		try {
			// Save the current selection for later
			var selectedItems = getSelectedItems();
			// Make sure everything is unselected, because they may be hidden in the next steps
			deselectAll();
			
			var allItems = getAllItems();
			
			for (var i : allItems) {
				i.setVisible(visibleItems.contains(i));
				
				if (!i.isVisible()) // remove the invisible item from the items that will be selected again later
					selectedItems.remove(i);
			}
			
			// Restore the previous selection
			setSelectedItems(selectedItems);
			
			updateCollapseExpandButtons();
		} finally {
			selectionModel.setValueIsAdjusting(false);
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		setOpaque(!isAquaLAF());
		var layout = new GroupLayout(this);
		setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getToolBarPnl())
				.addComponent(getVpListHeaderPnl())
				.addComponent(getVpListScr()));
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(getToolBarPnl())
				.addComponent(getVpListHeaderPnl())
				.addComponent(getVpListScr()));
	}
	
	private JPanel getToolBarPnl() {
		if (toolBarPnl == null) {
			toolBarPnl = new JPanel();
			toolBarPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			toolBarPnl.setLayout(new BoxLayout(toolBarPnl, BoxLayout.X_AXIS));
			toolBarPnl.add(getVpsBtn());
			toolBarPnl.add(Box.createHorizontalGlue());
			
			if (model.getLexiconType() != CyNetwork.class) {
				toolBarPnl.add(getExpandAllBtn());
				toolBarPnl.add(getCollapseAllBtn());
			}
		}
		
		return toolBarPnl;
	}
	
	private JPanel getVpListHeaderPnl() {
		if (vpListHeaderPnl == null) {
			vpListHeaderPnl = new JPanel();
			vpListHeaderPnl.setLayout(new BoxLayout(vpListHeaderPnl, BoxLayout.X_AXIS));
			
			vpListHeaderPnl.add(Box.createRigidArea(new Dimension(2, 12)));
			
			var defLbl = new HeaderLabel("Def.");
			defLbl.setToolTipText("Default Value");
			vpListHeaderPnl.add(defLbl);
			
			if (model.getLexiconType() != CyNetwork.class) {
				var mapLbl = new HeaderLabel("Map.");
				mapLbl.setToolTipText("Mapping");
				vpListHeaderPnl.add(mapLbl);
			}
			
			if (model.getLexiconType() != CyColumn.class) {
				var bypassLbl = new HeaderLabel("Byp.");
				bypassLbl.setToolTipText("Bypass");
				vpListHeaderPnl.add(bypassLbl);
			}
			
			vpListHeaderPnl.add(Box.createHorizontalGlue());
		}
		
		return vpListHeaderPnl;
	}
	
	private JScrollPane getVpListScr() {
		if (vpListScr == null) {
			vpListScr = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// Make redrawing the icons less expensive when scrolling
			vpListScr.getVerticalScrollBar().setUnitIncrement(8);
			// Try to fit sheet items to viewport's width when the scroll bar becomes visible
			vpListScr.getViewport().addChangeListener(evt -> {
				for (var item : getAllItems())
					item.fitToWidth(vpListScr.getViewport().getWidth());
			});
		}
		
		return vpListScr;
	}
	
	private DropDownMenuButton getVpsBtn() {
		if (vpsBtn == null) {
			vpsBtn = new DropDownMenuButton(getVpsMenu());
			vpsBtn.setText("Properties");
			vpsBtn.setToolTipText("Show/Hide Properties...");
			vpsBtn.setHorizontalAlignment(DropDownMenuButton.LEFT);
			vpsBtn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			vpsBtn.setContentAreaFilled(false);
		}
		
		return vpsBtn;
	}

	protected JPopupMenu getVpsMenu() {
		if (vpsMenu == null) {
			vpsMenu = new JPopupMenu();
			vpsMenu.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					updateMenuItems();
				}
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					// Nothing here...
				}
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					// Nothing here...
				}
			});
		}
		
		return vpsMenu;
	}

	protected JButton getExpandAllBtn() {
		if (expandAllBtn == null) {
			var iconManager = servicesUtil.get(IconManager.class);
			
			expandAllBtn = new JButton(IconManager.ICON_ANGLE_DOUBLE_DOWN);
			expandAllBtn.setToolTipText("Expand all mappings");
			expandAllBtn.setBorderPainted(false);
			expandAllBtn.setContentAreaFilled(false);
			expandAllBtn.setFocusPainted(false);
			expandAllBtn.setFont(iconManager.getIconFont(17.0f));
			expandAllBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			
			expandAllBtn.addActionListener(evt -> expandAllMappings());
		}
		
		return expandAllBtn;
	}
	
	protected JButton getCollapseAllBtn() {
		if (collapseAllBtn == null) {
			var iconManager = servicesUtil.get(IconManager.class);
			
			collapseAllBtn = new JButton(IconManager.ICON_ANGLE_DOUBLE_UP);
			collapseAllBtn.setToolTipText("Collapse all mappings");
			collapseAllBtn.setBorderPainted(false);
			collapseAllBtn.setContentAreaFilled(false);
			collapseAllBtn.setFocusPainted(false);
			collapseAllBtn.setFont(iconManager.getIconFont(17.0f));
			collapseAllBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			
			collapseAllBtn.addActionListener(evt -> collapseAllMappings());
		}
		
		return collapseAllBtn;
	}
	
	private void createMenuItems() {
		var rootMenu = getVpsMenu();
		
		// Remove previous menu items
		menuItemMap.clear();
		int length = rootMenu.getSubElements() != null ? rootMenu.getSubElements().length : 0;
		
		for (int i = 0; i < length; i++)
			rootMenu.remove(i);
		
		// Add new menu items
		var lexicon = model.getVisualLexicon();
		
		if (lexicon == null)
			return;
		
		var rootVp = model.getRootVisualProperty();
		var rootNode = lexicon.getVisualLexiconNode(rootVp);
		var collator = Collator.getInstance(Locale.getDefault());
		
		// Menu Items for showing/hiding each VP Sheet Item
		// ------------------------------------------------
		// -- Visual Properties --
		var queue = new PriorityQueue<>(50,
				new Comparator<VisualLexiconNode>() {
					@Override
					public int compare(VisualLexiconNode n1, VisualLexiconNode n2) {
						collator.setStrength(Collator.PRIMARY);
						
						return collator.compare(
								VisualPropertySheetItemModel.createTitle(n1.getVisualProperty()),
								VisualPropertySheetItemModel.createTitle(n2.getVisualProperty()));
					}
				}
		);
		queue.addAll(rootNode.getChildren());
		
		var menuMap = new HashMap<VisualLexiconNode, JComponent>();
		menuMap.put(rootNode, rootMenu);

		var style = model.getVisualStyle();
		var disabledProps = new HashSet<VisualProperty<?>>();
//		var depSet = style.getAllVisualPropertyDependencies();
		
//		for (var dep : depSet) {
//			// To do the same as in Cytoscape v2.8, we only care about these dependencies
//			if (!dep.getIdString().equals("arrowColorMatchesEdge") && !dep.getIdString().equals("nodeSizeLocked"))
//				continue; // TODO: revisit these requirements and remove this workaround.
			
			// In general, the user should not be able to lock the child properties of an enabled dependency.
//			if (dep.isDependencyEnabled())
//				disabledProps.addAll(dep.getVisualProperties());
//			else
//				disabledProps.add(dep.getParentVisualProperty());
//		}
		
		var nextNodes = new HashSet<VisualLexiconNode>();

		while (!queue.isEmpty()) {
			var curNode = queue.poll();
			var vp = curNode.getVisualProperty();
			
			if (vp.getTargetDataType() == model.getLexiconType()) {
				var children = curNode.getChildren();
				nextNodes.addAll(children);
				
				if (PropertySheetUtil.isCompatible(vp)) {
					var parentNode = curNode.getParent();
					boolean leaf = children.isEmpty();
					
					var vpSheetItem = getItem(vp);
					var label = vpSheetItem != null ? 
							vpSheetItem.getModel().getTitle() : VisualPropertySheetItemModel.createTitle(vp);
					
					if (!leaf) {
						// Non-leaf visual property...
						var nonLeafMenu = new JMenu(label);
						menuMap.put(curNode, nonLeafMenu);
						menuMap.get(parentNode).add(nonLeafMenu);
						// So this VP can also be added as a child of itself
						parentNode = curNode;
					}
					
					if (vpSheetItem != null) {
						// Show/hide the Visual Property Sheet Item
						var mi = new JCheckBoxMenuItem(label, vpSheetItem.isVisible());
						mi.addActionListener(evt -> toggleVisibility(vpSheetItem));
					
						menuMap.get(parentNode).add(mi);
						menuItemMap.put(vpSheetItem, mi);
						
						if (parentNode == curNode)
							menuMap.get(parentNode).add(new JSeparator());
					
						// Should this visual property be disabled?
						if (disabledProps.contains(vp))
							mi.setEnabled(false);
					}
				}
			}

			queue.addAll(nextNodes);
			nextNodes.clear();
		}
		
		// -- Visual Property Dependencies --
		var depTreeSet =
				new TreeSet<>((VisualPropertyDependency<?> d1, VisualPropertyDependency<?> d2) -> {
					collator.setStrength(Collator.PRIMARY);
					
					return collator.compare(d1.getDisplayName(), d2.getDisplayName());
				}
		);
		depTreeSet.addAll(style.getAllVisualPropertyDependencies());
		
		for (var dep : depTreeSet) {
			var vpSheetItem = getItem(dep);
			
			if (vpSheetItem != null) {
				// Show/hide the Visual Property Sheet Item
				var mi = new JCheckBoxMenuItem(dep.getDisplayName(), vpSheetItem.isVisible());
				mi.addActionListener(evt -> toggleVisibility(vpSheetItem));
			
				var parentNode = lexicon.getVisualLexiconNode(dep.getParentVisualProperty());
				var parentMenu = menuMap.get(parentNode);
				
				if (parentMenu == null) // just add it directly to the popup menu
					parentMenu = rootMenu; 
				
				parentMenu.add(mi);
				menuItemMap.put(vpSheetItem, mi);
			}
		}
	}
	
	private void updateMenuItems() {
		// Update the selected state of each menu item
		for (var entry : menuItemMap.entrySet()) {
			entry.getValue().setSelected(entry.getKey().isVisible());
		}
	}
	
	private void updateCollapseExpandButtons() {
		if (doNotUpdateCollapseExpandButtons || model.getLexiconType() == CyNetwork.class)
			return;
		
		boolean enableCollapse = false;
		boolean enableExpand = false;
		
		for (var item : items) {
			if (item.isVisible() && item.isEnabled()) {
				if (item.isExpanded())
					enableCollapse = true;
				else if (!enableExpand && !item.isExpanded() && item.getModel().getVisualMappingFunction() != null)
					enableExpand = true;
			}
			
			if (enableExpand && enableCollapse)
				break;
		}
		
		getCollapseAllBtn().setEnabled(enableCollapse);
		getExpandAllBtn().setEnabled(enableExpand);
	}
	
	private void collapseAllMappings() {
		doNotUpdateCollapseExpandButtons = true;
		
		for (var item : items)
			item.collapse();
		
		doNotUpdateCollapseExpandButtons = false;
		updateCollapseExpandButtons();
	}

	private void expandAllMappings() {
		doNotUpdateCollapseExpandButtons = true;
		
		for (var item : items) {
			// Expand only the ones that have a mapping
			if (item.isEnabled() && item.getModel().getVisualMappingFunction() != null)
				item.expand();
		}
		
		doNotUpdateCollapseExpandButtons = false;
		updateCollapseExpandButtons();
	}
	
	void onMousePressedItem(MouseEvent e, VisualPropertySheetItem<?> item) {
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
			} else {
				// No SHIFT/CTRL pressed
				var allItems = getAllItems(false);
				setSelectedIndex(allItems.indexOf(item));
			}
		}
	}
	
	private void shiftSelectTo(int index) {
		int size = items.size();
		
		if (index < 0 || index >= size)
			return;
		
		int anchor = selectionModel.getAnchorSelectionIndex();
		int lead = selectionModel.getLeadSelectionIndex();
		
		selectionModel.setValueIsAdjusting(true);
		
		// 1. remove everything between anchor and focus (lead)
		if (anchor != lead && (anchor >= 0 || lead >= 0))
			selectionModel.removeSelectionInterval(Math.max(0, anchor), Math.max(0, lead));
		
		// 2. add everything between anchor and the new index, which  should also be made the new lead
		selectionModel.addSelectionInterval(Math.max(0, anchor), index);
		
		selectionModel.setValueIsAdjusting(false);
		
		// 3. Make sure the lead component is focused
		var allItems = getAllItems(false);
		allItems.get(index).requestFocusInWindow();
	}
	
	private void toggleSelection(VisualPropertySheetItem<?> item) {
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
	
	void selectAll() {
		if (items.size() >= 0)
			selectionModel.setSelectionInterval(0, items.size() - 1);
	}
	
	void deselectAll() {
		selectionModel.clearSelection();
		selectionModel.setAnchorSelectionIndex(-1);
		selectionModel.setLeadSelectionIndex(-1);
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
		if (index >= items.size())
			return;
		
		selectionModel.setSelectionInterval(index, index);
	}
	
	private void toggleVisibility(VisualPropertySheetItem<?> item) {
		if (item.isVisible()) {
			// This item will be hidden in the next step, so deselect it first, if necessary
			if (item.isSelected())
				toggleSelection(item);
		} else {
			// This item will be shown in the next step and we want it to be the only one selected,
			// so clear the selection first, before the indexes change
			// (after hiding an item, the selection model won't have access to it anymore,
			// as the selection model only handle visible items)
			deselectAll();
		}
			
		item.setVisible(!item.isVisible());
		
		if (item.isVisible()) {
			// Select this item
			setSelectedItems(Collections.singleton(item));
		} else {
			// Always select the items again to make sure the model's indexes are updated
			var selectedItems = getSelectedItems();
			setSelectedItems(selectedItems);
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private static class HeaderLabel extends JLabel {
		
		HeaderLabel(String text) {
			super(text);
			setFont(getFont().deriveFont(Font.BOLD).deriveFont(10.0f));
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(BOTTOM);
			
			var d = new Dimension(VisualPropertySheetItem.VPButtonUI.getPreferredWidth(), 18);
			setMinimumSize(d);
			setPreferredSize(d);
			setMaximumSize(d);
		}
	}
}
