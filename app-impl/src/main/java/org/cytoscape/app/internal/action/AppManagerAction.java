package org.cytoscape.app.internal.action;

import java.awt.event.ActionEvent;

import org.cytoscape.app.internal.ui.AppManagerMediator;
import org.cytoscape.application.swing.AbstractCyAction;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2019 The Cytoscape Consortium
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
public class AppManagerAction extends AbstractCyAction {

	private final AppManagerMediator appManagerMediator;

	public AppManagerAction(AppManagerMediator appManagerMediator) {
		super("App Manager...");
		
		setPreferredMenu("Apps");
		setMenuGravity(1.0f);
		
		this.appManagerMediator = appManagerMediator;
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
		appManagerMediator.showAppManager(false, evt);
	}
	
	@Override
	public boolean isEnabled() {
		return !appManagerMediator.isAppManagerVisible();
	}
}
