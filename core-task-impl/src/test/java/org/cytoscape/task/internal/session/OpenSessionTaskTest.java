package org.cytoscape.task.internal.session;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OpenSessionTaskTest {
	
	@Mock private TaskMonitor tm;
	@Mock private CySessionManager mgr;
	@Mock private CySessionReaderManager readerManager;
	@Mock private CyApplicationManager appManager;
	@Mock private RecentlyOpenedTracker tracker;
	
	@Mock private CySessionReader reader;
	
	private File sampleFile;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		sampleFile = new File("./src/test/resources/test_session1.cys");
		when(readerManager.getReader(sampleFile.toURI(),sampleFile.getName())).thenReturn(reader);
	}
	
	@Test
	public void testRun() throws Exception {
		final OpenSessionTask t = new OpenSessionTask(mgr, readerManager, appManager, tracker);
		t.setTaskIterator(new TaskIterator(t));

		t.file = sampleFile;
		
		t.run(tm);
		verify(reader, times(1)).run(tm);
	}

}
