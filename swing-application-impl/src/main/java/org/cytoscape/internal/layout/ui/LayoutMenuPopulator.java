package org.cytoscape.internal.layout.ui;

import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.internal.view.CytoscapeMenuBar;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

public class LayoutMenuPopulator implements MenuListener {

	private Map<CyLayoutAlgorithm, Map> algorithmMap;
	private Map<CyLayoutAlgorithm, JMenuItem> menuMap;
	private Map<CyLayoutAlgorithm, Boolean> separatorMap;
	private GravityTracker gravityTracker;
	private JMenu layoutMenu;
	
	private final CyServiceRegistrar serviceRegistrar;

    public LayoutMenuPopulator(CytoscapeMenuBar menuBar, final CyServiceRegistrar serviceRegistrar) {
        algorithmMap = new HashMap<>();
        menuMap = new HashMap<>();
        separatorMap = new HashMap<>();
        this.serviceRegistrar = serviceRegistrar;
        
        this.gravityTracker = menuBar.getMenuTracker().getGravityTracker("Layout");
        this.layoutMenu = (JMenu)gravityTracker.getMenu();
        this.layoutMenu.addMenuListener(this);
    }

    public void addLayout(CyLayoutAlgorithm layout, Map props) {
        algorithmMap.put(layout, props);
    }

    public void removeLayout(CyLayoutAlgorithm layout, Map props) {
        algorithmMap.remove(layout);
        
        if (menuMap.containsKey(layout))
            layoutMenu.remove(menuMap.remove(layout));
    }

    @Override
    public void menuCanceled(MenuEvent e) { } ;

    @Override
    public void menuDeselected(MenuEvent e) { } ;

    @Override
    public void menuSelected(MenuEvent e) {
    	final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
    	final DialogTaskManager tm = serviceRegistrar.getService(DialogTaskManager.class);
    	
        CyNetworkView view = appMgr.getCurrentNetworkView();
        CyNetwork network = appMgr.getCurrentNetwork();

        // Figure out if we have anything selected
        boolean someSelected = false;
        if (network != null)
            someSelected = network.getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED, true) > 0;
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
                layoutMenu.remove(menuMap.remove(layout));
            }
        
            boolean usesNodeAttrs = false;
            if (network != null)
                usesNodeAttrs = hasValidAttributes(layout.getSupportedNodeAttributeTypes(),network.getDefaultNodeTable());
            boolean usesEdgeAttrs = false;
            if (network != null)
                usesEdgeAttrs = hasValidAttributes(layout.getSupportedEdgeAttributeTypes(),network.getDefaultEdgeTable());
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
				gravityTracker.addMenuSeparator(gravity + 0.0001);
				separatorMap.put(layout, Boolean.TRUE);
			} else if (separatorBefore && !separatorMap.containsKey(layout)) {
				gravityTracker.addMenuSeparator(gravity - 0.0001);
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
    	CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
    	
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
