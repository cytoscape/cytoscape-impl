package org.cytoscape.task.internal.zoom;


import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class FitSelectedTaskFactoryTest {
	@Test
	public void testGetTaskIterator() {
		UndoSupport undoSupport = mock(UndoSupport.class);
		FitSelectedTaskFactory factory = new FitSelectedTaskFactory(undoSupport);

		CyNetworkView view = mock(CyNetworkView.class);
		factory.setNetworkView(view);
		
		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );		
	}
}
