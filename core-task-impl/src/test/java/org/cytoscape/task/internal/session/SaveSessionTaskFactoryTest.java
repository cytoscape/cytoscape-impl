package org.cytoscape.task.internal.session;

import static org.mockito.Mockito.mock;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class SaveSessionTaskFactoryTest {

	@Test(expected=NullPointerException.class)
	public void testRun() throws Exception {

		CySessionManager mgr = mock(CySessionManager.class);;
		CySessionWriterManager wmgr = mock(CySessionWriterManager.class);
		RecentlyOpenedTracker tracker = mock(RecentlyOpenedTracker.class);
		CyEventHelper cyEventHelper = mock(CyEventHelper.class);

		
		SaveSessionTaskFactory factory = new SaveSessionTaskFactory(wmgr,mgr,tracker, cyEventHelper);
		
		TaskIterator ti = factory.createTaskIterator();
//		assertNotNull(ti);
//		
//		assertTrue( ti.hasNext() );
//		Task t = ti.next();
//		assertNotNull( t );				
	}	
}
