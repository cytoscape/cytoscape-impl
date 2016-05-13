package org.cytoscape.jobs.internal;

/*
 * #%L
 * Cytoscape Jobs Impl (jobs-impl)
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

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobMonitor;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.property.PropertyUpdatedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import static org.cytoscape.work.ServiceProperties.*;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.StatusBarPanelFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		CyEventHelper cyEventHelper = getService(bc,CyEventHelper.class);

		CyJobMonitor jobMonitor;

		// See if we have a graphics console or not
		boolean haveGUI = true;
		ServiceReference ref = 
			bc.getServiceReference("org.cytoscape.application.swing.CySwingApplication");

		CyJobManagerImpl cyJobManager = new CyJobManagerImpl(cyServiceRegistrarServiceRef, cyEventHelper);
		registerService(bc,cyJobManager,CyJobManager.class, new Properties());

		if (ref == null) {
			haveGUI = false;
			jobMonitor = new SimpleCyJobMonitor();
		} else {
			// So, if we have a GUI, create and register our status bar
			JobStatusBar statusBar = new JobStatusBar(cyServiceRegistrarServiceRef);
			Properties statusBarProperties = new Properties();
			statusBarProperties.setProperty("type", "JobStatus");
			registerService(bc,statusBar,StatusBarPanelFactory.class, statusBarProperties);
			
			// So, if we have a GUI, start up our jobs monitor
			jobMonitor = new GUICyJobMonitor(cyServiceRegistrarServiceRef, cyJobManager, statusBar);
			Properties guiJobProperties = new Properties();
			guiJobProperties.setProperty(TITLE, "Job Status Monitor");
			guiJobProperties.setProperty(PREFERRED_MENU, "Tools");
			guiJobProperties.setProperty(IN_TOOL_BAR, "true");
			registerService(bc,jobMonitor,TaskFactory.class, guiJobProperties);
		}

		// Our job manager also needs to handle the registration of jobs handlers and job session handlers
		registerServiceListener(bc, cyJobManager, "addJobMonitor", "removeJobMonitor", CyJobMonitor.class);
		registerServiceListener(bc, cyJobManager, "addExecutionService", "removeExecutionService", CyJobExecutionService.class);

		// Our job manager also needs to know about session load and save
		registerService(bc,cyJobManager,SessionAboutToBeSavedListener.class, new Properties());
		registerService(bc,cyJobManager,SessionLoadedListener.class, new Properties());

	}

}

