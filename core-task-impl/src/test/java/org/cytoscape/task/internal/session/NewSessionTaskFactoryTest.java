package org.cytoscape.task.internal.session;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class NewSessionTaskFactoryTest {
	
	@Test
	public void testRun() throws Exception {

		CySessionManager mgr = mock(CySessionManager.class);;

		NewSessionTaskFactory factory = new NewSessionTaskFactory(mgr);
		
		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );				
	}
}
