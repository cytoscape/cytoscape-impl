package org.cytoscape.internal;

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

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.CyVersion;
import org.cytoscape.internal.view.help.HelpAboutTaskFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

public class MacCyActivator extends AbstractCyActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		final CyShutdown shutdown = getService(context, CyShutdown.class);
		final CyVersion version = getService(context, CyVersion.class);
		final TaskFactory aboutTaskFactory = new HelpAboutTaskFactory(version);
		final DialogTaskManager taskManager = getService(context,DialogTaskManager.class);
		
		Application application = Application.getApplication();
		application.setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent event, QuitResponse response) {
				shutdown.exit(0);
			}
		});
		application.setAboutHandler(new AboutHandler() {
			@Override
			public void handleAbout(AboutEvent event) {
				taskManager.execute(aboutTaskFactory.createTaskIterator());
			}
		});
	}
}
