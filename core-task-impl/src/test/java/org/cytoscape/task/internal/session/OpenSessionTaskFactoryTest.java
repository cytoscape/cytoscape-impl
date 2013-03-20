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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.junit.Test;

public class OpenSessionTaskFactoryTest {
	
	@Test
	public void testRun() throws Exception {
		CySessionManager mgr = mock(CySessionManager.class);;
		CySessionReaderManager rmgr = mock(CySessionReaderManager.class);
		CyApplicationManager appMgr = mock(CyApplicationManager.class);
		RecentlyOpenedTracker tracker = mock(RecentlyOpenedTracker.class);
		TunableSetter ts = mock(TunableSetter.class);
		SynchronousTaskManager synchronousTaskManager = mock(SynchronousTaskManager.class);
		CyNetworkTableManager netTableMgr = mock(CyNetworkTableManager.class);
		
		OpenSessionTaskFactoryImpl factory = new OpenSessionTaskFactoryImpl(mgr, rmgr, appMgr, netTableMgr, tracker,
				synchronousTaskManager,ts);
		
		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);
		
		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull( t );				
	}	
}
