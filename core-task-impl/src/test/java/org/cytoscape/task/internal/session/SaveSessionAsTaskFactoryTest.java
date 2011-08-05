package org.cytoscape.task.internal.session;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class SaveSessionAsTaskFactoryTest {

	@Test
	public void testRun() throws Exception {

		CySessionManager mgr = mock(CySessionManager.class);;
		CySessionWriterManager wmgr = mock(CySessionWriterManager.class);

		SaveSessionAsTaskFactory factory = new SaveSessionAsTaskFactory(wmgr,mgr);
		
		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );				
	}	
	
}
