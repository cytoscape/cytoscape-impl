package org.cytoscape.internal;

import java.util.Properties;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.internal.view.help.HelpAboutTaskFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitResponse;

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

public class MacCyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext context) throws Exception {
		final CyServiceRegistrar serviceRegistrar = getService(context, CyServiceRegistrar.class);
		final CyShutdown shutdown = getService(context, CyShutdown.class);
		final TaskFactory aboutTaskFactory = new HelpAboutTaskFactory(serviceRegistrar);
		final DialogTaskManager taskManager = getService(context,DialogTaskManager.class);
		
		final CyShutdownEvent[] lastShutdownEvent = new CyShutdownEvent[1];
		CyShutdownListener listener = (CyShutdownEvent e) -> {
			lastShutdownEvent[0] = e;
		};
		registerService(context, listener, CyShutdownListener.class, new Properties());
		
		Application application = Application.getApplication();
		application.setQuitHandler((QuitEvent event, QuitResponse response) -> {
			shutdown.exit(0);
			if (lastShutdownEvent[0] != null && !lastShutdownEvent[0].actuallyShutdown()) {
				response.cancelQuit();
			}
		});
		application.setAboutHandler((AboutEvent event) -> {
			taskManager.execute(aboutTaskFactory.createTaskIterator());
		});
	}
}
