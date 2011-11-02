package org.cytoscape.plugin.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.plugin.CyPluginAdapter;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class PluginLoaderTaskFactoryTest {
	@Test
	public void testGetTaskIterator() {

		final CyPluginAdapter adapter = mock(CyPluginAdapter.class);

		PluginLoaderTaskFactory factory = new PluginLoaderTaskFactory(adapter);

		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);

		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull(t);
	}
}
