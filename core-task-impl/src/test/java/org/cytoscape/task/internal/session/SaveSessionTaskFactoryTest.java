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


import static org.mockito.Mockito.*;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.TaskIterator;


public class SaveSessionTaskFactoryTest {

	public void testRun() throws Exception {
		CySessionWriterManager wmgr = mock(CySessionWriterManager.class);
		RecentlyOpenedTracker tracker = mock(RecentlyOpenedTracker.class);
		CyEventHelper cyEventHelper = mock(CyEventHelper.class);

		CySession session = new CySession.Builder().build();
		
		CySessionManager mgr = mock(CySessionManager.class);
		when(mgr.getCurrentSession()).thenReturn(session);
		
		SaveSessionTaskFactoryImpl factory = new SaveSessionTaskFactoryImpl(wmgr, mgr, tracker, cyEventHelper);
		
		TaskIterator ti = factory.createTaskIterator();
//		assertNotNull(ti);
//		
//		assertTrue( ti.hasNext() );
//		Task t = ti.next();
//		assertNotNull( t );				
	}	
}
