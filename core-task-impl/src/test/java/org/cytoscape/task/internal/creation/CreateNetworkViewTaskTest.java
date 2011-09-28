package org.cytoscape.task.internal.creation;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.undo.UndoableEditSupport;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;


public class CreateNetworkViewTaskTest {
	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	
	private CyNetwork networkModel = support.getNetwork();
	private CyNetworkViewFactory viewFactory = viewSupport.getNetworkViewFactory();
	
	private CyNetworkViewManager networkViewManager = mock(CyNetworkViewManager.class);
	
	@Test
	public void testCreateNetworkViewTask() throws Exception {
		UndoableEditSupport undoableEditSupport = mock(UndoableEditSupport.class);
		UndoSupport undoSupport = mock(UndoSupport.class);
		when(undoSupport.getUndoableEditSupport()).thenReturn(undoableEditSupport);
				
		final TaskMonitor tm = mock(TaskMonitor.class);
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		final CreateNetworkViewTask task =
			new CreateNetworkViewTask(undoSupport, networkModel, viewFactory,
			                          networkViewManager, null, eventHelper);
		
		task.run(tm);
		verify(networkViewManager, times(1)).addNetworkView(any(CyNetworkView.class));
	}

}
