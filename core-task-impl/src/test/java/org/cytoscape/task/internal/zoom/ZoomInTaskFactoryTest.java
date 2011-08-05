package org.cytoscape.task.internal.zoom;


import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


public class ZoomInTaskFactoryTest {
	@Test
	public void testGetTaskIterator() {
		CyNetworkView view = mock(CyNetworkView.class);

		UndoSupport undoSupport = mock(UndoSupport.class);
		ZoomInTaskFactory factory = new ZoomInTaskFactory(undoSupport);
		factory.setNetworkView(view);

		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);

		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );
	}
}
