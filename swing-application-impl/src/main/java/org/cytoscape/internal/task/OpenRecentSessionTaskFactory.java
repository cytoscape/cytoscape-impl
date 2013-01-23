package org.cytoscape.internal.task;

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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenRecentSessionTaskFactory extends AbstractTaskFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(OpenRecentSessionTaskFactory.class);

	private final CySessionManager sessionManager;
	private final CySessionReaderManager readerManager;
	private final CyApplicationManager appManager;
	private final RecentlyOpenedTracker tracker;
	
	private final URL targetSession;

	
	public OpenRecentSessionTaskFactory(final CySessionManager sessionManager, final CySessionReaderManager readerManager,
			final CyApplicationManager appManager, final RecentlyOpenedTracker tracker, final URL targetSession) {
		this.sessionManager = sessionManager;
		this.readerManager = readerManager;
		this.appManager = appManager;
		this.tracker = tracker;
		
		this.targetSession = targetSession;
	}

	public TaskIterator createTaskIterator() {
		CySessionReader reader = null;
		try {
			reader = readerManager.getReader(targetSession.toURI(), targetSession.toString());
		} catch (URISyntaxException e) {
			throw new IllegalStateException("URL is invalid.", e);
		}
		
		if(reader == null)
			throw new NullPointerException("Could not find reader.");
		
		return new TaskIterator(new LoadSessionFromURLTask(reader), new LoadRecentSessionTask(reader));
	}
	
	private final class LoadSessionFromURLTask extends AbstractTask {

		private final CySessionReader reader;
		
		LoadSessionFromURLTask(final CySessionReader reader) {
			logger.debug("First, load the session file");
			this.reader = reader;
		}
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			reader.run(taskMonitor);
		}
		
	}

	private final class LoadRecentSessionTask extends AbstractTask {

		private final CySessionReader reader;

		LoadRecentSessionTask(final CySessionReader reader) {
			this.reader = reader;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			logger.debug("Post processiong for session...");

			final CySession newSession = reader.getSession();
			if (newSession == null)
				throw new NullPointerException("Session could not be read for file: " + targetSession.toString());

			final File file = new File(targetSession.toURI());
			sessionManager.setCurrentSession(newSession, file.getAbsolutePath());

			// Set Current network: this is necessary to update GUI.
			final RenderingEngine<CyNetwork> currentEngine = appManager.getCurrentRenderingEngine();
			if (currentEngine != null)
				appManager.setCurrentRenderingEngine(currentEngine);

			taskMonitor.setProgress(1.0);
			taskMonitor.setStatusMessage("Session file " + file + " successfully loaded.");

			// Add this session file URL as the most recent file.
			tracker.add(targetSession);
		}
	}

}
