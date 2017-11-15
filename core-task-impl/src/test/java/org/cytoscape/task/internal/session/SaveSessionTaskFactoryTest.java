package org.cytoscape.task.internal.session;

import static org.mockito.Mockito.when;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class SaveSessionTaskFactoryTest {

	@Mock CySessionManager sessionMgr;
	@Mock CySessionWriterManager writerMgr;
	@Mock RecentlyOpenedTracker tracker;
	@Mock CyEventHelper eventHelper;
	@Mock CyServiceRegistrar serviceRegistrar;
	@Mock private TaskMonitor tm;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		
		CySession session = new CySession.Builder().build();
		when(sessionMgr.getCurrentSession()).thenReturn(session);
		
		when(serviceRegistrar.getService(CySessionManager.class)).thenReturn(sessionMgr);
		when(serviceRegistrar.getService(CySessionWriterManager.class)).thenReturn(writerMgr);
		when(serviceRegistrar.getService(RecentlyOpenedTracker.class)).thenReturn(tracker);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
	}

	@Test
	public void testRun() throws Exception {
		SaveSessionTaskFactoryImpl factory = new SaveSessionTaskFactoryImpl(serviceRegistrar);
		
		TaskIterator ti = factory.createTaskIterator();
//		assertNotNull(ti);
//		
//		assertTrue(ti.hasNext());
//		Task t = ti.next();
//		assertNotNull( t );				
	}	
}
