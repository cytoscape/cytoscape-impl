package org.cytoscape.task.internal.loaddatatable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoadAttributesFileTaskFactoryImplTest {

	@Mock
	CyTableReaderManager rmgr;
	
	@Mock
	CyTableManager tmgr;
	
	@Mock
	TaskMonitor tm;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		
	}

	@Test(expected = NullPointerException.class)
	public void testLoadAttributesFileTaskFactory() throws Exception {

		final LoadAttributesFileTaskFactoryImpl factory = new LoadAttributesFileTaskFactoryImpl(rmgr, tmgr);
		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);

		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull(t);

		((LoadAttributesFileTask) t).file = new File("./src/test/resources/empty.txt");
		
		// This throws NPE, but it;s normal.
		t.run(tm);
	}
}
