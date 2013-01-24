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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class OpenSessionTaskFactoryImpl extends AbstractTaskFactory implements OpenSessionTaskFactory {

	private CySessionManager mgr;
	private CySessionReaderManager rmgr;

	private final CyApplicationManager appManager;
	private final RecentlyOpenedTracker tracker;

	private final SynchronousTaskManager<?> syncTaskManager;
	private final TunableSetter tunableSetter; 
	
	private OpenSessionTask task;

	public OpenSessionTaskFactoryImpl(CySessionManager mgr, final CySessionReaderManager rmgr,
			final CyApplicationManager appManager, final RecentlyOpenedTracker tracker,
			final SynchronousTaskManager<?> syncTaskManager, final TunableSetter tunableSetter) {
		this.mgr = mgr;
		this.rmgr = rmgr;
		this.appManager = appManager;
		this.tracker = tracker;
		this.syncTaskManager = syncTaskManager;
		this.tunableSetter = tunableSetter;
	}

	public TaskIterator createTaskIterator() {
		task = new OpenSessionTask(mgr, rmgr, appManager, tracker);
		return new TaskIterator(2, task);
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("file", file);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 
	}

}
