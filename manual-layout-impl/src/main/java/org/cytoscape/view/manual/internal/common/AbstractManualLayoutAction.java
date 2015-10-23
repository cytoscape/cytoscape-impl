package org.cytoscape.view.manual.internal.common;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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



import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.view.model.CyNetworkViewManager;

/**
 * Base class for displaying cytopanel menu items. This class primarily
 * manages the Layout Menu logic and tab selection of the tools cytopanel. 
 */
@SuppressWarnings("serial")
public abstract class AbstractManualLayoutAction 
	extends AbstractCyAction 
	implements CytoPanelComponentSelectedListener {

    static protected CytoPanel manualLayoutPanel; 

	private static int selectedIndex = -1;

	private final CySwingApplication swingApp;

	private final static String preferredMenu = "Layout";
	private final String title;
	private final CytoPanelComponent comp;

	/**
	 * Base class for displaying cytopanel menu items. 
	 *
	 * @param title The title of the menu item. 
	 */
	public AbstractManualLayoutAction(CytoPanelComponent comp, CySwingApplication swingApp, CyApplicationManager appMgr, final CyNetworkViewManager networkViewManager, float menuGravity) {
		super(comp.getTitle(), appMgr,"networkAndView", networkViewManager);
		this.title = comp.getTitle();
		this.swingApp = swingApp;
		this.comp = comp;
    	manualLayoutPanel = swingApp.getCytoPanel(CytoPanelName.SOUTH_WEST);
		setPreferredMenu(preferredMenu);
		setMenuGravity(menuGravity);
		useCheckBoxMenuItem = true;
	}

	/**
	 * Selects the component and hides/unhides the cytopanel as necessary. 
	 *
	 * @param ev Triggering event - not used. 
	 */
	public void actionPerformed(ActionEvent ev) {

		// Check the state of the manual layout Panel
		CytoPanelState curState = manualLayoutPanel.getState();

		int menuIndex = manualLayoutPanel.indexOfComponent(comp.getComponent());

		// Case 1: Panel is disabled
		if (curState == CytoPanelState.HIDE) {
			manualLayoutPanel.setState(CytoPanelState.DOCK);
			manualLayoutPanel.setSelectedIndex(menuIndex);
			selectedIndex = menuIndex;

		// Case 2: Panel is in the DOCK/FLOAT and a different panel is selected
		} else if ( manualLayoutPanel.getSelectedIndex() != menuIndex ) {
			manualLayoutPanel.setSelectedIndex(menuIndex);
			selectedIndex = menuIndex;

		// Case 3: The currently selected item is selected 
		} else { 
			manualLayoutPanel.setState(CytoPanelState.HIDE);
			selectedIndex = -1;
		}
	} 

	private JCheckBoxMenuItem getThisItem() {
		JMenu layouts = swingApp.getJMenu(preferredMenu);
		for ( int i = 0; i < layouts.getItemCount(); i++ ) {
			JMenuItem item = layouts.getItem(i);
			if (item instanceof JCheckBoxMenuItem && item.getText().equals(title)) {
				return (JCheckBoxMenuItem)item;	
			}
		}
		return null;
	}

	/**
	 * Enables of disables the action based on system state. 
	 *
	 * @param ev Triggering event - not used. 
	 */
	public void menuSelected(MenuEvent e) {
		// set the check next to the menu item
		JCheckBoxMenuItem item = getThisItem(); 
		int menuIndex = manualLayoutPanel.indexOfComponent(comp.getComponent());

		if ( item != null ) {
			if ( manualLayoutPanel.getSelectedIndex() != menuIndex || 
			     manualLayoutPanel.getState() == CytoPanelState.HIDE )
				item.setState(false);
			else 
				item.setState(true);
		}
	
		// enable the menu based on cytopanel state
		CytoPanelState parentState = swingApp.getCytoPanel(CytoPanelName.WEST).getState();
		if ( parentState == CytoPanelState.HIDE )
			setEnabled(false);
		else 
			setEnabled(true);

		// enable the menu based on presence of network 
		updateEnableState();
	}

	/**
	 * Makes sure the menu check stays in sync with the selections made in the cytopanel.
	 *
	 * @param componentIndex the index of the menu
	 */
	public void handleEvent(CytoPanelComponentSelectedEvent e) {
		selectedIndex = e.getSelectedIndex();
	}
}
