package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Icon;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.internal.view.CytoscapeDesktop;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

@SuppressWarnings("serial")
public class StarterPanelAction extends AbstractCyAction {
	
	private static String TITLE = "Show Starter Panel";

	private final CytoscapeDesktop desktop;
	
	/**
	 * Use this constructor to register the action as a menu item.
	 */
	public StarterPanelAction(float menuGravity, CytoscapeDesktop desktop) {
		super(TITLE);
		this.desktop = desktop;
		
		setPreferredMenu("View");
		setMenuGravity(menuGravity);
		insertSeparatorBefore = true;
		useCheckBoxMenuItem = true;
	}
	
	/**
	 * Use this constructor to register the action as a tool bar button.
	 */
	public StarterPanelAction(float toolbarGravity, Icon icon, CytoscapeDesktop desktop) {
		super(TITLE);
		this.desktop = desktop;
		
		putValue(LARGE_ICON_KEY, icon);
		putValue(SHORT_DESCRIPTION, TITLE);
		setIsInToolBar(true);
		setToolbarGravity(toolbarGravity);
		useToggleButton = true;
		
		desktop.getStarterPanel().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				updateEnableState();
			}
			@Override
			public void componentShown(ComponentEvent e) {
				updateEnableState();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (desktop.isStarterPanelVisible())
			desktop.hideStarterPanel();
		else
			desktop.showStarterPanel();
	}
	
	@Override
	public void menuSelected(MenuEvent evt) {
		updateEnableState();
	}
	
	@Override
	public void updateEnableState() {
		super.updateEnableState();
		putValue(SELECTED_KEY, desktop.isStarterPanelVisible());
	}
}
