package org.cytoscape.task.internal.session;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionLoadCancelledEvent;
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
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
	
	@Tunable(description="<html>Current session (all networks and tables) will be lost.<br />Do you want to continue?</html>", params="ForceSetDirectly=true;ForceSetTitle=New Session")
	public boolean destroyCurrentSession = true;

	private final CySessionManager mgr;
	private final CyEventHelper eventHelper;
	
	
	public NewSessionTask(final CySessionManager mgr, final CyEventHelper eventHelper) {
		this.mgr = mgr;
		this.eventHelper = eventHelper;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (destroyCurrentSession) {
			taskMonitor.setProgress(0.0);
			
			// Let everybody know the current session will be destroyed
			eventHelper.fireEvent(new SessionAboutToBeLoadedEvent(this));
			taskMonitor.setProgress(0.1);
			
			// Dispose the current session before loading the new one
			mgr.disposeCurrentSession();
			taskMonitor.setProgress(0.2);
			
			try {
				mgr.setCurrentSession(null, null);
			} catch (Exception e) {
				eventHelper.fireEvent(new SessionLoadCancelledEvent(this, e));
				throw e;
			}
			
			taskMonitor.setProgress(1.0);
		}
	}
	
	@Override
	public void cancel() {
		super.cancel();
		eventHelper.fireEvent(new SessionLoadCancelledEvent(this));
	}
}
