package org.cytoscape.task.internal.session;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;

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
public class OpenSessionTask extends AbstractOpenSessionTask {

	@ProvidesTitle
	public String getTitle() {
		return "Open Session";
	}
	
	private final File sessionFile;

	public OpenSessionTask(CyServiceRegistrar serviceRegistrar) {
		this(null, serviceRegistrar);
	}
	
	public OpenSessionTask(File sessionFile, CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		this.sessionFile = sessionFile;
	}

	/**
	 * Clear current session and open the cys file.
	 */
	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
		final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
		
		if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
			loadSession(this);
		else
			insertTasksAfterCurrentTask(new OpenSessionWithWarningTask());
	}
	
	private void loadSession(AbstractTask currentTask) {
		if (sessionFile == null) {
			// Should use Tunables to show a file chooser and let the user select the file
			getTaskIterator().insertTasksAfter(currentTask, new OpenSessionWithoutWarningTask());
		} else {
			// Should not show the Tunables dialog
			final Map<String, Object> m = new HashMap<>();
			m.put("file", sessionFile);
	
			final TunableSetter tunableSetter = serviceRegistrar.getService(TunableSetter.class);
			getTaskIterator().insertTasksAfter(currentTask,
					tunableSetter.createTaskIterator(new TaskIterator(new OpenSessionWithoutWarningTask()), m));
		}
	}
	
	public final class OpenSessionWithoutWarningTask extends AbstractTask {
		
		@Tunable(description="Session file to load:", params="fileCategory=session;input=true", context="gui")
		public File file;
		
		@Override
		public void run(final TaskMonitor tm) throws Exception {
			final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			final CySessionManager sessionManager = serviceRegistrar.getService(CySessionManager.class);
			
			try {
				try {
					tm.setStatusMessage("Opening Session File.\n\nIt may take a while.\nPlease wait...");
					tm.setProgress(0.0);
		
					if (file == null)
						throw new NullPointerException("No file specified.");
					
					reader = serviceRegistrar.getService(CySessionReaderManager.class)
							.getReader(file.toURI(), file.getName());
					
					if (reader == null)
						throw new NullPointerException("Failed to find appropriate reader for file: " + file);
					
					// Let everybody know the current session will be destroyed
					eventHelper.fireEvent(new SessionAboutToBeLoadedEvent(this));
					tm.setProgress(0.1);
					
					// Dispose the current session before loading the new one
					sessionManager.disposeCurrentSession();
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

			sessionManager.setCurrentSession(newSession, file.getAbsolutePath());
			
			tm.setProgress(1.0);
			tm.setStatusMessage("Session file " + file + " successfully loaded.");
			
			// Add this session file URL as the most recent file.
			serviceRegistrar.getService(RecentlyOpenedTracker.class).add(file.toURI().toURL());
		}
	}
	
	public final class OpenSessionWithWarningTask extends AbstractTask {
		
		@Tunable(
				description="<html>Current session (all networks and tables) will be lost.<br />Do you want to continue?</html>",
				params = "ForceSetDirectly=true;ForceSetTitle=Open Session",
				context = "gui"
		)
		public boolean loadSession;
		
		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			if (loadSession)
				loadSession(this);
		}
	}
}