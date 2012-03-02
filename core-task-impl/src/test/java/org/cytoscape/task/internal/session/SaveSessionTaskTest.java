package org.cytoscape.task.internal.session;

import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySession;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class SaveSessionTaskTest {
	
	@Mock private TaskMonitor tm;

	private CySession session;
	private String fileNameString = "testFile";
	@Mock private CySessionWriterManager writerMgr;
	@Mock private RecentlyOpenedTracker tracker;
	
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}


	@Test(expected=NullPointerException.class)
	public void testSaveSessionTask() throws Exception {
		final SaveSessionTask t = new SaveSessionTask(writerMgr, session, fileNameString, tracker);
		t.setTaskIterator(new TaskIterator(t));
		
		t.run(tm);
		// TODO: how can we test classes with generated code (cannot mock them!)
//		verify(reader, times(1)).run(tm);
	}
}
