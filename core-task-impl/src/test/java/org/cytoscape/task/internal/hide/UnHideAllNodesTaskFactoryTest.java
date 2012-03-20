package org.cytoscape.task.internal.hide;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;


public class UnHideAllNodesTaskFactoryTest {
	@Test
	public void testRun() throws Exception {
		UndoSupport undoSupport = mock(UndoSupport.class);
		CyEventHelper eventHelper = mock(CyEventHelper.class);
		UnHideAllNodesTaskFactory factory =
			new UnHideAllNodesTaskFactory(undoSupport, eventHelper);

		CyNetworkView view = mock(CyNetworkView.class);
		
		TaskIterator ti = factory.createTaskIterator(view);
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );				
	}
}
