package org.cytoscape.task.internal.session;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.internal.utils.SessionUtils;
import org.cytoscape.task.write.SaveSessionTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SaveSessionTaskFactoryImpl extends AbstractTaskFactory implements SaveSessionTaskFactory {

	private final CySessionManager sessionMgr;
	private final CySessionWriterManager writerMgr;
	private final RecentlyOpenedTracker tracker;
	private final CyEventHelper cyEventHelper;
	private final SessionUtils sessionUtils;
	
	public SaveSessionTaskFactoryImpl(CySessionWriterManager writerMgr, CySessionManager sessionMgr,
			final RecentlyOpenedTracker tracker, final CyEventHelper cyEventHelper, final SessionUtils sessionUtils) {
		this.sessionMgr = sessionMgr;
		this.writerMgr = writerMgr;
		this.tracker = tracker;
		this.cyEventHelper = cyEventHelper;
		this.sessionUtils = sessionUtils;
	}

	@Override
	public TaskIterator createTaskIterator() {
		// Check session file name is set or not.
		final String sessionFileName = sessionMgr.getCurrentSessionFileName();		
		
		// If there is no file name, use Save As task.  Otherwise, overwrite the current session.
		if (sessionFileName == null)
			return new TaskIterator(new SaveSessionAsTask(writerMgr, sessionMgr, tracker, cyEventHelper));
		else
			return new TaskIterator(new SaveSessionTask(writerMgr, sessionMgr, tracker, cyEventHelper));
	}
	
	@Override
	public boolean isReady() {
		return sessionUtils.isSessionReady();
	}
}
