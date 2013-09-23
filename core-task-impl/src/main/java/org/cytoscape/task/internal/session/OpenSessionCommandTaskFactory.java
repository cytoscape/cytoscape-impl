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
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class OpenSessionCommandTaskFactory extends AbstractTaskFactory {

	private final CySessionManager mgr;
	private final CySessionReaderManager rmgr;
	private final CyApplicationManager appManager;
	private final CyNetworkManager netManager;
	private final CyTableManager tableManager;
	private final CyNetworkTableManager netTableManager;
	private final CyGroupManager grManager;
	private final RecentlyOpenedTracker tracker;

	public OpenSessionCommandTaskFactory(final CySessionManager mgr,
									  final CySessionReaderManager rmgr,
									  final CyApplicationManager appManager,
									  final CyNetworkManager netManager,
									  final CyTableManager tableManager,
									  final CyNetworkTableManager netTableManager,
									  final CyGroupManager grManager,
									  final RecentlyOpenedTracker tracker) {
		this.mgr = mgr;
		this.rmgr = rmgr;
		this.appManager = appManager;
		this.netManager = netManager;
		this.tableManager = tableManager;
		this.netTableManager = netTableManager;
		this.grManager = grManager;
		this.tracker = tracker;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new OpenSessionCommandTask(mgr, rmgr, appManager, netManager, 
			                           tableManager, netTableManager, grManager, tracker));
	}
}
