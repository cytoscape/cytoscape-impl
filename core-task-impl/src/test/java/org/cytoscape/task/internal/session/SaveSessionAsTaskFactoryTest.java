package org.cytoscape.task.internal.session;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.junit.Test;
import org.mockito.Mock;

public class SaveSessionAsTaskFactoryTest {

	@Mock
	TunableSetter ts;
	
	@Test
	public void testRun() throws Exception {

		CySessionManager mgr = mock(CySessionManager.class);
		CySessionWriterManager wmgr = mock(CySessionWriterManager.class);
		RecentlyOpenedTracker tracker = mock(RecentlyOpenedTracker.class);
		CyEventHelper cyEventHelper = mock(CyEventHelper.class);

		SaveSessionAsTaskFactory factory = new SaveSessionAsTaskFactory(wmgr,mgr,tracker, cyEventHelper, ts);
		
		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );				
	}	
	
}
