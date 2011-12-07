package org.cytoscape.app.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.app.internal.PluginLoaderTaskFactory;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class PluginLoaderTaskFactoryTest {
	@Test
	public void testGetTaskIterator() {

		final CyAppAdapter adapter = mock(CyAppAdapter.class);

		PluginLoaderTaskFactory factory = new PluginLoaderTaskFactory(adapter);

		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);

		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull(t);
	}
}
