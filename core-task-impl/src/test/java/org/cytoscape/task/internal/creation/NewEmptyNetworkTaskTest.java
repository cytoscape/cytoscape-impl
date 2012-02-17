package org.cytoscape.task.internal.creation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
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
	private CyNetworkManager netmgr;
	@Mock
	private CyNetworkViewManager networkViewManager;
	@Mock
	private CyNetworkNaming namingUtil;
	@Mock
	private CyApplicationManager appManager;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testNewEmptyNetworkTask() throws Exception {

		final NewEmptyNetworkTask task = new NewEmptyNetworkTask(cnf, cnvf, netmgr, networkViewManager, namingUtil, appManager);
		final TaskMonitor taskMonitor = mock(TaskMonitor.class);
		task.run(taskMonitor);

		verify(netmgr, times(1)).addNetwork(any(CyNetwork.class));
		verify(networkViewManager, times(1)).addNetworkView(any(CyNetworkView.class));
	}

}
