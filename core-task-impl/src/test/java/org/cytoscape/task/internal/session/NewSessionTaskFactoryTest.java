package org.cytoscape.task.internal.session;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.junit.Test;
import org.mockito.Mock;

public class NewSessionTaskFactoryTest {
	
	@Mock
	TunableSetter ts;
	
	@Test
	public void testRun() throws Exception {

		CySessionManager mgr = mock(CySessionManager.class);;

		NewSessionTaskFactory factory = new NewSessionTaskFactory(mgr, ts);
		
		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );				
	}
}
