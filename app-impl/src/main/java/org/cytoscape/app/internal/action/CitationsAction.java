package org.cytoscape.app.internal.action;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.ui.CitationsDialog;
import org.cytoscape.app.internal.util.Utils;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskManager;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2016 The Cytoscape Consortium
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
public class CitationsAction extends AbstractCyAction {
	
	final WebQuerier webQuerier;
	final AppManager appMgr;
	final TaskManager taskMgr;
	final CySwingApplication swingApp;
	final OpenBrowser openBrowser;

	CitationsDialog dialog;

	public CitationsAction(WebQuerier webQuerier, AppManager appMgr, TaskManager taskMgr, CySwingApplication swingApp,
			OpenBrowser openBrowser) {
		super("Citations...");
		super.setPreferredMenu("Help");
		super.setMenuGravity(2.0f);

		this.webQuerier = webQuerier;
		this.appMgr = appMgr;
		this.taskMgr = taskMgr;
		this.swingApp = swingApp;
		this.openBrowser = openBrowser;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (dialog == null) {
			final Window owner = Utils.getWindowAncestor(e, swingApp);
			dialog = new CitationsDialog(webQuerier, appMgr, taskMgr, owner, openBrowser);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					dialog = null;
				}
			});
		}
		
		dialog.setVisible(true);
	}
	
	@Override
	public boolean isEnabled() {
		return dialog == null || !dialog.isVisible();
	}
}