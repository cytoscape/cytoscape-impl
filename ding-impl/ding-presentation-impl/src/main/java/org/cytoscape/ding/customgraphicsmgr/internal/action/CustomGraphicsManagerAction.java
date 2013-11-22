package org.cytoscape.ding.customgraphicsmgr.internal.action;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphicsmgr.internal.ui.CustomGraphicsBrowser;
import org.cytoscape.ding.customgraphicsmgr.internal.ui.CustomGraphicsManagerDialog;


public class CustomGraphicsManagerAction extends AbstractCyAction {
	private static final long serialVersionUID = -4582671383878015609L;
	private final CustomGraphicsManagerDialog dialog;

	public CustomGraphicsManagerAction(final CustomGraphicsManager manager,
	                                   final CyApplicationManager applicationManager, final CustomGraphicsBrowser browser)
	{
		super("Open Custom Graphics Manager");
		setPreferredMenu("View");
		setMenuGravity(10.0f);
		
		this.dialog = new CustomGraphicsManagerDialog(manager, applicationManager, browser);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.dialog.setVisible(true);
	}

}
