package org.cytoscape.task.internal.zoom;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;


public class FitSelectedTaskFactoryTest {
	@Test
	public void testGetTaskIterator() {
		UndoSupport undoSupport = mock(UndoSupport.class);
		FitSelectedTaskFactory factory = new FitSelectedTaskFactory(undoSupport);

		CyNetworkView view = mock(CyNetworkView.class);
		
		TaskIterator ti = factory.createTaskIterator(view);
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );		
	}
}
