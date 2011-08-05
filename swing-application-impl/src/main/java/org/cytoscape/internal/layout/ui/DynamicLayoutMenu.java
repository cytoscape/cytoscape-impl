/*
  File: DynamicLayoutMenu.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.internal.layout.ui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.RichVisualLexicon;
import org.cytoscape.work.TaskManager;


/**
 *
 * A DynamicLayoutMenu is a more complicated layout menu that constructs layout menu
 * items on-the-fly based on the capabilities of the layout algorithm and environment
 * factors such as whether or not nodes are selected, the presence of node or edge
 * attributes, etc.
 */
public class DynamicLayoutMenu extends JMenu implements MenuListener {
	private final static long serialVersionUID = 1202339874245069L;
	private CyLayoutAlgorithm layout;
	private static final String NOATTRIBUTE = "(none)";
	private Set<CyNode> selectedNodes;
	private CyApplicationManager appMgr;
	private TaskManager tm;

	/**
	 * Creates a new DynamicLayoutMenu object.
	 *
	 * @param layout  DOCUMENT ME!
	 */
	public DynamicLayoutMenu(CyLayoutAlgorithm layout, boolean enabled, CyApplicationManager appMgr, TaskManager tm) {
		super(layout.toString());
		addMenuListener(this);
		this.layout = layout;
		this.appMgr = appMgr;
		this.tm = tm;
		selectedNodes = new HashSet<CyNode>();
		setEnabled(enabled);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void menuCanceled(MenuEvent e) {
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void menuDeselected(MenuEvent e) {
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void menuSelected(MenuEvent e) {
		// Clear any previous entries
		this.removeAll();

		// Base the menu structure only on the current network. 
		CyNetwork network = appMgr.getCurrentNetwork();

		// First, do we support selectedOnly?
		selectedNodes = new HashSet<CyNode>(CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true));

		if (layout.supportsSelectedOnly() && (selectedNodes.size() > 0)) {
			// Add selected node/all nodes menu
			addSelectedOnlyMenus(network);
		} else if (layout.supportsNodeAttributes().size() > 0) {
			// Add node attributes menus
			addNodeAttributeMenus(this, network, false);
		} else if (layout.supportsEdgeAttributes().size() > 0) {
			// Add edge attributes menus
			addEdgeAttributeMenus(this, network, false);
		} else {
			// No special menus, so make sure we layout all selected
			List<CyNetworkView> views = appMgr.getSelectedNetworkViews();
			for (final CyNetworkView view: views) {
				layout.setSelectedOnly(false);
				layout.setLayoutAttribute(null);
				layout.setNetworkView(view);
				tm.execute(layout);
			}
		}
	}

	private void addNodeAttributeMenus(final JMenu parent, final CyNetwork net,
					   final boolean selectedOnly)
	{
		final CyTable nodeAttributes = net.getDefaultNodeTable(); 
		addAttributeMenus(parent, nodeAttributes, layout.supportsNodeAttributes(), selectedOnly);
	}

	private void addEdgeAttributeMenus(final JMenu parent, final CyNetwork net,
					   final boolean selectedOnly)
	{
		final CyTable edgeAttributes = net.getDefaultEdgeTable();
		addAttributeMenus(parent, edgeAttributes, layout.supportsEdgeAttributes(), selectedOnly);
	}

	private void addAttributeMenus(JMenu parent, CyTable attributes, Set<Class<?>> typeSet,
	                               boolean selectedOnly)
	{
		// Add any special attributes
		final List<String> specialList = layout.getInitialAttributeList();
		if (specialList != null)
			for (final String attrName : specialList) {
				parent.add(new LayoutAttributeMenuItem(attrName, selectedOnly));
		}

		for (final CyColumn column : attributes.getColumns()) {
			if (typeSet.contains(column.getType()))
				parent.add(new LayoutAttributeMenuItem(column.getName(),
								       selectedOnly));
		}
	}

	private void addSelectedOnlyMenus(final CyNetwork net) {
		JMenuItem allNodes;
		JMenuItem selNodes;

		if ((layout.supportsNodeAttributes().size() > 0) || (layout.supportsEdgeAttributes().size() > 0)) {
			allNodes = new JMenu("All Nodes");
			selNodes = new JMenu("Selected Nodes Only");

			if (layout.supportsNodeAttributes().size() > 0) {
				addNodeAttributeMenus((JMenu) allNodes, net, false);
				addNodeAttributeMenus((JMenu) selNodes, net, true);
			} else {
				addEdgeAttributeMenus((JMenu) allNodes, net, false);
				addEdgeAttributeMenus((JMenu) selNodes, net, true);
			}
		} else {
			allNodes = new LayoutAttributeMenuItem("All Nodes", false);
			selNodes = new LayoutAttributeMenuItem("Selected Nodes Only", true);
		}

		this.add(allNodes);
		this.add(selNodes);
	}

	protected class LayoutAttributeMenuItem extends JMenuItem implements ActionListener {
	private final static long serialVersionUID = 1202339874231860L;
		boolean selectedOnly = false;

		public LayoutAttributeMenuItem(String label, boolean selectedOnly) {
			super(label);
			addActionListener(this);
			this.selectedOnly = selectedOnly;
		}

		public void actionPerformed(ActionEvent e) {
			List<CyNetworkView> views = appMgr.getSelectedNetworkViews();
			for (final CyNetworkView netView : views) {
				if (layout.supportsSelectedOnly()) {
					if (selectedOnly && (selectedNodes.size() > 0))
						lockUnselectedNodeCoords(netView);
				}

				if (layout.supportsNodeAttributes().size() > 0 || layout.supportsEdgeAttributes().size() > 0)
					layout.setLayoutAttribute(e.getActionCommand());

				layout.setSelectedOnly(selectedOnly);
				layout.setNetworkView(netView);
				tm.execute(layout);
			}
		}
	}

	private void lockUnselectedNodeCoords(final CyNetworkView netView) {
		for (final View<CyNode> nv : netView.getNodeViews()) {
			CyNode node = nv.getModel();
	
			if (!selectedNodes.contains(node)) {
				nv.setLockedValue(MinimalVisualLexicon.NODE_X_LOCATION,
						  nv.getVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION));
				nv.setLockedValue(MinimalVisualLexicon.NODE_Y_LOCATION,
						  nv.getVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION));
				nv.setLockedValue(RichVisualLexicon.NODE_Z_LOCATION,
						  nv.getVisualProperty(RichVisualLexicon.NODE_Z_LOCATION));
			}
		}
	}
}
