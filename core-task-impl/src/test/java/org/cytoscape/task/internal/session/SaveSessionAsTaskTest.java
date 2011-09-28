package org.cytoscape.task.internal.session;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SaveSessionAsTaskTest {
	
	@Mock private TaskMonitor tm;
	@Mock private CySessionManager mgr;
	@Mock private CySessionWriterManager writerMgr;
	
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}


	@Test(expected=NullPointerException.class)
	public void testSaveSessionAsTask() throws Exception {
		final SaveSessionAsTask t = new SaveSessionAsTask(writerMgr, mgr);
		t.setTaskIterator(new TaskIterator(t));
		
		t.run(tm);
		verify(mgr, times(1)).getCurrentSession();
	}
}
