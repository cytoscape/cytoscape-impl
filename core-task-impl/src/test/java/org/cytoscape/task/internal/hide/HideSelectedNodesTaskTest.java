package org.cytoscape.task.internal.hide;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.AbstractNetworkViewTaskTest;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class HideSelectedNodesTaskTest extends AbstractNetworkViewTaskTest {
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private CyNetworkView view = viewSupport.getNetworkView();
	@Mock TaskMonitor tm;
	@Mock CyEventHelper eventHelper;
	@Mock UndoSupport undoSupport;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testHideSelectedEdgesTask() throws Exception {
		
		final HideSelectedNodesTask task =
			new HideSelectedNodesTask(undoSupport, eventHelper, view);
		task.run(tm);
	}

}
