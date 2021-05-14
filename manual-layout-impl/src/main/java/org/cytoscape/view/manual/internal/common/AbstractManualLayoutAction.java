package org.cytoscape.view.manual.internal.common;

import java.awt.event.ActionEvent;

import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.view.model.CyNetworkViewManager;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * Base class for displaying cytopanel menu items. This class primarily
 * manages the Layout Menu logic and tab selection of the tools cytopanel. 
 */
@SuppressWarnings("serial")
public abstract class AbstractManualLayoutAction extends AbstractCyAction {

    static protected CytoPanel manualLayoutPanel; 

	private final CySwingApplication swingApp;

	private final static String preferredMenu = "Layout";
	private final CytoPanelComponent comp;

	/**
	 * Base class for displaying cytopanel menu items. 
	 *
	 * @param title The title of the menu item. 
	 */
	public AbstractManualLayoutAction(
			CytoPanelComponent comp,
			CySwingApplication swingApp,
			CyApplicationManager appMgr,
			CyNetworkViewManager networkViewManager,
			float menuGravity
	) {
		super(comp.getTitle(), appMgr,"networkAndView", networkViewManager);
		this.swingApp = swingApp;
		this.comp = comp;
    	manualLayoutPanel = swingApp.getCytoPanel(CytoPanelName.SOUTH_WEST);
		setPreferredMenu(preferredMenu);
		setMenuGravity(menuGravity);
		useCheckBoxMenuItem = true;
	}

	/**
	 * Selects the component and hides/unhides the cytopanel as necessary. 
	 */
	@Override
	public void actionPerformed(ActionEvent ev) {
		// Check the state of the manual layout Panel
		CytoPanelState curState = manualLayoutPanel.getState();

		int menuIndex = manualLayoutPanel.indexOfComponent(comp.getComponent());

		if (curState == CytoPanelState.HIDE) {
			// Case 1: Panel is disabled
			manualLayoutPanel.setState(CytoPanelState.DOCK);
			manualLayoutPanel.setSelectedIndex(menuIndex);
		} else if (manualLayoutPanel.getSelectedIndex() != menuIndex) {
			// Case 2: Panel is in the DOCK/FLOAT and a different panel is selected
			manualLayoutPanel.setSelectedIndex(menuIndex);
		} else {
			// Case 3: The currently selected item is selected
			manualLayoutPanel.setState(CytoPanelState.HIDE);
		}
	}

	/**
	 * Enables of disables the action based on system state. 
	 */
	@Override
	public void menuSelected(MenuEvent e) {
		// set the check next to the menu item
		int menuIndex = manualLayoutPanel.indexOfComponent(comp.getComponent());

		if (manualLayoutPanel.getSelectedIndex() != menuIndex || manualLayoutPanel.getState() == CytoPanelState.HIDE)
			putValue(SELECTED_KEY, false);
		else
			putValue(SELECTED_KEY, true);

		// enable the menu based on cytopanel state
		CytoPanelState parentState = swingApp.getCytoPanel(CytoPanelName.WEST).getState();

		if (parentState == CytoPanelState.HIDE)
			setEnabled(false);
		else
			setEnabled(true);

		// enable the menu based on presence of network
		updateEnableState();
	}
}
