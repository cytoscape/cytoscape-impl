package org.cytoscape.task.internal.welcome;

import java.io.File;
import java.net.URI;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class OpenSpecifiedSessionTask extends AbstractTask {
	
	private final CySessionManager sessionMgr;
	private final CySessionReaderManager readerMgr;
	
	private final CyApplicationManager appManager;

	private final URI targetFile;
	
	private File file;
	
	public OpenSpecifiedSessionTask(final URI targetFile, final CySessionManager mgr, final CySessionReaderManager readerManager,
			final CyApplicationManager appManager) {
		this.sessionMgr = mgr;
		this.readerMgr = readerManager;
		this.appManager = appManager;
		this.targetFile = targetFile;
		
		if(targetFile == null)
			throw new NullPointerException("File location is null");
		
		file = new File(targetFile);
	}

	/**
	 * Clear current session and open the cys file.
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {

		taskMonitor.setStatusMessage("Opening Session File.\n\nIt may take a while.\nPlease wait...");
		taskMonitor.setProgress(0.0);
		
		CySessionReader reader = readerMgr.getReader(targetFile, targetFile.toString());
		taskMonitor.setProgress(0.1);
		if (reader == null)
			throw new NullPointerException("Failed to find appropriate reader for URI: " + targetFile);
		reader.run(taskMonitor);

		if (cancelled)
			return;
		insertTasksAfterCurrentTask(new LoadSessionTask(reader));
		taskMonitor.setProgress(1.0);
	}
	
	
	private final class LoadSessionTask extends AbstractTask {
		CySessionReader reader;
		
		LoadSessionTask(CySessionReader reader) {
			this.reader = reader;
		}
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			final CySession newSession = reader.getCySession();
			if ( newSession == null )
				throw new NullPointerException("Session could not be read for file: " + file);

			sessionMgr.setCurrentSession(newSession, file.getAbsolutePath());
			
			// Set Current network: this is necessary to update GUI.
			final RenderingEngine<CyNetwork> currentEngine = appManager.getCurrentRenderingEngine();
			if(currentEngine != null)
				appManager.setCurrentRenderingEngine(currentEngine);
			
			taskMonitor.setProgress(1.0);
			taskMonitor.setStatusMessage("Session file " + file + " successfully loaded.");
		
		}
	}

}
