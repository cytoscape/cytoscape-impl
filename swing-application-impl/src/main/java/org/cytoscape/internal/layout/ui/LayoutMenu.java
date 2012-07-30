/*
  File: LayoutMenu.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

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

import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;


/**
 *
 * A DynamicLayoutMenu is a more complicated layout menu that constructs layout menu
 * items on-the-fly based on the capabilities of the layout algorithm and environment
 * factors such as whether or not nodes are selected, the presence of node or edge
 * attributes, etc.
 */

// This should extend CyMenuItem, I think....
public class LayoutMenu extends JMenu implements MenuListener {
    private final static long serialVersionUID = 1202339874255880L;
    List<CyLayoutAlgorithm> subMenuList;
    private CyApplicationManager appMgr;
    private DialogTaskManager tm;
    private final LayoutComparator layoutComparator = new LayoutComparator();

    /**
     * Creates a new LayoutMenu object.
     *
     * @param menuName  DOCUMENT ME!
     */
    public LayoutMenu(String menuName, CyApplicationManager appMgr, DialogTaskManager tm) {
        super(menuName);
        addMenuListener(this);
        subMenuList = new ArrayList<CyLayoutAlgorithm>();
        this.appMgr = appMgr;
        this.tm = tm;
    }

    /**
     *  DOCUMENT ME!
     *
     * @param layout DOCUMENT ME!
     */
    public void add(CyLayoutAlgorithm layout) {
        subMenuList.add(layout);
    }

    /**
     *  DOCUMENT ME!
     *
     * @param layout DOCUMENT ME!
     */
    public void remove(CyLayoutAlgorithm layout) {
        subMenuList.remove(layout);
    }

    /**
     *  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getItemCount() {
        return subMenuList.size();
    }

    /**
     *  DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void menuCanceled(MenuEvent e) { } ;

    /**
     *  DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void menuDeselected(MenuEvent e) { } ;

    /**
     *  DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void menuSelected(MenuEvent e) {
        // Clear any previous entries
        this.removeAll();

        CyNetworkView view = appMgr.getCurrentNetworkView();
        if ( view == null )
            return;

        CyNetwork network = view.getModel();

        // Figure out if we have anything selected
        boolean someSelected = network.getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED, true) > 0;
        boolean enableMenuItem = checkEnabled();
        
        Collections.sort(subMenuList,layoutComparator);

        for ( CyLayoutAlgorithm layout : subMenuList ) {
        
            boolean usesNodeAttrs = 
                hasValidAttributes(layout.getSupportedNodeAttributeTypes(),network.getDefaultNodeTable());
            boolean usesEdgeAttrs = 
                hasValidAttributes(layout.getSupportedEdgeAttributeTypes(),network.getDefaultEdgeTable());
            boolean usesSelected = (layout.getSupportsSelectedOnly() && someSelected);

            if (usesNodeAttrs || usesEdgeAttrs || usesSelected) {
                super.add(new DynamicLayoutMenu(layout,network,enableMenuItem,appMgr,
                                                tm,usesNodeAttrs,usesEdgeAttrs,usesSelected));
            } else {
                super.add(new StaticLayoutMenu(layout,enableMenuItem,appMgr,tm));
            }
        }
    }
    
    private boolean hasValidAttributes(Set<Class<?>> typeSet, CyTable attributes) {
        for (final CyColumn column : attributes.getColumns())
            if (typeSet.contains(column.getType()))
                return true;
        return false;
    }

    private boolean checkEnabled() {
        CyNetwork network = appMgr.getCurrentNetwork();
        if ( network == null )
            return false;

        CyNetworkView view = appMgr.getCurrentNetworkView();
        if ( view == null )
            return false;
        else
            return true;
    }

    private static class LayoutComparator implements Comparator<CyLayoutAlgorithm> {
        public int compare(CyLayoutAlgorithm o1, CyLayoutAlgorithm o2) {
            return o1.toString().compareTo(o2.toString());    
        }
        public boolean equals(Object obj) {
            return ( obj == this );
        }
    }
}

