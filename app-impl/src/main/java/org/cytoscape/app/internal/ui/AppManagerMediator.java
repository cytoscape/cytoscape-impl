package org.cytoscape.app.internal.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.app.internal.util.Utils;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;

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

public class AppManagerMediator implements CyShutdownListener {

	private AppManagerDialog dialog;
	
	private final AppManager appManager;
	private final DownloadSitesManager downloadSitesManager;
	private final UpdateManager updateManager;
	private final CyServiceRegistrar serviceRegistrar;
	
	public AppManagerMediator(
			AppManager appManager,
			DownloadSitesManager downloadSitesManager,
			UpdateManager updateManager,
			CyServiceRegistrar serviceRegistrar
	) {
		this.appManager = appManager;
		this.downloadSitesManager = downloadSitesManager;
		this.updateManager = updateManager;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void handleEvent(CyShutdownEvent e) {
		if (dialog != null) {	
			dialog.setVisible(false);
			dialog.dispose();
			dialog = null;
		}
	}
	
	public void showAppManager(boolean goToUpdates, ActionEvent evt) {
		// Create and display the App Manager dialog
		if (dialog == null) {
			final CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);
			
			final Window owner = Utils.getWindowAncestor(evt, swingApplication);
			dialog = new AppManagerDialog(owner, appManager, downloadSitesManager, updateManager, serviceRegistrar);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					dialog = null;
				}
			});
		}
		
		if (goToUpdates)
			dialog.changeTab(2);
		
		dialog.setVisible(true);
	}
	
	public boolean isAppManagerVisible() {
		return dialog != null && dialog.isVisible();
	}
}
