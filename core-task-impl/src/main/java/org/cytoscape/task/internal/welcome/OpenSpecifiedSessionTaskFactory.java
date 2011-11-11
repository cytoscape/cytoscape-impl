package org.cytoscape.task.internal.welcome;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class OpenSpecifiedSessionTaskFactory implements TaskFactory {

	private CySessionManager mgr;
	private CySessionReaderManager rmgr;

	private final CyApplicationManager appManager;
	
	private URI sessionFileURI;

	public OpenSpecifiedSessionTaskFactory(CySessionManager mgr, final CySessionReaderManager rmgr,
			final CyApplicationManager appManager) {
		this.mgr = mgr;
		this.rmgr = rmgr;
		this.appManager = appManager;
	}
	
	public void setFileLocation(final URL url) throws URISyntaxException {
		this.sessionFileURI = url.toURI();
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(2,new OpenSpecifiedSessionTask(sessionFileURI, mgr, rmgr, appManager));
	}
}