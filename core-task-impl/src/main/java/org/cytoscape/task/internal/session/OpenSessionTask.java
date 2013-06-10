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
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


/**
 * Call the session reader and read everything in the zip archive.<br>
 * setAcceleratorCombo(java.awt.event.KeyEvent.VK_O, ActionEvent.CTRL_MASK);
 */
public class OpenSessionTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Open Session";
	}
	
	@Tunable(description="Session file to load", params="fileCategory=session;input=true")
	public File file;
	
	private final CySessionManager sessionMgr;
	private final CySessionReaderManager readerMgr;
	private final CyApplicationManager appManager;
	private final CyNetworkManager netManager;
	private final CyTableManager tableManager;
	private final CyNetworkTableManager netTableManager;
	private final CyGroupManager grManager;
	private final RecentlyOpenedTracker tracker;
	
	private CySessionReader reader;
	private Set<CyNetwork> currentNetworkSet;
	private Set<CyGroup> currentGroupSet;


	/**
	 * Constructor.<br>
	 * Add a menu item under "File" and set shortcut.
	 */
	public OpenSessionTask(final CySessionManager mgr,
						   final CySessionReaderManager readerManager,
						   final CyApplicationManager appManager,
						   final CyNetworkManager netManager,
						   final CyTableManager tableManager,
						   final CyNetworkTableManager netTableManager,
						   final CyGroupManager grManager,
						   final RecentlyOpenedTracker tracker) {
		this.sessionMgr = mgr;
		this.readerMgr = readerManager;
		this.appManager = appManager;
		this.netManager = netManager;
		this.tableManager = tableManager;
		this.netTableManager = netTableManager;
		this.grManager = grManager;
		this.tracker = tracker;
	}

	/**
	 * Clear current session and open the cys file.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Opening Session File.\n\nIt may take a while.\nPlease wait...");
		taskMonitor.setProgress(0.0);

		if (file == null)
			throw new NullPointerException("No file specified.");
		
		reader = readerMgr.getReader(file.toURI(), file.getName());
		
		if (reader == null)
			throw new NullPointerException("Failed to find appropriate reader for file: " + file);
		
		// Save the current network and group set, in case loading the new session is cancelled later
		currentNetworkSet = new HashSet<CyNetwork>(netTableManager.getNetworkSet());
		currentGroupSet = new HashSet<CyGroup>();
		
		for (final CyNetwork n : currentNetworkSet)
			currentGroupSet.addAll(grManager.getGroupSet(n));
		
		taskMonitor.setProgress(0.2);
		reader.run(taskMonitor);
		taskMonitor.setProgress(0.8);
		
		if (cancelled)
			return;

		if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
			insertTasksAfterCurrentTask(new LoadSessionWithoutWarningTask());
		else
			insertTasksAfterCurrentTask(new LoadSessionWithWarningTask());
		
		taskMonitor.setProgress(1.0);
	}
	
	@Override
	public void cancel() {
		super.cancel();
		
		if (reader != null)
			reader.cancel(); // Remember to cancel the Session Reader!
	}

	CySession getCySession() {
		return reader.getSession();
	}
	
	private void changeCurrentSession(TaskMonitor taskMonitor) throws Exception {
		final CySession newSession = reader.getSession();
		
		if (newSession == null)
			throw new NullPointerException("Session could not be read for file: " + file);

		sessionMgr.setCurrentSession(newSession, file.getAbsolutePath());
		
		// Set Current network: this is necessary to update GUI.
		final RenderingEngine<CyNetwork> currentEngine = appManager.getCurrentRenderingEngine();
		
		if (currentEngine != null)
			appManager.setCurrentRenderingEngine(currentEngine);
		
		taskMonitor.setProgress(1.0);
		taskMonitor.setStatusMessage("Session file " + file + " successfully loaded.");
		
		// Add this session file URL as the most recent file.
		tracker.add(file.toURI().toURL());
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
		
		@Tunable(description="<html>Current session (all networks and tables) will be lost.<br />Do you want to continue?</html>", params="ForceSetDirectly=true;ForceSetTitle=Open Session")
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