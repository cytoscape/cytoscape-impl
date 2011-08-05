package org.cytoscape.task.internal.proxysettings;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class ProxySettingsTaskFactoryTest {
	@Test
	public void testRun() throws Exception {

		StreamUtil streamUtil = mock(StreamUtil.class);

		ProxySettingsTaskFactory factory = new ProxySettingsTaskFactory(streamUtil);
		
		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );				
	}	
}
