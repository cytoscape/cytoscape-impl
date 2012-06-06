package org.cytoscape.task.internal.creation;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NewEmptyNetworkTaskTest {

	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();

	private CyNetworkFactory cnf = support.getNetworkFactory();
	private CyNetworkViewFactory cnvf = viewSupport.getNetworkViewFactory();

	@Mock
	private CyNetworkManager netMgr;
	@Mock
	private CyNetworkViewManager netViewMgr;
	@Mock
	private CyNetworkNaming namingUtil;
	@Mock
	private VisualMappingManager vmm;
	@Mock
	private VisualStyle currentStyle;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		when(vmm.getCurrentVisualStyle()).thenReturn(currentStyle);
	}

	@Test
	public void testNewEmptyNetworkTask() throws Exception {
		final NewEmptyNetworkTask task = new NewEmptyNetworkTask(cnf, cnvf, netMgr, netViewMgr, namingUtil, vmm);
		final TaskMonitor taskMonitor = mock(TaskMonitor.class);
		task.run(taskMonitor);

		verify(netMgr, times(1)).addNetwork(any(CyNetwork.class));
		verify(netViewMgr, times(1)).addNetworkView(any(CyNetworkView.class));
		verify(vmm, times(1)).setVisualStyle(eq(currentStyle), any(CyNetworkView.class));
	}
}
