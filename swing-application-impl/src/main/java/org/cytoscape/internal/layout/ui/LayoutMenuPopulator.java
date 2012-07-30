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

import org.cytoscape.view.layout.CyLayoutAlgorithm;
import static org.cytoscape.work.ServiceProperties.*;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.view.CytoscapeMenuBar;

import java.util.*;


public class LayoutMenuPopulator {

    private Map<String, List<CyLayoutAlgorithm>> menuAlgorithmMap;
    private Map<String, LayoutMenu> menuMap;
    private CyApplicationManager appMgr;
    private DialogTaskManager tm;
    private CytoscapeMenuBar menuBar;

    public LayoutMenuPopulator(CytoscapeMenuBar menuBar, CyApplicationManager appMgr, DialogTaskManager tm) {
        menuAlgorithmMap = new HashMap<String,List<CyLayoutAlgorithm>>();
        menuMap = new HashMap<String,LayoutMenu>();
        this.appMgr = appMgr;
        this.tm = tm;
        this.menuBar = menuBar;
    }

    public void addLayout(CyLayoutAlgorithm layout, Map props) {
        String fullMenuName = (String)props.get(PREFERRED_MENU);
        if (fullMenuName == null )
            fullMenuName = "Layout." + layout.toString();
        
        String actualMenuName = fullMenuName;
        if ( actualMenuName.startsWith("Layout."))
            actualMenuName = actualMenuName.substring(7);

        // make sure the list is set up for this name
        if ( !menuAlgorithmMap.containsKey(fullMenuName) ) {
            List<CyLayoutAlgorithm> menuList = new ArrayList<CyLayoutAlgorithm>();
            menuAlgorithmMap.put(fullMenuName, menuList);
        }

        // add layout to the list of layouts for this name
        menuAlgorithmMap.get(fullMenuName).add(layout);

        // make sure the menu is set up
        if ( !menuMap.containsKey(fullMenuName) ) {
            LayoutMenu menu = new LayoutMenu(actualMenuName, appMgr, tm);
            menuMap.put(fullMenuName, menu);
            // This needs to be done so that gravity is supported
            menuBar.getMenu("Layout").add(menu);
        } else {
            // See if the menu type has changed from a Menu to a MenuItem or visa-versa
            // Yes, replace it.
            LayoutMenu menu = menuMap.get(fullMenuName);
            // if (menu.updateSubMenus()) {
                // Update the menu
            // }
        }

        // add layout to the menu for this name
        menuMap.get(fullMenuName).add(layout);
    }

    public void removeLayout(CyLayoutAlgorithm layout, Map props) {
        for (String menu : menuAlgorithmMap.keySet()) {
            List<CyLayoutAlgorithm> menuList = menuAlgorithmMap.get(menu);
            if (menuList.indexOf(layout) >= 0) {
                menuList.remove(layout);
                menuMap.get(menu).remove(layout);
                return;
            }
        }
    }
}
