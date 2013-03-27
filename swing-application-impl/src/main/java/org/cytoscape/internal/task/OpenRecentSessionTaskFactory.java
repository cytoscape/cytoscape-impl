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
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenRecentSessionTaskFactory extends AbstractTaskFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(OpenRecentSessionTaskFactory.class);

	private final CySessionManager sessionManager;
	private final CySessionReaderManager readerManager;
	private final CyApplicationManager appManager;
	private final CyNetworkManager netManager;
	private final CyTableManager tableManager;
	private final CyNetworkTableManager netTableManager;
	private final CyGroupManager grManager;
	private final RecentlyOpenedTracker tracker;
	private final URL targetSession;
	
	private Set<CyNetwork> currentNetworkSet;
	private Set<CyGroup> currentGroupSet;

	
	public OpenRecentSessionTaskFactory(final CySessionManager sessionManager,
										final CySessionReaderManager readerManager,
										final CyApplicationManager appManager,
										final CyNetworkManager netManager,
										final CyTableManager tableManager,
										final CyNetworkTableManager netTableManager,
										final CyGroupManager grManager,
										final RecentlyOpenedTracker tracker,
										final URL targetSession) {
		this.sessionManager = sessionManager;
		this.readerManager = readerManager;
		this.appManager = appManager;
		this.netManager = netManager;
		this.tableManager = tableManager;
		this.netTableManager = netTableManager;
		this.grManager = grManager;
		this.tracker = tracker;
		this.targetSession = targetSession;
	}

	@Override
	public TaskIterator createTaskIterator() {
		CySessionReader reader = null;
		
		try {
			reader = readerManager.getReader(targetSession.toURI(), targetSession.toString());
		} catch (URISyntaxException e) {
			throw new IllegalStateException("URL is invalid.", e);
		}
		
		if (reader == null)
			throw new NullPointerException("Could not find reader.");
		
		return new TaskIterator(new LoadSessionFromURLTask(reader));
	}
	
	private final class LoadSessionFromURLTask extends AbstractTask {

		private final CySessionReader reader;
		
		LoadSessionFromURLTask(final CySessionReader reader) {
			logger.debug("First, load the session file");
			this.reader = reader;
		}
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// Save the current network and group set, in case loading the new session is cancelled later
			currentNetworkSet = new HashSet<CyNetwork>(netTableManager.getNetworkSet());
			currentGroupSet = new HashSet<CyGroup>();
			
			for (final CyNetwork n : currentNetworkSet)
				currentGroupSet.addAll(grManager.getGroupSet(n));
			
			reader.run(taskMonitor);
			
			if (cancelled)
				return;

			if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
				insertTasksAfterCurrentTask(new LoadSessionWithoutWarningTask());
			else
				insertTasksAfterCurrentTask(new LoadSessionWithWarningTask());
		}
		
		@Override
		public void cancel() {
			super.cancel();
			
			if (reader != null)
				reader.cancel(); // Remember to cancel the Session Reader!
		}
		
		private void changeCurrentSession(TaskMonitor taskMonitor) throws Exception {
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
		
		private synchronized void disposeCancelledSession() {
			final CySession newSession = reader.getSession();
			
			if (newSession != null) {
				for (final CyNetworkView view : newSession.getNetworkViews())
					view.dispose();
			}
			
			if (currentNetworkSet != null) {
				// Dispose cancelled networks and groups:
				// This is necessary because the new CySession contains only registered networks;
				// unregistered networks (e.g. CyGroup networks) may have been loaded and need to be disposed as well.
				// The Network Table Manager should contain all networks, including the unregistered ones.
				final Set<CyNetwork> newNetworkSet = new HashSet<CyNetwork>(netTableManager.getNetworkSet());
				
				for (final CyNetwork net : newNetworkSet) {
					if (!currentNetworkSet.contains(net)) {
						for (final CyGroup gr : grManager.getGroupSet(net)) {
							if (currentGroupSet != null && !currentGroupSet.contains(gr))
								grManager.destroyGroup(gr);
						}
						
						net.dispose();
					}
				}
				
				currentGroupSet = null;
				currentNetworkSet = null;
			}
		}
		
		public final class LoadSessionWithWarningTask extends AbstractTask {
			
			@Tunable(description="<html>Current session (all networks and tables) will be lost.<br />Do you want to continue?</html>", params="ForceSetDirectly=true")
			public boolean changeCurrentSession = true;
			
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				if (changeCurrentSession) 
					changeCurrentSession(taskMonitor);
				else
					disposeCancelledSession();
			}

			@Override
			public void cancel() {
				super.cancel();
				disposeCancelledSession();
			}
		}
		
		public final class LoadSessionWithoutWarningTask extends AbstractTask {
			
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				changeCurrentSession(taskMonitor);
			}
		}
	}
}
