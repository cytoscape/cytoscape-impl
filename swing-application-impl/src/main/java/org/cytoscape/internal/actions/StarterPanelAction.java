package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.internal.view.CytoscapeDesktop;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
	
	private static String TITLE = "Starter Panel";
	private static String SHOW = "Show";
	private static String HIDE = "Hide";

	private final CytoscapeDesktop desktop;
	
	public StarterPanelAction(final float menuGravity, final CytoscapeDesktop desktop) {
		super(getTitle(desktop));
		this.desktop = desktop;
		
		setPreferredMenu("View");
		setMenuGravity(menuGravity);
		insertSeparatorBefore = true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (desktop.isStarterPanelVisible())
			desktop.hideStarterPanel();
		else
			desktop.showStarterPanel();
	}
	
	@Override
	public void menuSelected(MenuEvent e) {
		putValue(Action.NAME, getTitle(desktop));
	}
	
	private static String getTitle(final CytoscapeDesktop desktop) {
		return (desktop.isStarterPanelVisible() ? HIDE : SHOW) + " " + TITLE;
	}
}
