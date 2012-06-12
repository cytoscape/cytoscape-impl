package org.cytoscape.task.internal.session;

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
