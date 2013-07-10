package org.cytoscape.view.vizmap.gui.internal.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.view.vizmap.gui.util.PropertySheetUtil;

@SuppressWarnings("serial")
public class VisualPropertySheet extends JPanel{

	private JPanel toolBarPnl;
	private JPanel vpListHeaderPnl;
	private JScrollPane vpListScr;
	private DropDownMenuButton addVpsBtn;
	private JPopupMenu addVpsMenu;
	private JButton expandAllBtn;
	private JButton collapseAllBtn;
	
	private final VisualPropertySheetModel model;
	
	private final IconManager iconMgr;
	
	private final TreeSet<VisualPropertySheetItem<?>> items;
	private final Map<VisualProperty<?>, VisualPropertySheetItem<?>> vpItemMap;
	private final Map<VisualPropertyDependency<?>, VisualPropertySheetItem<?>> depItemMap;
	private final Map<VisualPropertySheetItem<?>, JCheckBoxMenuItem> menuItemMap;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VisualPropertySheet(final VisualPropertySheetModel model, final IconManager iconMgr) {
		if (model == null)
			throw new IllegalArgumentException("'model' must not be null");
		if (iconMgr == null)
			throw new IllegalArgumentException("'iconMgr' must not be null");
		
		this.model = model;
		this.iconMgr = iconMgr;
		
		items = new TreeSet<VisualPropertySheetItem<?>>(
				new Comparator<VisualPropertySheetItem<?>>() {
					@Override
					public int compare(final VisualPropertySheetItem<?> i1, final VisualPropertySheetItem<?> i2) {
						VisualPropertySheetItemModel<?> m1 = i1.getModel();
						VisualPropertySheetItemModel<?> m2 = i2.getModel();
						String title1 = m1.getTitle();
						String title2 = m2.getTitle();
						
						final VisualPropertyDependency<?> dep1 = m1.getVisualPropertyDependency();
						final VisualPropertyDependency<?> dep2 = m2.getVisualPropertyDependency();
						
						// Put dependencies in the end of the sorted list
						if (dep1 == null && dep2 != null)
							return -1;
						if (dep1 != null && dep2 == null)
							return 1;
						
						if (dep1 != null && dep2 != null) {
							title1 = dep1.getDisplayName();
							title2 = dep2.getDisplayName();
						}
						
						// Locale-specific sorting
						final Collator collator = Collator.getInstance(Locale.getDefault());
						collator.setStrength(Collator.PRIMARY);
						
						return collator.compare(title1, title2);
					}
				}
			);
		
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
						if (!SwingUtilities.isRightMouseButton(e)) {
							if (e.isShiftDown()) {
								// TODO Select range
								i.setSelected(!i.isSelected());
							} else if (e.isControlDown()) {
								// Add to selection
								i.setSelected(!i.isSelected());
							} else {
								// Select only this one
								setSelectedItems((Set) (Collections.singleton(i)));
							}
						}
					}
				});
			}
			
			// Add an empty panel to fill the vertical gap
			final JPanel fillPnl = new JPanel();
			fillPnl.setBackground(VisualPropertySheetItem.BG_COLOR);
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 1;
			p.add(fillPnl, c);
			
			getVpListScr().setViewportView(p);
			getVpListScr().repaint();
		}
		
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
		for (final VisualPropertySheetItem<?> i : items)
			 i.setSelected(selectedItems != null && selectedItems.contains(i));
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
			toolBarPnl.add(getAddVpsBtn());
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
			vpListHeaderPnl.setBackground(Color.DARK_GRAY);
			vpListHeaderPnl.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
			vpListHeaderPnl.setLayout(new BoxLayout(vpListHeaderPnl, BoxLayout.X_AXIS));
			
			vpListHeaderPnl.add(Box.createRigidArea(new Dimension(2, 12)));
			
			final JLabel defLbl = new HeaderLabel("Def.");
			defLbl.setToolTipText("Default Value");
			vpListHeaderPnl.add(defLbl);
			
			if (model.getTargetDataType() != CyNetwork.class) {
				final JLabel mapLbl = new HeaderLabel("Map.");
				mapLbl.setToolTipText("Visual Mapping");
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
		}
		
		return vpListScr;
	}
	
	private DropDownMenuButton getAddVpsBtn() {
		if (addVpsBtn == null) {
			addVpsBtn = new DropDownMenuButton(getAddVpsMenu());
			addVpsBtn.setText("Add Visual Properties");
			addVpsBtn.setHorizontalAlignment(DropDownMenuButton.LEFT);
			addVpsBtn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		}
		
		return addVpsBtn;
	}

	private JPopupMenu getAddVpsMenu() {
		if (addVpsMenu == null) {
			addVpsMenu = new JPopupMenu();
			addVpsMenu.addPopupMenuListener(new PopupMenuListener() {
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
		
		return addVpsMenu;
	}

	protected JButton getExpandAllBtn() {
		if (expandAllBtn == null) {
			expandAllBtn = new JButton(iconMgr.getIcon(IconManager.EXPAND_ALL_ICON));
			expandAllBtn.setToolTipText("Expand all visual mapping panels");
			expandAllBtn.setBorder(BorderFactory.createEmptyBorder());
		}
		
		return expandAllBtn;
	}
	
	protected JButton getCollapseAllBtn() {
		if (collapseAllBtn == null) {
			collapseAllBtn = new JButton(iconMgr.getIcon(IconManager.COLLAPSE_ALL_ICON));
			collapseAllBtn.setToolTipText("Collapse all visual mapping panels");
			collapseAllBtn.setBorder(BorderFactory.createEmptyBorder());
		}
		
		return collapseAllBtn;
	}
	
	private void createMenuItems() {
		final JPopupMenu rootMenu = getAddVpsMenu();
		
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
			
			final JMenuItem addAllMi = new JMenuItem("Add All");
			addAllMi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					for (final Entry<VisualPropertySheetItem<?>, JCheckBoxMenuItem> entry : menuItemMap.entrySet()) {
						setVisible(entry.getKey(), true);
					}
				}
			});
			rootMenu.add(addAllMi);
			
			final JMenuItem removeAllMi = new JMenuItem("Remove All");
			removeAllMi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					for (final Entry<VisualPropertySheetItem<?>, JCheckBoxMenuItem> entry : menuItemMap.entrySet()) {
						setVisible(entry.getKey(), false);
					}
				}
			});
			rootMenu.add(removeAllMi);
			
			rootMenu.add(new JSeparator());
			
			final JMenuItem makeDefMi = new JMenuItem("Make Default");
			makeDefMi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					// TODO
					JOptionPane.showMessageDialog(VisualPropertySheet.this, "Feature not implemented yet...");
				}
			});
			rootMenu.add(makeDefMi);
			
			getAddVpsBtn().setEnabled(true);
		} else {
			getAddVpsBtn().setEnabled(false);
		}
	}
	
	private void updateMenuItems() {
		// Update the selected state of each menu item
		for (final Entry<VisualPropertySheetItem<?>, JCheckBoxMenuItem> entry : menuItemMap.entrySet()) {
			entry.getValue().setSelected(entry.getKey().isVisible());
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private static class HeaderLabel extends JLabel {
		
		final static Font FONT = new Font("Arial", Font.BOLD, 10);
		final static Color FG_COLOR = Color.WHITE;
		
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
