package org.cytoscape.task.internal.session;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
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
		CyApplicationManager appManager = mock(CyApplicationManager.class);
		RecentlyOpenedTracker tracker = mock(RecentlyOpenedTracker.class);
		TunableSetter ts = mock(TunableSetter.class);

		SynchronousTaskManager synchronousTaskManager = mock(SynchronousTaskManager.class);
		OpenSessionTaskFactory factory = new OpenSessionTaskFactory(mgr, rmgr, appManager, tracker, synchronousTaskManager,ts);
		
		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );				
	}	
}
