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
import org.cytoscape.util.swing.IconManager;


public class CustomGraphicsManagerAction extends AbstractCyAction {
	
	private static final long serialVersionUID = -4582671383878015609L;
	
	private CustomGraphicsManagerDialog dialog;

	private final CustomGraphicsManager cgManager;
	private final CyApplicationManager appManager;
	private final CustomGraphicsBrowser browser;
	private final IconManager iconManager;

	public CustomGraphicsManagerAction(final CustomGraphicsManager cgManager,
	                                   final CyApplicationManager appManager,
	                                   final CustomGraphicsBrowser browser,
	                                   final IconManager iconManager) {
		super("Open Custom Graphics Manager");
		setPreferredMenu("View");
		setMenuGravity(10.0f);
		
		this.cgManager = cgManager;
		this.appManager = appManager;
		this.browser = browser;
		this.iconManager = iconManager;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (dialog == null)
			dialog = new CustomGraphicsManagerDialog(cgManager, appManager, browser, iconManager);
		
		dialog.setVisible(true);
	}
}
