package org.cytoscape.task.internal.zoom;


import javax.swing.undo.UndoableEditSupport;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.task.internal.zoom.FitContentTaskFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class FitContentTaskFactoryTest {
	@Test
	public void testGetTaskIterator() {
		CyNetworkView view = mock(CyNetworkView.class);
		
		UndoableEditSupport undoableEditSupport = mock(UndoableEditSupport.class);
		UndoSupport undoSupport = mock(UndoSupport.class);
		when(undoSupport.getUndoableEditSupport()).thenReturn(undoableEditSupport);

		FitContentTaskFactory factory = new FitContentTaskFactory(undoSupport);
		factory.setNetworkView(view);
		
		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );		
	}
}
