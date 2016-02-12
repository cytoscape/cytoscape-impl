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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OpenSessionTaskFactoryTest {
	
	@Mock private TaskMonitor tm;
	@Mock private CySessionManager mgr;
	@Mock private CySessionReaderManager readerMgr;
	@Mock private CyNetworkManager netMgr;
	@Mock private CyTableManager tableMgr;
	@Mock private CyNetworkTableManager netTableMgr;
	@Mock private CyGroupManager grMgr;
	@Mock private TunableSetter ts;
	@Mock private RecentlyOpenedTracker tracker;
	@Mock private CyEventHelper eventHelper;
	
	@Test
	public void testRun() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		OpenSessionTaskFactoryImpl factory = new OpenSessionTaskFactoryImpl(mgr, readerMgr, netMgr, tableMgr,
				netTableMgr, grMgr, tracker, ts, eventHelper);
		
		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);
		
		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull( t );				
	}	
}
