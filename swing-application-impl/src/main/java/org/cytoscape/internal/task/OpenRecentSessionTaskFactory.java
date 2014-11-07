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
import org.cytoscape.event.CyEventHelper;
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
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionLoadCancelledEvent;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class OpenRecentSessionTaskFactory extends AbstractTaskFactory {
	
	private final CySessionManager sessionManager;
	private final CySessionReaderManager readerManager;
	private final CyApplicationManager appManager;
	private final CyNetworkManager netManager;
	private final CyTableManager tableManager;
	private final CyNetworkTableManager netTableManager;
	private final CyGroupManager grManager;
	private final RecentlyOpenedTracker tracker;
	private final URL targetSession;
	private final CyEventHelper eventHelper;
	
	public OpenRecentSessionTaskFactory(final CySessionManager sessionManager,
										final CySessionReaderManager readerManager,
										final CyApplicationManager appManager,
										final CyNetworkManager netManager,
										final CyTableManager tableManager,
										final CyNetworkTableManager netTableManager,
										final CyGroupManager grManager,
										final RecentlyOpenedTracker tracker,
										final URL targetSession,
										final CyEventHelper eventHelper) {
		this.sessionManager = sessionManager;
		this.readerManager = readerManager;
		this.appManager = appManager;
		this.netManager = netManager;
		this.tableManager = tableManager;
		this.netTableManager = netTableManager;
		this.grManager = grManager;
		this.tracker = tracker;
		this.targetSession = targetSession;
		this.eventHelper = eventHelper;
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
			if (reader == null)
				throw new IllegalArgumentException("'reader' must not be null.");
			
			this.reader = reader;
		}
		
		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
				insertTasksAfterCurrentTask(new OpenSessionWithoutWarningTask());
			else
				insertTasksAfterCurrentTask(new OpenSessionWithWarningTask());	
		}
		
		public final class OpenSessionWithoutWarningTask extends AbstractTask {
			
			private Set<CyNetwork> currentNetworkSet = new HashSet<CyNetwork>();
			private Set<CyGroup> currentGroupSet = new HashSet<CyGroup>();
			
			@Override
			public void run(final TaskMonitor taskMonitor) throws Exception {
				eventHelper.fireEvent(new SessionAboutToBeLoadedEvent(this));
				
				try {
					taskMonitor.setStatusMessage("Opening Session File.\n\nIt may take a while.\nPlease wait...");
					taskMonitor.setProgress(0.0);
					
					// Save the current network and group set, in case loading the new session is cancelled later
					currentNetworkSet.addAll(netTableManager.getNetworkSet());
					
					for (final CyNetwork n : currentNetworkSet)
						currentGroupSet.addAll(grManager.getGroupSet(n));
					
					reader.run(taskMonitor);
					taskMonitor.setProgress(0.8);
				} catch (Exception e) {
					eventHelper.fireEvent(new SessionLoadCancelledEvent(this, e));
					throw e;
				}
				
				if (cancelled) {
					disposeCancelledSession();
				} else {
					try {
						changeCurrentSession(taskMonitor);
					} catch (Exception e) {
						eventHelper.fireEvent(new SessionLoadCancelledEvent(this, e));
						throw e;
					}
				}
			}
			
			@Override
			public void cancel() {
				super.cancel();
				
				if (reader != null)
					reader.cancel(); // Remember to cancel the Session Reader!
				
				eventHelper.fireEvent(new SessionLoadCancelledEvent(this));
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
		}
		
		public final class OpenSessionWithWarningTask extends AbstractTask {
			
			@Tunable(description="<html>Current session (all networks and tables) will be lost.<br />Do you want to continue?</html>",
					 params="ForceSetDirectly=true;ForceSetTitle=Open Session")
			public boolean loadSession;
			
			@Override
			public void run(final TaskMonitor taskMonitor) throws Exception {
				if (loadSession)
					insertTasksAfterCurrentTask(new OpenSessionWithoutWarningTask());
			}
		}
	}
}
