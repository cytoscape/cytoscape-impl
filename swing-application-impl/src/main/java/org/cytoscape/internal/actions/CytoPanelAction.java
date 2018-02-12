package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;

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
public class CytoPanelAction extends AbstractCyAction {
	
	protected static String SHOW = "Show";
	protected static String HIDE = "Hide";

	protected String title;
	protected CytoPanelName position;
	private CySwingApplication desktop;

	public CytoPanelAction(final CytoPanelName position, final boolean show, final CySwingApplication desktop,
			float menuGravity) {
		super(show ? HIDE + " " + position.getTitle() : SHOW + " " + position.getTitle());

		this.title = position.getTitle();
		this.position = position;
		this.desktop = desktop;
		
		setPreferredMenu("View");
		setMenuGravity(menuGravity);
	}

	/**
	 * Toggles the cytopanel state.  
	 */
	@Override
	public void actionPerformed(ActionEvent ev) {
		CytoPanelState curState = desktop.getCytoPanel(position).getState();

		if (position == CytoPanelName.SOUTH_WEST && curState == CytoPanelState.HIDE)			// #4100
		{
			CytoPanel west = desktop.getCytoPanel(CytoPanelName.WEST);
			CytoPanelState westState = west.getState();
			if (	westState == CytoPanelState.HIDE)
				west.setState(CytoPanelState.DOCK);
		}
		if (curState == CytoPanelState.HIDE)
			desktop.getCytoPanel(position).setState(CytoPanelState.DOCK);
		else
			desktop.getCytoPanel(position).setState(CytoPanelState.HIDE);
	} 

	/**
	 * This dynamically sets the title of the menu based on the state of the CytoPanel.
	 */
	@Override
	public void menuSelected(MenuEvent me) {
		CytoPanelState curState = desktop.getCytoPanel(position).getState();
		
		if (curState == CytoPanelState.HIDE)
			putValue(Action.NAME, SHOW + " " + title);
		else
			putValue(Action.NAME, HIDE + " " + title);
	}
}
