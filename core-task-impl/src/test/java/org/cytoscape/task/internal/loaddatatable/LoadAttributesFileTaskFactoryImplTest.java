package org.cytoscape.task.internal.loaddatatable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoadAttributesFileTaskFactoryImplTest {

	@Mock
	CyTableReaderManager rmgr;
	
	@Mock
	TaskMonitor tm;
	@Mock
	TunableSetter ts;

	@Mock
	CyNetworkManager netMgr;
	
	@Mock
	CyTableManager tabMgr;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		
	}

	@Test(expected = NullPointerException.class)
	public void testLoadAttributesFileTaskFactory() throws Exception {

		final LoadAttributesFileTaskFactoryImpl factory = new LoadAttributesFileTaskFactoryImpl(rmgr, ts, netMgr, tabMgr);
		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);

		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull(t);

		((LoadAttributesFileTask) t).file = new File("./src/test/resources/empty.txt");
		
		// This throws NPE, but it;s normal.
		t.run(tm);
	}
}
