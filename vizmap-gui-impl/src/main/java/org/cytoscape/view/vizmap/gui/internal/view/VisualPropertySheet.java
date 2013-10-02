package org.cytoscape.view.vizmap.gui.internal.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.theme.ThemeManager;
import org.cytoscape.view.vizmap.gui.internal.theme.ThemeManager.CyFont;
import org.cytoscape.view.vizmap.gui.util.PropertySheetUtil;

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
	
	private final ThemeManager themeMgr;
	
	private final TreeSet<VisualPropertySheetItem<?>> items;
	private final Map<VisualProperty<?>, VisualPropertySheetItem<?>> vpItemMap;
	private final Map<VisualPropertyDependency<?>, VisualPropertySheetItem<?>> depItemMap;
	private final Map<VisualPropertySheetItem<?>, JCheckBoxMenuItem> menuItemMap;
	
	private VisualPropertySheetItem<?> selectionHead;
	private VisualPropertySheetItem<?> selectionTail;
	private boolean doNotUpdateCollapseExpandButtons;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VisualPropertySheet(final VisualPropertySheetModel model, final ThemeManager themeMgr) {
		if (model == null)
			throw new IllegalArgumentException("'model' must not be null");
		if (themeMgr == null)
			throw new IllegalArgumentException("'themeMgr' must not be null");
		
		this.model = model;
		this.themeMgr = themeMgr;
		
		items = new TreeSet<VisualPropertySheetItem<?>>();
		vpItemMap = new HashMap<VisualProperty<?>, VisualPropertySheetItem<?>>();
		depItemMap = new HashMap<VisualPropertyDependency<?>, VisualPropertySheetItem<?>>();
		menuItemMap = new HashMap<VisualPropertySheetItem<?>, JCheckBoxMenuItem>();
		
		init();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public VisualPropertySheetModel getModel() {
		return model;
	}
	
	public void setItems(final Set<VisualPropertySheetItem<?>> newItems) {
		// Remove current items
		vpItemMap.clear();
		depItemMap.clear();
		items.clear();
		
		if (newItems != null) {
			items.addAll(newItems);
			
			// Create the internal panel that contains the visual property editors
			final JPanel p = new JPanel(new GridBagLayout());
			final GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.gridx = 0;
			c.weightx = 1;
			
			// Add the visual property editors to the internal panel
			int y = 0;
			int minWidth = 120;
			
			for (final VisualPropertySheetItem<?> i : items) {
				c.gridy = y++;
				p.add(i, c);
				
				// Save it for future use
				if (i.getModel().getVisualPropertyDependency() == null)
					vpItemMap.put(i.getModel().getVisualProperty(), i);
				else
					depItemMap.put(i.getModel().getVisualPropertyDependency(), i);
				
				// Add listeners
				i.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(final MouseEvent e) {
						onMouseClickedItem(e, i);
					}
				});
				
				if (i.getModel().isVisualMappingAllowed()) {
					i.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentShown(final ComponentEvent e) {
							updateCollapseExpandButtons();
						}
						@Override
						public void componentHidden(final ComponentEvent e) {
							if (selectionHead == i) selectionHead = null;
							if (selectionTail == i) selectionTail = null;
							updateCollapseExpandButtons();
						}
					});
					i.addPropertyChangeListener("enabled", new PropertyChangeListener() {
						@Override
						public void propertyChange(final PropertyChangeEvent e) {
							updateCollapseExpandButtons();
						}
					});
					i.addPropertyChangeListener("expanded", new PropertyChangeListener() {
						@Override
						public void propertyChange(final PropertyChangeEvent e) {
							updateCollapseExpandButtons();
						}
					});
					i.getPropSheetPnl().getTable().addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(final MouseEvent e) {
							onMouseClickedItem(e, i);
						}
					});
				}
				
				minWidth = Math.max(minWidth, i.getPreferredSize().width);
			}
			
			// Add an empty panel to fill the vertical gap
			final JPanel fillPnl = new JPanel();
			fillPnl.setBackground(VisualPropertySheetItem.BG_COLOR);
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 1;
			p.add(fillPnl, c);
			
			fillPnl.addMouseListener(new MouseAdapter() {
				@Override
				@SuppressWarnings("unchecked")
				public void mouseClicked(final MouseEvent e) {
					if (!e.isShiftDown() || e.isControlDown()) // Deselect all items
						setSelectedItems(Collections.EMPTY_SET);
				}
			});
			
			getVpListScr().setViewportView(p);
			
			minWidth = Math.min((minWidth += 10), 400);
			getVpListScr().setMinimumSize(new Dimension(minWidth, getVpListScr().getMinimumSize().height));
			setMinimumSize(new Dimension(minWidth, getMinimumSize().height));
			setPreferredSize(new Dimension(minWidth, getPreferredSize().height));
			
			if (getParent() != null) {
				minWidth = Math.max(minWidth + 8, getParent().getMinimumSize().width);
				getParent().setMinimumSize(new Dimension(minWidth, getParent().getMinimumSize().height));
			}
		}
		
		updateCollapseExpandButtons();
		createMenuItems();
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<VisualPropertySheetItem<?>> getItems() {
		return (SortedSet<VisualPropertySheetItem<?>>) items.clone();
	}
	
	@SuppressWarnings("unchecked")
	public <T> VisualPropertySheetItem<T> getItem(final VisualProperty<T> vp) {
		return (VisualPropertySheetItem<T>) vpItemMap.get(vp);
	}
	
	@SuppressWarnings("unchecked")
	public <T> VisualPropertySheetItem<T> getItem(final VisualPropertyDependency<T> dep) {
		return (VisualPropertySheetItem<T>) depItemMap.get(dep);
	}
	
	public synchronized Set<VisualPropertySheetItem<?>> getSelectedItems() {
		 final Set<VisualPropertySheetItem<?>> set = new HashSet<VisualPropertySheetItem<?>>();
		 
		 for (final VisualPropertySheetItem<?> i : items) {
			 if (i.isSelected())
				 set.add(i);
		 }
		 
		 return set;
	}
	
	public synchronized void setSelectedItems(final Set<VisualPropertySheetItem<?>> selectedItems) {
		for (final VisualPropertySheetItem<?> i : items) {
			 i.setSelected(selectedItems != null && i.isVisible() && selectedItems.contains(i));
			 
			 if (!i.isSelected()) {
				 if (i == selectionHead) selectionHead = null;
				 if (i == selectionTail) selectionTail = null;
			 }
		}
	}
	
	public void setVisible(final VisualPropertySheetItem<?> item, final boolean visible) {
		if (!visible)
			item.setSelected(false);
		
		item.setVisible(visible);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		final GroupLayout layout = new GroupLayout(this);
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
			toolBarPnl.setLayout(new BoxLayout(toolBarPnl, BoxLayout.X_AXIS));
			toolBarPnl.add(getVpsBtn());
			toolBarPnl.add(Box.createHorizontalGlue());
			
			if (model.getTargetDataType() != CyNetwork.class) {
				toolBarPnl.add(getExpandAllBtn());
				toolBarPnl.add(getCollapseAllBtn());
			}
		}
		
		return toolBarPnl;
	}
	
	private JPanel getVpListHeaderPnl() {
		if (vpListHeaderPnl == null) {
			vpListHeaderPnl = new JPanel();
			vpListHeaderPnl.setBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			vpListHeaderPnl.setLayout(new BoxLayout(vpListHeaderPnl, BoxLayout.X_AXIS));
			
			vpListHeaderPnl.add(Box.createRigidArea(new Dimension(2, 12)));
			
			final JLabel defLbl = new HeaderLabel("Def.");
			defLbl.setToolTipText("Default Value");
			vpListHeaderPnl.add(defLbl);
			
			if (model.getTargetDataType() != CyNetwork.class) {
				final JLabel mapLbl = new HeaderLabel("Map.");
				mapLbl.setToolTipText("Mapping");
				vpListHeaderPnl.add(mapLbl);
			}
			
			final JLabel bypassLbl = new HeaderLabel("Byp.");
			bypassLbl.setToolTipText("Bypass");
			vpListHeaderPnl.add(bypassLbl);
			
			vpListHeaderPnl.add(Box.createHorizontalGlue());
		}
		
		return vpListHeaderPnl;
	}
	
	private JScrollPane getVpListScr() {
		if (vpListScr == null) {
			vpListScr = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// Make redrawing the icons less expensive when scrolling
			vpListScr.getVerticalScrollBar().setUnitIncrement(8);
			// Try to fit sheet items to viewport's width when the scroll bar becomes visible
			vpListScr.getViewport().addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					for (final VisualPropertySheetItem<?> item : getItems())
						item.fitToWidth(vpListScr.getViewport().getWidth());
				}
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
		}
		
		return vpsBtn;
	}

	protected JPopupMenu getVpsMenu() {
		if (vpsMenu == null) {
			vpsMenu = new JPopupMenu();
			vpsMenu.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					updateMenuItems();
				}
				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}
				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}
			});
		}
		
		return vpsMenu;
	}

	protected JButton getExpandAllBtn() {
		if (expandAllBtn == null) {
			expandAllBtn = new JButton("\uF103"); // icon-double-angle-down
			expandAllBtn.setToolTipText("Expand all mappings");
			expandAllBtn.setBorderPainted(false);
			expandAllBtn.setContentAreaFilled(false);
			expandAllBtn.setOpaque(false);
			expandAllBtn.setFocusPainted(false);
			expandAllBtn.setFont(themeMgr.getFont(CyFont.FONTAWESOME_FONT).deriveFont(17.0f));
			expandAllBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			
			expandAllBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					expandAllMappings();
				}
			});
		}
		
		return expandAllBtn;
	}
	
	protected JButton getCollapseAllBtn() {
		if (collapseAllBtn == null) {
			collapseAllBtn = new JButton("\uF102"); // icon-double-angle-up
			collapseAllBtn.setToolTipText("Collapse all mappings");
			collapseAllBtn.setBorderPainted(false);
			collapseAllBtn.setContentAreaFilled(false);
			collapseAllBtn.setOpaque(false);
			collapseAllBtn.setFocusPainted(false);
			collapseAllBtn.setFont(themeMgr.getFont(CyFont.FONTAWESOME_FONT).deriveFont(17.0f));
			collapseAllBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			
			collapseAllBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					collapseAllMappings();
				}
			});
		}
		
		return collapseAllBtn;
	}
	
	private void createMenuItems() {
		final JPopupMenu rootMenu = getVpsMenu();
		
		// Remove previous menu items
		menuItemMap.clear();
		final int length = rootMenu.getSubElements() != null ? rootMenu.getSubElements().length : 0;
		
		for (int i = 0; i < length; i++)
			rootMenu.remove(i);
		
		// Add new menu items
		final VisualLexicon lexicon = model.getVisualLexicon();
		final VisualProperty<?> rootVp = model.getRootVisualProperty();
		final VisualLexiconNode rootNode = lexicon.getVisualLexiconNode(rootVp);
		
		// Menu Items for showing/hiding each VP Sheet Item
		// ------------------------------------------------
		// -- Visual Properties --
		final Queue<VisualLexiconNode> queue = new PriorityQueue<VisualLexiconNode>(50,
				new Comparator<VisualLexiconNode>() {
					@Override
					public int compare(final VisualLexiconNode n1, final VisualLexiconNode n2) {
						final Collator collator = Collator.getInstance(Locale.getDefault());
						collator.setStrength(Collator.PRIMARY);
						
						return collator.compare(
								VisualPropertySheetItemModel.createTitle(n1.getVisualProperty()),
								VisualPropertySheetItemModel.createTitle(n2.getVisualProperty()));
					}
				});
		queue.addAll(rootNode.getChildren());
		
		final Map<VisualLexiconNode, JComponent> menuMap = new HashMap<VisualLexiconNode, JComponent>();
		menuMap.put(rootNode, rootMenu);

		final VisualStyle style = model.getVisualStyle();
		final Set<VisualProperty<?>> disabledProps = new HashSet<VisualProperty<?>>();
//		final Set<VisualPropertyDependency<?>> depSet = style.getAllVisualPropertyDependencies();
		
//		for (final VisualPropertyDependency<?> dep : depSet) {
//			// To do the same as in Cytoscape v2.8, we only care about these dependencies
//			if (!dep.getIdString().equals("arrowColorMatchesEdge") && !dep.getIdString().equals("nodeSizeLocked"))
//				continue; // TODO: revisit these requirements and remove this workaround.
			
			// In general, the user should not be able to lock the child properties of an enabled dependency.
//			if (dep.isDependencyEnabled())
//				disabledProps.addAll(dep.getVisualProperties());
//			else
//				disabledProps.add(dep.getParentVisualProperty());
//		}
		
		final Set<VisualLexiconNode> nextNodes = new HashSet<VisualLexiconNode>();

		while (!queue.isEmpty()) {
			final VisualLexiconNode curNode = queue.poll();
			final VisualProperty<?> vp = curNode.getVisualProperty();
			
			if (vp.getTargetDataType() == model.getTargetDataType()) {
				final Collection<VisualLexiconNode> children = curNode.getChildren();
				nextNodes.addAll(children);
				
				if (PropertySheetUtil.isCompatible(vp)) {
					VisualLexiconNode parentNode = curNode.getParent();
					boolean leaf = children.isEmpty();
					
					final VisualPropertySheetItem<?> vpSheetItem = getItem(vp);
					final String label = vpSheetItem != null ? 
							vpSheetItem.getModel().getTitle() : VisualPropertySheetItemModel.createTitle(vp);
					
					if (!leaf) {
						// Non-leaf visual property...
						final JMenuItem nonLeafMenu = new JMenu(label);
						menuMap.put(curNode, nonLeafMenu);
						menuMap.get(parentNode).add(nonLeafMenu);
						// So this VP can also be added as a child of itself
						parentNode = curNode;
					}
					
					if (vpSheetItem != null) {
						final JCheckBoxMenuItem mi = new JCheckBoxMenuItem(label, vpSheetItem.isVisible());
						mi.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(final ActionEvent e) {
								// Show/hide the Visual Property Sheet Item
								setVisible(vpSheetItem, !vpSheetItem.isVisible());
							}
						});
					
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
		final TreeSet<VisualPropertyDependency<?>> depTreeSet = new TreeSet<VisualPropertyDependency<?>>(
				new Comparator<VisualPropertyDependency<?>>() {
					@Override
					public int compare(final VisualPropertyDependency<?> d1, final VisualPropertyDependency<?> d2) {
						final Collator collator = Collator.getInstance(Locale.getDefault());
						collator.setStrength(Collator.PRIMARY);
						
						return collator.compare(d1.getDisplayName(), d2.getDisplayName());
					}
				});
		depTreeSet.addAll(style.getAllVisualPropertyDependencies());
		
		for (final VisualPropertyDependency<?> dep : depTreeSet) {
			final VisualPropertySheetItem<?> vpSheetItem = getItem(dep);
			
			if (vpSheetItem != null) {
				final JCheckBoxMenuItem mi = new JCheckBoxMenuItem(dep.getDisplayName(), vpSheetItem.isVisible());
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						// Show/hide the Visual Property Sheet Item
						setVisible(vpSheetItem, !vpSheetItem.isVisible());
					}
				});
			
				final VisualLexiconNode parentNode = lexicon.getVisualLexiconNode(dep.getParentVisualProperty());
				JComponent parentMenu = menuMap.get(parentNode);
				
				if (parentMenu == null) // just add it directly to the popup menu
					parentMenu = rootMenu; 
				
				parentMenu.add(mi);
				menuItemMap.put(vpSheetItem, mi);
			}
		}
		
		// Other Menu Items
		// ------------------------------------------------
		if (menuMap.size() > 1) {
			rootMenu.add(new JSeparator());
			
			{
				final JMenuItem mi = new JMenuItem("Show All");
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						for (final Entry<VisualPropertySheetItem<?>, JCheckBoxMenuItem> entry : menuItemMap.entrySet()) {
							setVisible(entry.getKey(), true);
						}
					}
				});
				rootMenu.add(mi);
			}
			{
				final JMenuItem mi = new JMenuItem("Hide All");
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						for (final Entry<VisualPropertySheetItem<?>, JCheckBoxMenuItem> entry : menuItemMap.entrySet()) {
							setVisible(entry.getKey(), false);
						}
					}
				});
				rootMenu.add(mi);
			}
			
			getVpsBtn().setEnabled(true);
		} else {
			getVpsBtn().setEnabled(false);
		}
	}
	
	private void updateMenuItems() {
		// Update the selected state of each menu item
		for (final Entry<VisualPropertySheetItem<?>, JCheckBoxMenuItem> entry : menuItemMap.entrySet()) {
			entry.getValue().setSelected(entry.getKey().isVisible());
		}
	}
	
	private void updateCollapseExpandButtons() {
		if (doNotUpdateCollapseExpandButtons || model.getTargetDataType() == CyNetwork.class)
			return;
		
		boolean enableCollapse = false;
		boolean enableExpand = false;
		
		for (final VisualPropertySheetItem<?> item : items) {
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
		
		for (final VisualPropertySheetItem<?> item : items)
			item.collapse();
		
		doNotUpdateCollapseExpandButtons = false;
		updateCollapseExpandButtons();
	}

	private void expandAllMappings() {
		doNotUpdateCollapseExpandButtons = true;
		
		for (final VisualPropertySheetItem<?> item : items) {
			// Expand only the ones that have a mapping
			if (item.isEnabled() && item.getModel().getVisualMappingFunction() != null)
				item.expand();
		}
		
		doNotUpdateCollapseExpandButtons = false;
		updateCollapseExpandButtons();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void onMouseClickedItem(final MouseEvent e, final VisualPropertySheetItem<?> item) {
		final String os = System.getProperty("os.name").toLowerCase();
		final boolean isMac = os.indexOf("mac") >= 0;
		
		if (SwingUtilities.isRightMouseButton(e) && !(isMac && SwingUtilities.isLeftMouseButton(e) && e.isMetaDown())) {
			// RIGHT-CLICK...
			// Note: SwingUtilities.isRightMouseButton also interpret left-click + COMMAND (meta) key down on MacOS
			//       as a right-click, which is wrong, so let's ignore that action!
			selectionHead = item;
		} else if (SwingUtilities.isLeftMouseButton(e)) {
			// LEFT-CLICK...
			if ((isMac && e.isMetaDown()) || (!isMac && e.isControlDown())) {
				// COMMAND button down on MacOS or CONTROL button down on another OS.
				// Toggle this item's selection state
				item.setSelected(!item.isSelected());
				// Find new selection range head
				selectionHead = item.isSelected() ? item : findNextSelectionHead(selectionHead);
			} else {
				if (e.isShiftDown()) {
					if (selectionHead != null && selectionHead.isVisible() && selectionHead.isSelected()
							&& selectionHead != item) {
						// First deselect previous range, if there is a tail
						if (selectionTail != null)
							changeRangeSelection(selectionHead, selectionTail, false);
						// Now select the new range
						changeRangeSelection(selectionHead, (selectionTail = item), true);
					} else if (!item.isSelected()) {
						item.setSelected(true);
					}
				} else {
					setSelectedItems((Set) (Collections.singleton(item)));
				}
				
				if (getSelectedItems().size() == 1)
					selectionHead = item;
			}
		}
	}
	
	private void changeRangeSelection(final VisualPropertySheetItem<?> item1, final VisualPropertySheetItem<?> item2,
			final boolean selected) {
		final NavigableSet<VisualPropertySheetItem<?>> subSet;
		
		if (item1.compareTo(item2) <= 0)
			subSet = items.subSet(item1, false, item2, true);
		else
			subSet = items.subSet(item2, true, item1, false);
				
		for (final VisualPropertySheetItem<?> nextItem : subSet) {
			if (nextItem.isVisible())
				nextItem.setSelected(selected);
		}
	}
	
	private VisualPropertySheetItem<?> findNextSelectionHead(final VisualPropertySheetItem<?> fromItem) {
		VisualPropertySheetItem<?> head = null;
		
		if (fromItem != null) {
			NavigableSet<VisualPropertySheetItem<?>> subSet = items.tailSet(fromItem, false);
			
			// Try with the tail subset first
			for (final VisualPropertySheetItem<?> nextItem : subSet) {
				if (nextItem.isVisible() && nextItem.isSelected()) {
					head = nextItem;
					break;
				}
			}
			
			if (head == null) {
				// Try with the head subset
				subSet = items.headSet(fromItem, false);
				final Iterator<VisualPropertySheetItem<?>> iterator = subSet.descendingIterator();
				
				while (iterator.hasNext()) {
					final VisualPropertySheetItem<?> nextItem = iterator.next();
					
					if (nextItem.isVisible() && nextItem.isSelected()) {
						head = nextItem;
						break;
					}
				}
			}
		}
		
		return head;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private static class HeaderLabel extends JLabel {
		
		final static Font FONT = new Font("Arial", Font.BOLD, 10);
		final static Color FG_COLOR = Color.DARK_GRAY;
		
		HeaderLabel(final String text) {
			super(text);
			setFont(FONT);
			setForeground(FG_COLOR);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(BOTTOM);
			
			final Dimension d = new Dimension(VisualPropertySheetItem.VPButtonUI.getPreferredWidth(), 18);
			setMinimumSize(d);
			setPreferredSize(d);
			setMaximumSize(d);
		}
	}
}
