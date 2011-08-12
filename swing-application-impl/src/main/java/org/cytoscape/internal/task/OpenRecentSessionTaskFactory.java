package org.cytoscape.internal.task;

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
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenRecentSessionTaskFactory implements TaskFactory {
	
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

	public TaskIterator getTaskIterator() {
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

			final CySession newSession = reader.getCySession();
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
