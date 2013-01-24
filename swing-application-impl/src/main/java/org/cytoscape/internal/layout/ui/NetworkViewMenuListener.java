package org.cytoscape.internal.layout.ui;

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

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DynamicSubmenuListener;


public class NetworkViewMenuListener implements MenuListener {

	private final MenuListener actualMenuListener;
	private final ActionEnableSupport menuEnableSupport; 

	public NetworkViewMenuListener(DynamicSubmenuListener actualMenuListener, CyApplicationManager appMgr, final CyNetworkViewManager networkViewManager, String enableFor) {
		this.actualMenuListener = actualMenuListener;
		this.menuEnableSupport = new ActionEnableSupport(actualMenuListener, enableFor, appMgr, networkViewManager);
	}

	public void menuSelected(MenuEvent m) {
		menuEnableSupport.updateEnableState();
		if ( !menuEnableSupport.isCurrentlyEnabled() )
			return;

		actualMenuListener.menuSelected(m);
	}

	public void menuDeselected(MenuEvent m) {
		actualMenuListener.menuDeselected(m);
	}

	public void menuCanceled(MenuEvent m) {
		actualMenuListener.menuCanceled(m);
	}
}
