package org.cytoscape.view.vizmap.gui.internal.bypass;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.util.PropertySheetUtil;

@Deprecated
final class BypassMenuBuilder {

	private static final String ROOT_MENU_LABEL = "Bypass Visual Style";
	private static final String RESET_ALL_MENU_LABEL = "Reset All";
	private static final String EDIT_BYPASS_MENU_LABEL = "Edit Bypass";
	private static final String CLEAR_MENU_LABEL = "Clear";
	
	// Try to set it at the bottom of context menu
	private static final float ROOT_GRAVITY = 1000000f;

	private static final Font ENABLED_FONT = new Font("Helvetica", Font.BOLD, 14);
	private static final Icon ENABLED_ICON = new ImageIcon(
			BypassMenuBuilder.class.getResource("/images/icons/CrystalClearIcons_Action-lock-silver-icon.png"));
	private static final Color ENABLED_COLOR = Color.RED;

	private final VisualLexiconNode root;
	private final EditorManager editorManager;
	private final VisualMappingManager vmm;
	private final Collection<VisualProperty<?>> vpSet;

	public BypassMenuBuilder(final VisualLexiconNode root, final EditorManager editorManager,
			final VisualMappingManager vmm) {
		this.root = root;
		this.editorManager = editorManager;
		this.vmm = vmm;
		this.vpSet = new HashSet<VisualProperty<?>>();
	}

	/**
	 * 
	 * @param netView
	 * @param view a View&lt;CyNode&gt;, View&lt;CyEdge&gt; or View&lt;CyNetwork&gt; object
	 * @return
	 */
	public CyMenuItem build(final CyNetworkView netView, final View<? extends CyIdentifiable> view) {
		final Class<? extends CyIdentifiable> targetClass = view.getModel().getClass();
		final Queue<VisualLexiconNode> queue = new PriorityQueue<VisualLexiconNode>(50,
				new VisualLexiconNodeComparator());
		final Map<VisualLexiconNode, JMenuItem> menuMap = new HashMap<VisualLexiconNode, JMenuItem>();

		final JMenu rootJMenu = new JMenu(ROOT_MENU_LABEL);

		final CyMenuItem rootMenu = new CyMenuItem(rootJMenu, ROOT_GRAVITY);
		queue.addAll(root.getChildren());
		menuMap.put(root, rootMenu.getMenuItem());

		// The dependencies that are enabled in the current visual style will determine which properties
		// should not be lockable for now.
		final VisualStyle style = vmm.getCurrentVisualStyle();
		final Set<VisualProperty<?>> disabledProps = new HashSet<VisualProperty<?>>();
		final Set<VisualPropertyDependency<?>> depSet = style.getAllVisualPropertyDependencies();
		
		for (final VisualPropertyDependency<?> dep : depSet) {
			// To do the same as in Cytoscape v2.8, we only care about these dependencies
			if (!dep.getIdString().equals("arrowColorMatchesEdge") && !dep.getIdString().equals("nodeSizeLocked"))
				continue; // TODO: revisit these requirements and remove this workaround.
			
			// In general, the user should not be able to lock the child properties of an enabled dependency.
			if (dep.isDependencyEnabled())
				disabledProps.addAll(dep.getVisualProperties());
			else
				disabledProps.add(dep.getParentVisualProperty());
		}
		
		final Set<VisualLexiconNode> nextNodes = new HashSet<VisualLexiconNode>();

		while (!queue.isEmpty()) {
			final VisualLexiconNode currentNode = queue.poll();
			final VisualProperty<?> vp = currentNode.getVisualProperty();
			
			if (vp.getTargetDataType().isAssignableFrom(targetClass)) {
				final Collection<VisualLexiconNode> children = currentNode.getChildren();
				nextNodes.addAll(children);
				
				if (PropertySheetUtil.isCompatible(vp)) {
					final JMenuItem menu;
					VisualLexiconNode parentNode = currentNode.getParent();
					boolean leaf = children.isEmpty();
					
					if (!leaf) {
						// Non-leaf visual property...
						final JMenuItem nonLeafMenu = new JMenu(vp.getDisplayName());
						menuMap.put(currentNode, nonLeafMenu);
						menuMap.get(parentNode).add(nonLeafMenu);
						
						// Other non-leaf VPs can cause ClassCastExceptions when their values are propagated
						// to their descendants, so let's accept only these ones
						// TODO: What are the generic rules for handling any Lexicon's visual property tree in the UI?
						if (vp == BasicVisualLexicon.NODE_SIZE || vp == BasicVisualLexicon.EDGE_UNSELECTED_PAINT) {
							// So this VP can also be added as a child of itself
							parentNode = currentNode;
							leaf = true;
						}
					}
					
					if (leaf) {
						if (view.isDirectlyLocked(vp)) {
							final JMenuItem clear = new JMenuItem(CLEAR_MENU_LABEL);
							clear.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									view.clearValueLock(vp);
									netView.updateView();
								}
							});
							
							final JMenuItem edit = new JMenuItem(EDIT_BYPASS_MENU_LABEL);
							edit.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									applBypassValue(netView, view, vp);
								}
							});
							
