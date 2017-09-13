package org.cytoscape.task.internal.session;

import java.io.File;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionSaveCancelledEvent;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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

public class SaveSessionTask extends AbstractTask {

	private CySessionWriter writer;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	/**
	 * setAcceleratorCombo(KeyEvent.VK_S, ActionEvent.CTRL_MASK);
	 */
	public SaveSessionTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CySession session = null;
		File file = null;
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		
		try {
			final CySessionManager sessionMgr = serviceRegistrar.getService(CySessionManager.class);
			taskMonitor.setProgress(0.05);
	
			session = sessionMgr.getCurrentSession();
			final String fileName = sessionMgr.getCurrentSessionFileName();
			
			file = new File(fileName);
			writer = new CySessionWriter(session, file, serviceRegistrar);
			writer.run(taskMonitor);
			
			taskMonitor.setProgress(1.0);
			
			// Add this session file URL as the most recent file.
			if (!file.getName().endsWith(".cys"))
				file = new File(file.getPath() + ".cys");
		} catch (Exception e) {
			eventHelper.fireEvent(new SessionSaveCancelledEvent(this));
			throw e;
		}
		
		if (!cancelled) {
			// Fire event to tell others session has been saved to a file.
			eventHelper.fireEvent(new SessionSavedEvent(this, session, file.getAbsolutePath()));
			serviceRegistrar.getService(RecentlyOpenedTracker.class).add(file.toURI().toURL());
		}
	}
	
	@Override
	public void cancel() {
		super.cancel();
		
		if (writer != null)
			writer.cancel();
	}
}