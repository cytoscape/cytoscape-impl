package org.cytoscape.task.internal.session;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NewSessionTaskTest {

	@Mock private TaskMonitor tm;
	@Mock private CySessionManager mgr;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testRun() throws Exception {
		final NewSessionTask t = new NewSessionTask(mgr);

		t.run(tm);
		verify(mgr, times(1)).setCurrentSession(null, null);
	}
}
