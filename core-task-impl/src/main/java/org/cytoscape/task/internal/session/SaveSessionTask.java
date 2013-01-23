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

import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SaveSessionTask extends AbstractTask {

	private final CySessionWriterManager writerMgr;
	private final RecentlyOpenedTracker tracker;
	private final CySessionManager sessionMgr;

	/**
	 * setAcceleratorCombo(KeyEvent.VK_S, ActionEvent.CTRL_MASK);
	 */
	public SaveSessionTask(final CySessionWriterManager writerMgr,
						   final CySessionManager sessionMgr,
						   final RecentlyOpenedTracker tracker) {
		super();

		if (writerMgr == null)
			throw new NullPointerException("CySessionWriterManager is null.");
		if (sessionMgr == null)
			throw new NullPointerException("CySessionManager is null.");
		
		this.writerMgr = writerMgr;
		this.sessionMgr = sessionMgr;
		this.tracker = tracker;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.05);

		final CySession session = sessionMgr.getCurrentSession();
		final String fileName = sessionMgr.getCurrentSessionFileName();
		
		File file = new File(fileName);
		insertTasksAfterCurrentTask(new CySessionWriter(writerMgr, session, file));
		
		// Add this session file URL as the most recent file.
		if (!file.getName().endsWith(".cys"))
			file = new File(file.getPath() + ".cys");
		
		tracker.add(file.toURI().toURL());
		taskMonitor.setProgress(1.0);
	}
}