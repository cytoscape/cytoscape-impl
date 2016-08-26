package org.cytoscape.jobs.internal;

import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.jobs.CyJobMonitor;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.StatusBarPanelFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		// See if we have a graphics console or not
		ServiceReference ref = bc.getServiceReference("org.cytoscape.application.swing.CySwingApplication");

		CyJobManagerImpl jobManager = new CyJobManagerImpl();
		registerService(bc, jobManager, CyJobManager.class, new Properties());
		
		final CyJobMonitor jobMonitor;

		if (ref == null) {
			// No GUI...
			jobMonitor = new SimpleCyJobMonitor();
		} else {
			// So, if we have a GUI, create and register our status bar
			JobStatusBar statusBar = new JobStatusBar(serviceRegistrar);
			Properties statusBarProperties = new Properties();
			statusBarProperties.setProperty("type", "JobStatus");
			registerService(bc, statusBar, StatusBarPanelFactory.class, statusBarProperties);

			// So, if we have a GUI, start up our jobs monitor
			jobMonitor = new GUICyJobMonitor(serviceRegistrar, jobManager, statusBar);
			Properties guiJobProperties = new Properties();
			guiJobProperties.setProperty(TITLE, "Job Status Monitor");
			guiJobProperties.setProperty(PREFERRED_MENU, "Tools");
			guiJobProperties.setProperty(IN_TOOL_BAR, "true");
			registerService(bc, jobMonitor, TaskFactory.class, guiJobProperties);
		}

		// Our job manager also needs to handle the registration of jobs handlers and job session handlers
		registerServiceListener(bc, jobManager, "addJobMonitor", "removeJobMonitor", CyJobMonitor.class);
		registerServiceListener(bc, jobManager, "addExecutionService", "removeExecutionService", CyJobExecutionService.class);

		// Our job manager also needs to know about session load and save
		registerService(bc, jobManager, SessionAboutToBeSavedListener.class, new Properties());
		registerService(bc, jobManager, SessionLoadedListener.class, new Properties());
	}
}

