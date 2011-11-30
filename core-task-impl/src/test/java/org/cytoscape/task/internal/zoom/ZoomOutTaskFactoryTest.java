package org.cytoscape.task.internal.zoom;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;


public class ZoomOutTaskFactoryTest {
	@Test
	public void testGetTaskIterator() {
		CyNetworkView view = mock(CyNetworkView.class);
		
		UndoSupport undoSupport = mock(UndoSupport.class);
		ZoomOutTaskFactory factory = new ZoomOutTaskFactory(undoSupport);
		factory.setNetworkView(view);
		
		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );		
	}
}
