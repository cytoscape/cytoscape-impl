/*
 File: OpenSessionTask.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.task.internal.session; 


import java.io.File;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


/**
 * Call the session reader and read everything in the zip archive.<br>
 * setAcceleratorCombo(java.awt.event.KeyEvent.VK_O, ActionEvent.CTRL_MASK);
 */
public class OpenSessionTask extends AbstractTask {

	@Tunable(description="Session file to load", params="fileCategory=session;input=true")
	public File file;
	
	private final CySessionManager sessionMgr;
	private final CySessionReaderManager readerMgr;
	
	private final CyApplicationManager appManager;
	private final RecentlyOpenedTracker tracker;
	
	private CySessionReader reader;

	/**
	 * Constructor.<br>
	 * Add a menu item under "File" and set shortcut.
	 */
	public OpenSessionTask(final CySessionManager mgr, final CySessionReaderManager readerManager,
			final CyApplicationManager appManager, final RecentlyOpenedTracker tracker) {
		this.sessionMgr = mgr;
		this.readerMgr = readerManager;
		this.appManager = appManager;
		this.tracker = tracker;
	}

	/**
	 * Clear current session and open the cys file.
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {

		taskMonitor.setStatusMessage("Opening Session File.\n\nIt may take a while.\nPlease wait...");
		taskMonitor.setProgress(0.0);

		if ( file == null )
			throw new NullPointerException("No file specified!");
		
		reader = readerMgr.getReader(file.toURI(),file.getName());
		if (reader == null)
			throw new NullPointerException("Failed to find appropriate reader for file: " + file);
		taskMonitor.setProgress(0.2);
		reader.run(taskMonitor);
		taskMonitor.setProgress(0.8);
		if (cancelled)
			return;

		insertTasksAfterCurrentTask(new LoadSessionTask(reader));
		taskMonitor.setProgress(1.0);
	}
	
	CySession getCySession() {
		return reader.getSession();
	}
	
	
	private final class LoadSessionTask extends AbstractTask {
		CySessionReader reader;
		
		LoadSessionTask(CySessionReader reader) {
			this.reader = reader;
		}
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			final CySession newSession = reader.getSession();
			if ( newSession == null )
				throw new NullPointerException("Session could not be read for file: " + file);

			sessionMgr.setCurrentSession(newSession, file.getAbsolutePath());
			
			// Set Current network: this is necessary to update GUI.
			final RenderingEngine<CyNetwork> currentEngine = appManager.getCurrentRenderingEngine();
			if(currentEngine != null)
				appManager.setCurrentRenderingEngine(currentEngine);
			
			taskMonitor.setProgress(1.0);
			taskMonitor.setStatusMessage("Session file " + file + " successfully loaded.");
			
			// Add this session file URL as the most recent file.
			tracker.add(file.toURI().toURL());
		}
	}
}