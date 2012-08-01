/*
  File: LayoutMenuPopulator.java

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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.view.CytoscapeMenuBar;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskManager;
import static org.cytoscape.work.ServiceProperties.*;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.util.swing.JMenuTracker;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;

import java.util.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;


public class LayoutMenuPopulator implements MenuListener {

		private Map<CyLayoutAlgorithm, Map> algorithmMap;
		private Map<CyLayoutAlgorithm, JMenuItem> menuMap;
		private Map<CyLayoutAlgorithm, Boolean> separatorMap;
    private CyApplicationManager appMgr;
    private DialogTaskManager tm;
    private GravityTracker gravityTracker; 
    private JMenu layoutMenu;

    public LayoutMenuPopulator(CytoscapeMenuBar menuBar, CyApplicationManager appMgr, DialogTaskManager tm) {
        algorithmMap = new HashMap<CyLayoutAlgorithm,Map>();
        menuMap = new HashMap<CyLayoutAlgorithm,JMenuItem>();
        separatorMap = new HashMap<CyLayoutAlgorithm,Boolean>();
        this.appMgr = appMgr;
        this.tm = tm;
        this.gravityTracker = menuBar.getMenuTracker().getGravityTracker("Layout");
        this.layoutMenu = (JMenu)gravityTracker.getMenu();
        this.layoutMenu.addMenuListener(this);
    }

    public void addLayout(CyLayoutAlgorithm layout, Map props) {
        algorithmMap.put(layout, props);
    }

    public void removeLayout(CyLayoutAlgorithm layout, Map props) {
        algorithmMap.remove(layout);
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
        CyNetworkView view = appMgr.getCurrentNetworkView();
        if ( view == null )
            return;

        CyNetwork network = view.getModel();

        // Figure out if we have anything selected
        boolean someSelected = network.getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED, true) > 0;
        boolean enableMenuItem = checkEnabled();

        // Get all of the algorithms
        for ( CyLayoutAlgorithm layout : algorithmMap.keySet() ) {
            Map props = algorithmMap.get(layout);
            double gravity = 1000.0;
            if (props.get(MENU_GRAVITY) != null)
            	gravity = Double.parseDouble((String)props.get(MENU_GRAVITY));

						boolean separatorAfter = false;
            if (props.get(INSERT_SEPARATOR_AFTER) != null)
            	separatorAfter = Boolean.parseBoolean((String)props.get(INSERT_SEPARATOR_AFTER));

						boolean separatorBefore = false;
            if (props.get(INSERT_SEPARATOR_BEFORE) != null)
            	separatorBefore = Boolean.parseBoolean((String)props.get(INSERT_SEPARATOR_BEFORE));

            // Remove the old menu
            if (menuMap.containsKey(layout)) {
                layoutMenu.remove(menuMap.get(layout));
                menuMap.remove(layout);
            }
        
            boolean usesNodeAttrs = 
                hasValidAttributes(layout.getSupportedNodeAttributeTypes(),network.getDefaultNodeTable());
            boolean usesEdgeAttrs = 
                hasValidAttributes(layout.getSupportedEdgeAttributeTypes(),network.getDefaultEdgeTable());
            boolean usesSelected = (layout.getSupportsSelectedOnly() && someSelected);

            if (usesNodeAttrs || usesEdgeAttrs || usesSelected) {
                JMenu newMenu = new DynamicLayoutMenu(layout,network,enableMenuItem,appMgr,
                                                      tm,usesNodeAttrs,usesEdgeAttrs,usesSelected);
                menuMap.put(layout, newMenu);
                gravityTracker.addMenu(newMenu, gravity);
            } else {
                JMenuItem newMenu = new StaticLayoutMenu(layout,enableMenuItem,appMgr,tm);
                menuMap.put(layout, newMenu);
                gravityTracker.addMenuItem(newMenu, gravity);
            }

						if (separatorAfter && !separatorMap.containsKey(layout)) {
                gravityTracker.addMenuSeparator(gravity+0.0001);
                separatorMap.put(layout, Boolean.TRUE);
						} else if (separatorBefore && !separatorMap.containsKey(layout)) {
                gravityTracker.addMenuSeparator(gravity-0.0001);
                separatorMap.put(layout, Boolean.TRUE);
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
    
}
