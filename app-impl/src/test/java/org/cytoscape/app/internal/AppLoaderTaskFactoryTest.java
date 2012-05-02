package org.cytoscape.app.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.app.internal.AppLoaderTaskFactory;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class AppLoaderTaskFactoryTest {
	@Test
	public void testGetTaskIterator() {

		final CySwingAppAdapter adapter = mock(CySwingAppAdapter.class);

		AppLoaderTaskFactory factory = new AppLoaderTaskFactory(adapter);

		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);

		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull(t);
	}
}
