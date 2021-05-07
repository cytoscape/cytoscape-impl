package org.cytoscape.task.internal.session;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class NewSessionTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "New Session";
	}
	
	@Tunable(
			description = "Deprecated",
			longDescription = "Deprecated since version 3.6.",
			context = "nogui"
	)
	@Deprecated
	public boolean destroyCurrentSession = true;
	
	@Tunable(
			description = "<html>Current session (all networks and tables) will be lost.<br />Do you want to continue?</html>",
			params = "ForceSetDirectly=true;ForceSetTitle=New Session",
			context = "gui"
	)
	public boolean confirm = true;

	private final CyServiceRegistrar serviceRegistrar;
	
	public NewSessionTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (confirm && destroyCurrentSession) { // Also checks destroyCurrentSession for backwards compatibility
			tm.setTitle("Create New Session");
			tm.setProgress(0.0);
			
			final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			final CySessionManager sessionManager = serviceRegistrar.getService(CySessionManager.class);
			
			// Let everybody know the current session will be destroyed
			eventHelper.fireEvent(new SessionAboutToBeLoadedEvent(this));
			tm.setProgress(0.1);
			
			// Dispose the current session before loading the new one
			sessionManager.disposeCurrentSession();
			tm.setProgress(0.2);
			
			sessionManager.setCurrentSession(null, null);
			tm.setProgress(1.0);
		}
	}
}
