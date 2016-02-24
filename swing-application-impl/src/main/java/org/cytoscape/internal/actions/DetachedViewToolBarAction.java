package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.internal.view.NetworkViewMediator;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
public class DetachedViewToolBarAction extends AbstractCyAction {
	
	private static String TITLE = "Detached View Tool Bars";
	private static String SHOW = "Show";
	private static String HIDE = "Hide";

	private final NetworkViewMediator netViewMediator;
	
	public DetachedViewToolBarAction(final float menuGravity, final NetworkViewMediator netViewMediator) {
		super(getTitle(netViewMediator));
		this.netViewMediator = netViewMediator;
		
		setPreferredMenu("View");
		setMenuGravity(menuGravity);
		insertSeparatorBefore = true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		netViewMediator.setViewToolBarsVisible(!netViewMediator.isViewToolBarsVisible());
	}
	
	@Override
	public void menuSelected(MenuEvent e) {
		putValue(Action.NAME, getTitle(netViewMediator));
	}
	
	private static String getTitle(final NetworkViewMediator netViewMediator) {
		return (netViewMediator.isViewToolBarsVisible() ? HIDE : SHOW) + " " + TITLE;
	}
}
