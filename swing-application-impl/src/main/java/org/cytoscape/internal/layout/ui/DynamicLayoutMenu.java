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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.swing.DialogTaskManager;


/**
 *
 * A DynamicLayoutMenu is a more complicated layout menu that constructs layout menu
 * items on-the-fly based on the capabilities of the layout algorithm and environment
 * factors such as whether or not nodes are selected, the presence of node or edge
 * attributes, etc.
 */
public class DynamicLayoutMenu extends JMenu implements MenuListener {
    private final static long serialVersionUID = 1202339874245069L;
    private final CyLayoutAlgorithm layout;
    private final DialogTaskManager tm;
    private final boolean usesNodeAttrs;
    private final boolean usesEdgeAttrs;
    private final boolean supportsSelectedOnly;
    private final CyNetwork network;
    private final CyApplicationManager appMgr;

	private static final String UNWEIGHTED = "(none)";


    /**
     * Creates a new DynamicLayoutMenu object.
     *
     * @param layout  DOCUMENT ME!
     */
    public DynamicLayoutMenu(CyLayoutAlgorithm layout, CyNetwork network, boolean enabled, CyApplicationManager appMgr, DialogTaskManager tm,
    		                 boolean usesNodeAttrs, boolean usesEdgeAttrs, boolean supportsSelectedOnly) {
        super(layout.toString());
        this.layout = layout;
        this.network = network;
        this.tm = tm;
        this.appMgr = appMgr;
        addMenuListener(this);
        setEnabled(enabled);
        this.supportsSelectedOnly = supportsSelectedOnly;
        this.usesNodeAttrs = usesNodeAttrs;
        this.usesEdgeAttrs = usesEdgeAttrs;
    }

    public void menuCanceled(MenuEvent e) { }

    public void menuDeselected(MenuEvent e) { }

    public void menuSelected(MenuEvent e) {
        // Clear any previous entries
        this.removeAll();

        if (supportsSelectedOnly) {
            // Add selected node/all nodes menu
            addSelectedOnlyMenus(this);
        } else if (usesNodeAttrs) {
            // Add node attributes menus
            addNodeAttributeMenus(this, false);
        } else if (usesEdgeAttrs) {
            // Add edge attributes menus
            addEdgeAttributeMenus(this, false);
        } else {
        	throw new RuntimeException("Layout algorithm expected valid attributes or sel");
        }
    }

    private void addNodeAttributeMenus(final JMenu parent, final boolean selectedOnly) {
        final CyTable nodeAttributes = network.getDefaultNodeTable();
        addAttributeMenus(parent, nodeAttributes, layout.getSupportedNodeAttributeTypes(), selectedOnly);
    }

    private void addEdgeAttributeMenus(final JMenu parent, final boolean selectedOnly) {
        final CyTable edgeAttributes = network.getDefaultEdgeTable();
        addAttributeMenus(parent, edgeAttributes, layout.getSupportedEdgeAttributeTypes(), selectedOnly);
    }

    private void addAttributeMenus(JMenu parent, CyTable attributes, Set<Class<?>> typeSet, boolean selectedOnly) {
        parent.add(new LayoutAttributeMenuItem(UNWEIGHTED, selectedOnly));
    	for (final CyColumn column : attributes.getColumns())
            if (typeSet.contains(column.getType()))
                parent.add(new LayoutAttributeMenuItem(column.getName(), selectedOnly));
    }

    private void addSelectedOnlyMenus(JMenu parent) {
        JMenuItem allNodes;
        JMenuItem selNodes;

        if (usesNodeAttrs || usesEdgeAttrs) {
            allNodes = new JMenu("All Nodes");
            selNodes = new JMenu("Selected Nodes Only");
            
            if (usesNodeAttrs) {
                addNodeAttributeMenus((JMenu) allNodes, false);
                addNodeAttributeMenus((JMenu) selNodes, true);
            } else {
                addEdgeAttributeMenus((JMenu) allNodes, false);
                addEdgeAttributeMenus((JMenu) selNodes, true);
            }
        } else {
            allNodes = new LayoutAttributeMenuItem("All Nodes", false);
            selNodes = new LayoutAttributeMenuItem("Selected Nodes Only", true);
        }

        parent.add(allNodes);
        parent.add(selNodes);
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
        	// NOTE: We iterate over all selected networks, not just the current network!
            List<CyNetworkView> views = appMgr.getSelectedNetworkViews();
            for (final CyNetworkView netView : views) {
                Set<View<CyNode>> nodeViews;
                if (layout.getSupportsSelectedOnly() && selectedOnly)
                    nodeViews = getSelectedNodeViews(netView);
                else
                    nodeViews = CyLayoutAlgorithm.ALL_NODE_VIEWS;

                String layoutAttribute = null;
                if (layout.getSupportedNodeAttributeTypes().size() > 0 || layout.getSupportedEdgeAttributeTypes().size() > 0){
                	layoutAttribute = e.getActionCommand();
                	if (layoutAttribute.equals(UNWEIGHTED))
                		layoutAttribute = null;
                }
                tm.execute(layout.createTaskIterator(netView,layout.getDefaultLayoutContext(),nodeViews,layoutAttribute));
            }
        }
        
        private Set<View<CyNode>> getSelectedNodeViews(final CyNetworkView view) {
        	List<CyNode> selectedNodes = CyTableUtil.getNodesInState(view.getModel(), CyNetwork.SELECTED, true);
            if ( selectedNodes.isEmpty() )
                return CyLayoutAlgorithm.ALL_NODE_VIEWS;

            Set<View<CyNode>> nodeViews = new HashSet<View<CyNode>>();
            for ( CyNode n : selectedNodes ) {
            	View<CyNode> nodeView = view.getNodeView(n);
            	if (nodeView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE))
            		nodeViews.add(nodeView);
            }
            return nodeViews;
        }
    }


}
