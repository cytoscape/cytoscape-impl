package org.cytoscape.task.internal.hide;


import static org.mockito.Mockito.*;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.AbstractNetworkViewTaskTest;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
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
	@Mock VisualMappingManager vmMgr;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		final VisualStyle vs = mock(VisualStyle.class);
		when(vmMgr.getVisualStyle(any(CyNetworkView.class))).thenReturn(vs);
	}
	
	@Test
	public void testHideSelectedEdgesTask() throws Exception {
		final HideSelectedNodesTask task = new HideSelectedNodesTask(undoSupport, eventHelper, vmMgr, view);
		task.run(tm);
	}

}
