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


public class UnHideAllTaskFactoryTest {
	@Test
	public void testRun() throws Exception {
		UndoSupport undoSupport = mock(UndoSupport.class);
		CyEventHelper eventHelper = mock(CyEventHelper.class);
		UnHideAllTaskFactory factory = new UnHideAllTaskFactory(undoSupport, eventHelper);

		CyNetworkView view = mock(CyNetworkView.class);
		factory.setNetworkView(view);
		
		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );				
	}	
}
