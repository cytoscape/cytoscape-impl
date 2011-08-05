package org.cytoscape.task.internal.session;

import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class SaveSessionTaskTest {
	
	@Mock private TaskMonitor tm;
	@Mock private CySessionManager mgr;
	@Mock private CySessionWriterManager writerMgr;
	
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}


	@Test(expected=NullPointerException.class)
	public void testSaveSessionTask() throws Exception {
		final SaveSessionTask t = new SaveSessionTask(writerMgr, mgr);
		t.setTaskIterator(new TaskIterator(t));
		
		t.run(tm);
		// TODO: how can we test classes with generated code (cannot mock them!)
//		verify(reader, times(1)).run(tm);
	}
}