							menu = new JMenu(vp.getDisplayName());
							menu.add(clear);
							menu.add(edit);
		
							// Update color and icon
							JMenuItem enabledItem = menu;
							VisualLexiconNode enabledParent = parentNode;
							
							do {
								enabledItem.setForeground(ENABLED_COLOR);
								enabledItem.setIcon(ENABLED_ICON);
								enabledItem.setFont(ENABLED_FONT);
								enabledItem = menuMap.get(enabledParent);
								enabledParent = enabledParent.getParent();
							} while (enabledItem != null);
							
							rootJMenu.setIcon(ENABLED_ICON);
							rootJMenu.setForeground(ENABLED_COLOR);
							rootJMenu.setFont(ENABLED_FONT);
							
							vpSet.add(vp);
						} else {
							menu = new JMenuItem(vp.getDisplayName());
							menu.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									applBypassValue(netView, view, vp);
								}
							});
						}
					
						menuMap.get(parentNode).add(menu);
						
						if (parentNode == currentNode)
							menuMap.get(parentNode).add(new JSeparator());
						
						// Should this visual property be disabled?
						if (disabledProps.contains(vp))
							menu.setEnabled(false);
					}
				}
			}

			if (queue.isEmpty()) {
				queue.addAll(nextNodes);
				nextNodes.clear();
			}
		}

		final JSeparator separator = new JSeparator();
		final JMenuItem resetMenu = new JMenuItem(RESET_ALL_MENU_LABEL);
		resetMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearAll(netView, view);
			}
		});
		
		rootJMenu.add(separator);
		rootJMenu.add(resetMenu);
		
		return rootMenu;
	}

	
	/**
	 * Apply bypass
	 * 
	 * @param netView
	 * @param graphObjectView
	 * @param vp
	 */
	private final void applBypassValue(final CyNetworkView netView, final View<? extends CyIdentifiable> graphObjectView,
			VisualProperty<?> vp) {
		final ValueEditor<Object> editor = (ValueEditor<Object>) editorManager.getValueEditor(vp.getRange().getType());
		final Object bypassValue = editor.showEditor(null, graphObjectView.getVisualProperty(vp));
		
		if (bypassValue != null) { // null means the action was cancelled
			// Set lock for the vp
			graphObjectView.setLockedValue(vp, bypassValue);
			
			// Apply the new value only for the given view
			// TODO don't do this, because it overwrites some bypassed values with default ones!!! Calling setLockedValue should be enough
//			final CyRow row = netView.getModel().getRow(graphObjectView.getModel());
//			vmm.getCurrentVisualStyle().apply(row, graphObjectView);
			
			// Redraw the view
			netView.updateView();
		}
	}

	private final void clearAll(final CyNetworkView netView, final View<? extends CyIdentifiable> nodeView) {
		boolean needToUpdateView = false;
		final VisualStyle style = vmm.getCurrentVisualStyle();
		
		for (VisualProperty<?> vp : vpSet) {
			final boolean lock = nodeView.isDirectlyLocked(vp);
			if (lock) {
				nodeView.clearValueLock(vp);
				needToUpdateView = true;
			}
		}

		if (needToUpdateView) {
			style.apply(netView);
			netView.updateView();
		}
	}

	private static final class VisualLexiconNodeComparator implements Comparator<VisualLexiconNode> {
		@Override
		public int compare(final VisualLexiconNode node1, final VisualLexiconNode node2) {
			return node1.compareTo(node2);
		}
	}
}
