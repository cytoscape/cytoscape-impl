package org.cytoscape.task.internal.session;

import java.io.File;
import java.net.URI;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.view.presentation.RenderingEngine;
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

/**
 * Call the session reader and read everything in the zip archive.<br>
 * setAcceleratorCombo(java.awt.event.KeyEvent.VK_O, ActionEvent.CTRL_MASK);
 */
public class OpenSessionCommandTask extends AbstractOpenSessionTask {

	@ProvidesTitle
	public String getTitle() {
		return "Open Session";
	}
	
	@Tunable(description="Session file to load:", params="fileCategory=session;input=true")
	public File file;
	@Tunable(description="URL from which to load the session file:", params="fileCategory=session;input=true")
	public String url;

	/**
	 * Constructor.<br>
	 * Add a menu item under "File" and set shortcut.
	 */
	public OpenSessionCommandTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		final CySessionManager sessionManager = serviceRegistrar.getService(CySessionManager.class);
		
		try {
			try {
				tm.setTitle("Open Session");
				tm.setStatusMessage("Opening Session File.\n\nIt may take a while.\nPlease wait...");
				tm.setProgress(0.0);
		
				if (file == null && (url == null || url.trim().isEmpty()))
					throw new NullPointerException("No file or URL specified.");
				
				final CySessionReaderManager readerMgr = serviceRegistrar.getService(CySessionReaderManager.class);
				
				if (file != null)
					reader = readerMgr.getReader(file.toURI(), file.getName());
				else
					reader = readerMgr.getReader(new URI(url.trim()), url);
				
				if (reader == null)
					throw new NullPointerException("Failed to find appropriate reader for file: " + file);
				
				// Let everybody know the current session will be destroyed
				eventHelper.fireEvent(new SessionAboutToBeLoadedEvent(this));
				tm.setProgress(0.1);
				
				// Dispose the current session before loading the new one
				serviceRegistrar.getService(CySessionManager.class).disposeCurrentSession();
				tm.setProgress(0.2);
				
				// Now we can read the new session
				if (!cancelled)
					reader.run(tm);
				
				tm.setProgress(0.8);
			} catch (Exception e) {
				disposeCancelledSession(e, sessionManager);
				throw e;
			}
			
			if (cancelled)
				disposeCancelledSession(null, sessionManager);
			else
				changeCurrentSession(sessionManager, tm);
		} finally {
			// plug big memory leak
			reader = null;
		}
	}
		
	private void changeCurrentSession(CySessionManager sessionManager, TaskMonitor tm) throws Exception {
		final CySession newSession = reader.getSession();
		
		if (newSession == null)
			throw new NullPointerException("Session could not be read for file: " + file);

		String fileName = file != null ? file.getAbsolutePath() : new URI(url).getPath().replace("/", "");
		sessionManager.setCurrentSession(newSession, fileName);
		
		// Set Current network: this is necessary to update GUI
		final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		final RenderingEngine<CyNetwork> currentEngine = appManager.getCurrentRenderingEngine();
		
		if (currentEngine != null)
			appManager.setCurrentRenderingEngine(currentEngine);
		
		tm.setProgress(1.0);
		tm.setStatusMessage("Session file " + fileName + " successfully loaded.");
		
		// Add this session file URL as the most recent file.
		if (file != null)
			serviceRegistrar.getService(RecentlyOpenedTracker.class).add(file.toURI().toURL());
	}
}
