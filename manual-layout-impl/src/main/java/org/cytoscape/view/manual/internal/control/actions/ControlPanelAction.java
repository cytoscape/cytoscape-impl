package org.cytoscape.view.manual.internal.control.actions;

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


import org.cytoscape.view.manual.internal.common.AbstractManualLayoutAction;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;

/**
 * Action for the Align and Distribute functionality. 
 */
public class ControlPanelAction extends AbstractManualLayoutAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 949048793512867597L;

	/**
 	 * Action for the Align and Distribute functionality. Should be in menu slot 2.
	 */
	public ControlPanelAction(CytoPanelComponent comp, CySwingApplication swingApp, CyApplicationManager appMgr, final CyNetworkViewManager networkViewManager) {
		super(comp, swingApp, appMgr, networkViewManager, 1.2f);
	}
}
